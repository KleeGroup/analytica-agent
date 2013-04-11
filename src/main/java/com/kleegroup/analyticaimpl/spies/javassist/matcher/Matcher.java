package com.kleegroup.analyticaimpl.spies.javassist.matcher;

/**
 * @author npiedeloup
 * @version $Id: Matcher.java,v 1.1 2011/05/12 10:16:12 prahmoune Exp $
 */
public interface Matcher {

	/**
	 * @param inputString chaine à tester
	 * @return Si la chaine d'entrée est valide vis-à-vis du ou des patterns de ce Matcher
	 */
	boolean isMatch(String inputString);
}
