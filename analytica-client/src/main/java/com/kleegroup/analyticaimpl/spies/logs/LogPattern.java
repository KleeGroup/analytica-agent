package com.kleegroup.analyticaimpl.spies.logs;

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
	private final int indexSubType;
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
	public LogPattern(final String code, final String patternRegExp, final boolean error, final int indexProcessesJson, final int indexDate, final int indexThreadName, final int indexType, final int indexSubType, final int indexTime, final boolean processRoot, final boolean cleanStack) {
		this.code = code;
		this.patternRegExp = patternRegExp;
		this.error = error;
		startLog = indexTime < 1;
		this.indexProcessesJson = indexProcessesJson;
		this.indexDate = indexDate;
		this.indexThreadName = indexThreadName;
		this.indexType = indexType;
		this.indexSubType = indexSubType;
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
	public int getIndexSubType() {
		return indexSubType;
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
