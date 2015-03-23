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

import java.util.regex.Pattern;

/**
 * Pattern de lecture du log.
 * @author npiedeloup
 * @version $Id: $
 */
final class LogPattern {

	private final String code;
	private final String patternRegExp;
	private final boolean error;
	private final boolean startLog;
	private final int indexProcessesJson;
	private final int indexDate;
	private final int indexThreadName;
	private final int indexType;
	private final int indexCategoryTerms;
	private final int indexTime;
	private final boolean processRoot;
	private final boolean cleanStack;

	private transient Pattern pattern;

	/**
	 * Constructeur.
	 * @param code Code du pattern, pouvant servir de type de process
	 * @param patternRegExp RegExp de lecture
	 * @param error Si correspond à une erreur
	 * @param indexProcessesJson Si correspond au Json complet du process
	 * @param indexDate Index de la date dans la regexp
	 * @param indexThreadName Index du thread dans la regexp
	 * @param indexType Index du type dans la regexp (0 pour utiliser le code du pattern)
	 * @param indexSubType Index du sous type dans la regexp (0 si pas utilisé)
	 * @param indexTime Index de la durée dans la regexp (0 si pas utilisé)
	 * @param processRoot Si correspond à une racine de process : généra un process contenant les log encapsulés
	 * @param cleanStack Si correspond à event réinitialisant les process débutés (ex : reboot serveur)
	 */
	public LogPattern(final String code, final String patternRegExp, final boolean error, final int indexProcessesJson, final int indexDate, final int indexThreadName, final int indexType, final int indexCategoryTerms, final int indexTime, final boolean processRoot, final boolean cleanStack) {
		this.code = code;
		this.patternRegExp = patternRegExp;
		this.error = error;
		startLog = indexTime < 1;
		this.indexProcessesJson = indexProcessesJson;
		this.indexDate = indexDate;
		this.indexThreadName = indexThreadName;
		this.indexType = indexType;
		this.indexCategoryTerms = indexCategoryTerms;
		/** TODO : un tableau ? */
		this.indexTime = indexTime;
		this.processRoot = processRoot;
		this.cleanStack = cleanStack;
	}

	/**
	 * @return Code du pattern
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return RegExp de lecture du log
	 */
	public Pattern getPattern() {
		if (pattern == null) {
			pattern = Pattern.compile(patternRegExp);
		}
		return pattern;
	}

	/**
	 * 
	 * @return Si correspond à un json complet
	 */
	public boolean isProcessesJson() {
		return indexProcessesJson > 0;
	}

	/**
	 * @return  Index du json dans la regexp (-1 si pas de json)
	 */
	public int getIndexProcessesJson() {
		return indexProcessesJson;
	}

	/**
	 * @return Si correspond à une erreur
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * @return Si correspond à une ligne de début de process
	 */
	public boolean isStartLog() {
		return startLog;
	}

	/**
	 * @return Index de la date dans la regexp
	 */
	public int getIndexDate() {
		return indexDate;
	}

	/**
	 * @return Index du thread dans la regexp
	 */
	public int getIndexThreadName() {
		return indexThreadName;
	}

	/**
	 * @return Index du type dans la regexp (-1 pour utiliser le code du pattern)
	 */
	public int getIndexType() {
		return indexType;
	}

	/**
	 * @return Index du sous type dans la regexp (-1 si pas utilisé)
	 */
	public int getIndexCategoryTerms() {
		return indexCategoryTerms;
	}

	/**
	 * @return Index de la durée dans la regexp (-1 si pas utilisé)
	 */
	public int getIndexTime() {
		return indexTime;
	}

	/**
	 * @return Si correspond à une racine de process : généra un process contenant les log encapsulés
	 */
	public boolean isProcessRoot() {
		return processRoot;
	}

	/**
	 * @return Si correspond à event réinitialisant les process débutés (ex : reboot serveur)
	 */
	public boolean isCleanStack() {
		return cleanStack;
	}
}
