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
	 * @param error Si correspond � une erreur
	 * @param indexDate Index de la date dans la regexp
	 * @param indexThreadName Index du thread dans la regexp
	 * @param indexType Index du type dans la regexp (-1 pour utiliser le code du pattern)
	 * @param indexSubType Index du sous type dans la regexp (-1 si pas utilis�)
	 * @param indexTime Index de la dur�e dans la regexp (-1 si pas utilis�)
	 * @param processRoot Si correspond � une racine de process : g�n�ra un process contenant les log encapsul�s
	 * @param cleanStack Si correspond � event r�initialisant les process d�but�s (ex : reboot serveur)
	 */
	public LogPattern(final String code, final String patternRegExp, final boolean error, final int indexDate, final int indexThreadName, final int indexType, final int indexSubType, final int indexTime, final boolean processRoot, final boolean cleanStack) {
		this.code = code;
		this.patternRegExp = patternRegExp;
		this.error = error;
		startLog = indexTime == -1;
		this.indexDate = indexDate;
		this.indexThreadName = indexThreadName;
		this.indexType = indexType;
		this.indexSubType = indexSubType;
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
	 * @return Si correspond � une erreur
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * @return Si correspond � une ligne de d�but de process
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
	 * @return Index du sous type dans la regexp (-1 si pas utilis�)
	 */
	public int getIndexSubType() {
		return indexSubType;
	}

	/**
	 * @return Index de la dur�e dans la regexp (-1 si pas utilis�)
	 */
	public int getIndexTime() {
		return indexTime;
	}

	/**
	 * @return Si correspond � une racine de process : g�n�ra un process contenant les log encapsul�s
	 */
	public boolean isProcessRoot() {
		return processRoot;
	}

	/**
	 * @return Si correspond � event r�initialisant les process d�but�s (ex : reboot serveur)
	 */
	public boolean isCleanStack() {
		return cleanStack;
	}
}