package com.kleegroup.analyticaimpl.spies.logs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.inject.Named;

import kasper.kernel.exception.KRuntimeException;
import kasper.kernel.lang.Activeable;
import kasper.kernel.lang.Option;
import kasper.kernel.util.Assertion;
import kasper.resource.ResourceManager;

import com.google.gson.Gson;
import com.kleegroup.analytica.agent.AgentManager;
import com.kleegroup.analytica.core.KProcess;
import com.kleegroup.analytica.core.KProcessBuilder;

/**
 * Monitoring de facade par Proxy automatique sur les interfaces.
 * @author npiedeloup
 * @version $Id: CounterProxy.java,v 1.5 2010/11/23 09:49:33 npiedeloup Exp $
 */
public final class LogSpyReader implements Activeable {

	private static final String ME_ERROR_PCT = "ERROR_PCT";
	private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000;

	private final AgentManager agentManager;
	private final URL logFileUrl;
	private final List<String> dateFormats;
	private final List<LogPattern> patterns;

	private final Map<String, List<LogInfo>> logInfoMap = new HashMap<String, List<LogInfo>>();
	private Date lastDateRead = new Date(0);

	private static final Comparator<? super LogInfo> LOG_INFO_COMPARATOR = new Comparator<LogInfo>() {
		@Override
		public int compare(final LogInfo o1, final LogInfo o2) {
			if (o1 == null) {
				return o2 != null ? -1 : 0;
			} else if (o2 == null) {
				return 1;
			}
			final int compare = o1.getStartDateEvent().compareTo(o2.getStartDateEvent());
			return compare != 0 ? compare : o1.getType().compareTo(o2.getType());
		}
	};

	/**
	 * Constructeur.
	 * @param agentManager  Agent de récolte de process
	 * @param resourceManager Resource manager
	 * @param logFileUrl Url du fichier de log à parser
	 * @param confFileUrl Url du fichier de configuration
	 */
	@Inject
	public LogSpyReader(final AgentManager agentManager, final ResourceManager resourceManager, @Named("logFileUrl") final String logFileUrl, @Named("confFileUrl") final String confFileUrl) {
		Assertion.notNull(agentManager);
		Assertion.notNull(resourceManager);
		Assertion.notEmpty(logFileUrl);
		//---------------------------------------------------------------------
		this.agentManager = agentManager;
		this.logFileUrl = resourceManager.resolve(logFileUrl);

		final URL confFile = resourceManager.resolve(confFileUrl);
		final String confJson = readConf(confFile);
		final LogSpyConf conf = new Gson().fromJson(confJson, LogSpyConf.class);
		dateFormats = conf.getDateFormats();
		patterns = conf.getLogPatterns();
	}

	private List<LogInfo> getLogInfos(final String threadName) {
		List<LogInfo> logInfos = logInfoMap.get(threadName);
		if (logInfos == null) {
			logInfos = new ArrayList<LogInfo>();
			logInfoMap.put(threadName, logInfos);
		}
		return logInfos;
	}

	private void appendLogInfo(final LogInfo logInfo) {
		final List<LogInfo> logInfos = getLogInfos(logInfo.getThreadName());
		logInfos.add(logInfo);
	}

	private KProcess extractProcess(final String threadName) {
		final List<LogInfo> logInfos = getLogInfos(threadName);
		//1 - on tri par date de début
		Collections.sort(logInfos, LOG_INFO_COMPARATOR);

		//		System.out.println("extract >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		//		for (final LogInfo logInfo : logInfos) {
		//			System.out.println("    " + logInfo.toString());
		//		}
		//		System.out.println("extract <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

		final Stack<KProcessBuilder> stackProcessBuilder = new Stack<KProcessBuilder>();
		final Stack<LogInfo> stackLogInfo = new Stack<LogInfo>();
		KProcessBuilder processBuilder;
		//2 - on dépile les lignes de log
		for (final LogInfo logInfo : logInfos) {
			processBuilder = new KProcessBuilder(logInfo.getStartDateEvent(), logInfo.getTime(), logInfo.getType(), logInfo.getSubType());
			processBuilder.setMeasure(ME_ERROR_PCT, logInfo.getLogPattern().isError() ? 100 : 0);
			if (stackLogInfo.isEmpty()) {
				//2a - la premiere ligne crée la racine
				stackLogInfo.push(logInfo);
				stackProcessBuilder.push(processBuilder);
			} else {
				//2b - Ajoute les suivantes dans la pile
				push(logInfo, processBuilder, stackLogInfo, stackProcessBuilder);
			}
		}
		//3 - A la fin on depile tout
		KProcess process;
		do {
			final KProcessBuilder processBuilderPrevious = stackProcessBuilder.pop();
			stackLogInfo.pop();
			process = processBuilderPrevious.build();
			//3a - Le process est ajouté au parent s'il existe
			if (!stackLogInfo.isEmpty()) {
				final KProcessBuilder processBuilderParent = stackProcessBuilder.peek();
				processBuilderParent.addSubProcess(process);
			}
		} while (!stackLogInfo.isEmpty());

		//4 - On purge les logs lus 
		getLogInfos(threadName).clear();

		//5 - On retourne le process résultat
		return process;
	}

