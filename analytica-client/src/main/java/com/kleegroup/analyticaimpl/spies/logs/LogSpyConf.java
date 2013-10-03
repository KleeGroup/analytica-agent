package com.kleegroup.analyticaimpl.spies.logs;

import java.util.List;

/**
 * Configuration du parser.
 * @author npiedeloup
 * @version $Id: $
 */
public final class LogSpyConf {

	private final List<String> dateFormats;
	private final List<LogPattern> logPatterns;

	/**
	 * Constructeur.
	 * @param dateFormats Formats de date reconnu
	 * @param logPatterns Patterns de lecture du log
	 */
	public LogSpyConf(final List<String> dateFormats, final List<LogPattern> logPatterns) {
		this.dateFormats = dateFormats;
		this.logPatterns = logPatterns;
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
