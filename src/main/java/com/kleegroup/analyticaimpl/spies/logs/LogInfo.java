package com.kleegroup.analyticaimpl.spies.logs;

import java.util.Date;

/**
 * Info sur le log lu.
 * @author npiedeloup
 * @version $Id: $
 */
final class LogInfo {

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
	 * @param type Type associé
	 * @param subType Sous catégorie
	 * @param time Temps d'execution du traitement loggé
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
			startDateEvent = new Date(dateEvent.getTime() - time);
		}
		//System.out.println("found " + toString());
	}

	/**
	 * @return Date de début du process
	 */
	public Date getStartDateEvent() {
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
	 * @return Sous catégories du process
	 */
	public String getSubType() {
		return subType;
	}

	/**
	 * @return Durée du process
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
		return startDateEvent.toString() + " " + threadName + " " + type + " " + subType + " " + time + " " + logPattern.getCode();
	}

}