	private void push(final LogInfo logInfo, final KProcessBuilder processBuilder, final Stack<LogInfo> stackLogInfo, final Stack<KProcessBuilder> stackProcessBuilder) {
		final LogInfo logInfoPrevious = stackLogInfo.peek();
		final Date dateEvent = logInfo.getDateEvent();
		if (!dateEvent.after(logInfoPrevious.getDateEvent()) && !dateEvent.before(logInfoPrevious.getStartDateEvent())) {
			//Si l'event est inclus dans les dates du précédent on l'ajout dedans
			stackLogInfo.push(logInfo);
			stackProcessBuilder.push(processBuilder);
		} else {
			//Si l'event n'est pas inclus dans les dates du précédent, 
			//on dépile le précédent qu'on ajoute au parent, 
			//puis on empile le nouveau suivant la même regle
			final KProcessBuilder processBuilderPrevious = stackProcessBuilder.pop();
			stackLogInfo.pop();

			Assertion.invariant(!stackProcessBuilder.isEmpty(), "La stackProcessBuilder est vide");
			final KProcessBuilder processBuilderParent = stackProcessBuilder.peek();
			processBuilderParent.addSubProcess(processBuilderPrevious.build());
			push(logInfo, processBuilder, stackLogInfo, stackProcessBuilder);
		}
	}

	/** {@inheritDoc} */
	public void start() {
		try {
			//on lit le fichier
			final InputStream in = logFileUrl.openStream();
			try {
				final Reader isr = new InputStreamReader(in);
				try {
					final BufferedReader br = new BufferedReader(isr);
					try {
						readLogFile(br);
					} finally {
						br.close();
					}
				} finally {
					isr.close();
				}
			} finally {
				in.close();
			}
		} catch (final IOException e) {
			throw new KRuntimeException(e, "Erreur de lecture du log");
		}
	}

	private String readConf(final URL confFileURL) {
		final StringBuilder sb = new StringBuilder();
		try {
			//on lit le fichier
			final InputStream in = confFileURL.openStream();
			try {
				final Reader isr = new InputStreamReader(in);
				try {
					final BufferedReader br = new BufferedReader(isr);
					try {
						String currentLine;
						while ((currentLine = br.readLine()) != null) {
							sb.append(currentLine).append('\n');
						}
					} finally {
						br.close();
					}
				} finally {
					isr.close();
				}
			} finally {
				in.close();
			}
		} catch (final IOException e) {
			throw new KRuntimeException(e, "Erreur de lecture de la conf");
		}
		return sb.toString();
	}

	private void readLogFile(final BufferedReader br) throws IOException {
		long lineCount = 0;
		long parsedLineCount = 0;
		String currentLine;
		Option<LogInfo> logInfoOption;
		while ((currentLine = br.readLine()) != null) {
			lineCount++;
			logInfoOption = parseLine(currentLine);
			if (logInfoOption.isDefined()) {
				parsedLineCount++;
				final LogInfo logInfo = logInfoOption.get();
				appendLogInfo(logInfo);
				if (logInfo.getLogPattern().isProcessRoot()) {
					agentManager.add(extractProcess(logInfo.getThreadName()));
				} else if (logInfo.getLogPattern().isCleanStack()) {
					logInfoMap.clear();
				}
			}
			if (lineCount % 10000 == 0) {
				System.out.println("read " + lineCount + " lines, parsed: " + parsedLineCount + " (" + parsedLineCount * 100 / lineCount + "%)");
			}
		}
	}

	private Option<LogInfo> parseLine(final String currentLine) {
		//(date, nom thread, type, sous type, si erreur, durée, si log de fin
		for (final LogPattern logPattern : patterns) {
			final Matcher startMatch = logPattern.getPattern().matcher(currentLine);
			if (startMatch.find()) {
				final String date = startMatch.group(logPattern.getIndexDate());
				final String threadName = logPattern.getIndexThreadName() != -1 ? startMatch.group(logPattern.getIndexThreadName()) : "none";
				final String type = logPattern.getIndexType() != -1 ? startMatch.group(logPattern.getIndexType()) : logPattern.getCode();
				final String subType = logPattern.getIndexSubType() != -1 ? startMatch.group(logPattern.getIndexSubType()) : "";
				final String time = logPattern.getIndexTime() != -1 ? startMatch.group(logPattern.getIndexTime()) : null;
				return Option.some(new LogInfo(readDate(date), threadName, type, subType, readTime(time), logPattern));
			}
		}
		return Option.none();
	}

	private long readTime(final String time) {
		return time != null ? Long.valueOf(time) : -1;
	}

	private Date readDate(final String date) {
		Date dateTime = null;
		for (final String dateFormat : dateFormats) {
			final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
			try {
				dateTime = sdf.parse(date);
				if (dateTime.getTime() <= ONE_DAY_MILLIS) {
					dateTime = adjustDate(dateTime, lastDateRead);
				}
			} catch (final ParseException e) {
				//rien on tente les autres formats
			}
		}
		if (dateTime == null) {
			throw new KRuntimeException("Date non reconnue : " + date);
		}
		lastDateRead = dateTime;
		return dateTime;
	}

	private static Date adjustDate(final Date dateToAdjust, final Date lastDateRead) {
		final long dateToAdjustTime = dateToAdjust.getTime();
		Assertion.invariant(dateToAdjustTime < ONE_DAY_MILLIS, "La dateTime à ajuster contenait déjà la date");
		final long lastDateReadTime = lastDateRead.getTime();
		final long dateAdjustedTime = dateToAdjustTime + lastDateReadTime / ONE_DAY_MILLIS * ONE_DAY_MILLIS;
		if (dateAdjustedTime >= lastDateReadTime - ONE_DAY_MILLIS / 24) { //au moins 1h d'écart (ce point est util pour les changements de jour donc on passe de 22h à 7h par exemple)
			return new Date(dateAdjustedTime);
		} else {
			return new Date(dateAdjustedTime + ONE_DAY_MILLIS);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		//rien
	}
}
