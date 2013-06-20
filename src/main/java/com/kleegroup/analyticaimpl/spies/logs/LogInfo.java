package com.kleegroup.analyticaimpl.spies.logs;

import java.text.SimpleDateFormat;
import java.util.Date;

import kasper.kernel.util.Assertion;

/**
 * Info sur le log lu.
 * @author npiedeloup
 * @version $Id: $
 */
final class LogInfo {

	private LogInfo startLogInfo = null;
	private final Date dateEvent;
	private final Date startDateEvent;
	private final String threadName;
	private final String type;
	private final String subType;
	private final long time;
	private final LogPattern logPattern;

	/**
	 * Constructeur.
	 * @param dateEvent Date du log
	 * @param threadName Nom du thread
	 * @param type Type associ�
	 * @param subType Sous cat�gorie
	 * @param time Temps d'execution du traitement logg�
	 * @param logPattern Pattern de lecture du log
	 */
	public LogInfo(final Date dateEvent, final String threadName, final String type, final String subType, final long time, final LogPattern logPattern) {
		this.dateEvent = dateEvent;
		this.threadName = threadName;
		this.type = type.toUpperCase();
		this.subType = subType;
		this.logPattern = logPattern;
		if (logPattern.isStartLog()) {
			this.time = -1;
			startDateEvent = dateEvent;
		} else {
			this.time = time;
			startDateEvent = new Date(dateEvent.getTime() - time - (logPattern.isProcessRoot() ? 16 : 0));
		}
		//System.out.println("found " + toString());
	}

	/**
	 * Associe un log de d�but � ce log de fin.
	 * @param startLogInfo Log de d�but
	 */
	public void linkStartLogInfo(final LogInfo startLogInfo) {
		Assertion.notNull(startLogInfo);
		Assertion.precondition(startLogInfo != this, "Cycle");
		Assertion.precondition(startLogInfo.getLogPattern().isStartLog(), "Ce LogInfo n''est pas un log de d�but : {0}", startLogInfo);
		Assertion.precondition(startLogInfo.getType().equals(type), "Ce LogInfo n''est pas du m�me type : {0} != {1}", type, startLogInfo);
		Assertion.precondition(startLogInfo.getSubType().equals(subType), "Ce LogInfo n''est pas du m�me sous-type : {0} != {1}", subType, startLogInfo);
		//---------------------------------------------------------------------
		this.startLogInfo = startLogInfo;
	}

	/**
	 * @return Date de d�but du process
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
	 * @return Type du process
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return Sous cat�gories du process
	 */
	public String getSubType() {
		return subType;
	}

	/**
	 * @return Dur�e du process
	 */
	public long getTime() {
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
		return sdfDate.format(getStartDateEvent()) + (startLogInfo != null ? "(link)" : "") + ">" + sdfHour.format(getDateEvent()) + threadName + " " + type + " " + subType + " " + time + " " + logPattern.getCode();
	}

}
