package com.kleegroup.analyticaimpl.spies.logs;

import io.vertigo.kernel.lang.Assertion;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Info sur le log lu.
 * @author npiedeloup
 * @version $Id: $
 */
final class LogInfo {
	private static final long ROOT_DECAL_MS = 0;
	private LogInfo startLogInfo = null;
	private final Date dateEvent;
	private final Date startDateEvent;
	private final String json;
	private final String threadName;
	private final String type;
	private final String subType;
	private final long time;
	private final LogPattern logPattern;

	/**
	 * Constructeur.
	 * @param dateEvent Date du log
	 * @param threadName Nom du thread
	 * @param type Type associé
	 * @param subType Sous catégorie
	 * @param time Temps d'execution du traitement loggé
	 * @param logPattern Pattern de lecture du log
	 */
	public LogInfo(final Date dateEvent, final String threadName, final String type, final String subType, final long time, final LogPattern logPattern) {
		this.threadName = threadName;
		this.type = type.toUpperCase();
		this.subType = subType;
		this.logPattern = logPattern;
		if (logPattern.isStartLog()) {
			this.time = time;
			startDateEvent = dateEvent;
			if (time > 0) {
				this.dateEvent = new Date(dateEvent.getTime() + time + (logPattern.isProcessRoot() ? ROOT_DECAL_MS : 0));
			} else {
				this.dateEvent = new Date(dateEvent.getTime() + (logPattern.isProcessRoot() ? ROOT_DECAL_MS : 0));
			}
		} else {
			this.time = time;
			this.dateEvent = new Date(dateEvent.getTime() + (logPattern.isProcessRoot() ? ROOT_DECAL_MS : 0));
			startDateEvent = new Date(dateEvent.getTime() - time - (logPattern.isProcessRoot() ? ROOT_DECAL_MS : 0));
		}
		json = null;
		//System.out.println("found " + toString());
	}

	/**
	 * Constructeur.
	 *  @param threadName Nom du thread
	 * @param json Json complet du process
	 * @param logPattern Pattern de lecture du log
	 */
	public LogInfo(final String threadName, final String json, final LogPattern logPattern) {
		this.threadName = threadName;
		this.json = json;
		this.logPattern = logPattern;
		type = null;
		subType = null;
		time = -1;
		dateEvent = null;
		startDateEvent = null;
	}

	/**
	 * Associe un log de début à ce log de fin.
	 * @param startLogInfo Log de début
	 */
	public void linkStartLogInfo(final LogInfo startLogInfo) {
		Assertion.checkNotNull(startLogInfo);
		Assertion.checkArgument(startLogInfo != this, "Cycle");
		Assertion.checkArgument(startLogInfo.getLogPattern().isStartLog(), "Ce LogInfo n''est pas un log de début : {0}", startLogInfo);
		Assertion.checkArgument(startLogInfo.getType().equals(type), "Ce LogInfo n''est pas du même type : {0} != {1}", type, startLogInfo);
		Assertion.checkArgument(startLogInfo.getSubType().equals(subType), "Ce LogInfo n''est pas du même sous-type : {0} != {1}", subType, startLogInfo);
		//---------------------------------------------------------------------
		this.startLogInfo = startLogInfo;
	}

	/**
	 * @return Date de début du process
	 */
	public Date getStartDateEvent() {
		if (startLogInfo != null) {
			return startLogInfo.getStartDateEvent();
		}
		return startDateEvent;
	}

	/**
	 * @return Date de fin du process
	 */
	public Date getDateEvent() {
		return dateEvent;
	}

	/**
	 * @return Nom du thread
	 */
	public String getThreadName() {
		return threadName;
	}

	/**
	 * @return Json
	 */
	public String getJson() {
		return json;
	}

	/**
	 * @return Type du process
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return Sous catégories du process
	 */
	public String getSubType() {
		return subType;
	}

	/**
	 * @return Durée du process
	 */
	public long getTime() {
		if (time == -1 && startLogInfo != null) {
			return getDateEvent().getTime() - getStartDateEvent().getTime();
		}
		return time;
	}

	/**
	 * @return LogPattern de lecture du log
	 */
	public LogPattern getLogPattern() {
		return logPattern;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
		final SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm:ss.SSS ");
		return sdfDate.format(getStartDateEvent()) + (startLogInfo != null ? "(link)" : "") + ">" + sdfHour.format(getDateEvent()) + threadName + " " + type + " " + subType + " " + getTime() + " " + logPattern.getCode();
	}

}
