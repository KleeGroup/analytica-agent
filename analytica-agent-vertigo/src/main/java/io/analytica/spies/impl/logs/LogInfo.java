/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidière - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>
 */
package io.analytica.spies.impl.logs;

import io.vertigo.lang.Assertion;

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
	private final String categoryTerms;
	private final long time;
	private final LogPattern logPattern;

	/**
	 * Constructeur.
	 * @param dateEvent Date du log
	 * @param threadName Nom du thread
	 * @param type Type associé
	 * @param categoryTerms Sous catégorie
	 * @param time Temps d'execution du traitement loggé
	 * @param logPattern Pattern de lecture du log
	 */
	public LogInfo(final Date dateEvent, final String threadName, final String type, final String categoryTerms, final long time, final LogPattern logPattern) {
		this.threadName = threadName;
		this.type = type.toUpperCase();
		this.categoryTerms = categoryTerms;
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
		categoryTerms = null;
		time = -1;
		dateEvent = null;
		startDateEvent = null;
	}

	/**
	 * Associe un log de début à ce log de fin.
	 * @param logInfo Log de début
	 */
	public void linkStartLogInfo(final LogInfo logInfo) {
		Assertion.checkNotNull(logInfo);
		Assertion.checkArgument(logInfo != this, "Cycle");
		Assertion.checkArgument(logInfo.getLogPattern().isStartLog(), "Ce LogInfo n''est pas un log de début : {0}", logInfo);
		Assertion.checkArgument(logInfo.getType().equals(type), "Ce LogInfo n''est pas du même type : {0} != {1}", type, logInfo);
		Assertion.checkArgument(logInfo.getCategoryTerms().equals(categoryTerms), "Ce LogInfo n''est pas du même sous-type : {0} != {1}", categoryTerms, logInfo);
		//---------------------------------------------------------------------
		startLogInfo = logInfo;
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
	public String getCategoryTerms() {
		return categoryTerms;
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
		return sdfDate.format(getStartDateEvent()) + (startLogInfo != null ? "(link)" : "") + ">" + sdfHour.format(getDateEvent()) + threadName + " " + type + " " + categoryTerms + " " + getTime() + " " + logPattern.getCode();
	}

}
