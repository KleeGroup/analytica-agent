package com.kleegroup.analyticaimpl.spies.javassist.matcher;

import javassist.CtBehavior;

/**
 * Class matches simple regular expressions of the form:
 * <li>this*
 * <li>* is so *
 * <li>input_file_*.dat
 * <li>com.ml.gdfs.common.util.text.*
 * <li>?he q???k br?wn fox
 * <li>java 1.4.?
 *
 * @author npiedeloup
 * @version $Id: GlobMatcher.java,v 1.1 2011/05/12 10:16:12 prahmoune Exp $
 */
public final class CtBehaviorMatcher implements Matcher<CtBehavior> {

	private final RegExpMatcher methodNameMatcher;

	/**
	 * @param pattenString initializes the matcher with the glob patterm.
	 */
	public CtBehaviorMatcher(final String pattenString) {
		//Assertion.notEmpty(pattenString);
		//---------------------------------------------------------------------
		methodNameMatcher = new RegExpMatcher(pattenString);
	}

	/** {@inheritDoc}*/
	public boolean isMatch(final CtBehavior input) {
		//Assertion.notNull(input);
		return methodNameMatcher.isMatch(input.getName());
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "<method>" + methodNameMatcher + "</method>";
	}
}
