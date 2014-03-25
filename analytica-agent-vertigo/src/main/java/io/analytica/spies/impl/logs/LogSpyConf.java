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

import java.util.List;

/**
 * Configuration du parser.
 * @author npiedeloup
 * @version $Id: $
 */
public final class LogSpyConf {

	private final String systemName;
	private final String[] systemLocation;

	private final List<String> dateFormats;
	private final List<LogPattern> logPatterns;

	/**
	 * Constructeur.
	 * @param systemName System name
	 * @param systemLocation System location
	 * @param dateFormats Formats de date reconnu
	 * @param logPatterns Patterns de lecture du log
	 */
	public LogSpyConf(final String systemName, final String[] systemLocation, final List<String> dateFormats, final List<LogPattern> logPatterns) {
		this.systemName = systemName;
		this.systemLocation = systemLocation;
		this.dateFormats = dateFormats;
		this.logPatterns = logPatterns;
	}

	/**
	 * @return System Name
	 */
	public String getSystemName() {
		return systemName;
	}

	/**
	 * @return System Location
	 */
	public String[] getSystemLocation() {
		return systemLocation;
	}

	/**
	 * @return Formats de date reconnu
	 */
	public List<String> getDateFormats() {
		return dateFormats;
	}

	/**
	 * @return Patterns de lecture du log
	 */
	public List<LogPattern> getLogPatterns() {
		return logPatterns;
	}

}
