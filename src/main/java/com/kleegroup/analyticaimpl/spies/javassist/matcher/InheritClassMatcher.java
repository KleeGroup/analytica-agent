package com.kleegroup.analyticaimpl.spies.javassist.matcher;

import javassist.CtClass;
import javassist.NotFoundException;
import kasper.kernel.exception.KRuntimeException;
import kasper.kernel.util.Assertion;

/**
 * Match une class héritant ou implémentant d'une autre class.
 * Exemple :
 * <li>#inherits java.sql.DataSource
 * <li>#inherits myproject.domain.Service
 * 
 * @author npiedeloup
 * @version $Id: GlobMatcher.java,v 1.1 2011/05/12 10:16:12 prahmoune Exp $
 */
public final class InheritClassMatcher implements Matcher<CtClass> {
	private static final String OBJECT_CLASSNAME = Object.class.getName();
	private final Matcher<String> superClassMatcher;

	/**
	 * @param pattenString initializes the matcher with the glob patterm.
	 */
	public InheritClassMatcher(final String pattenString) {
		Assertion.notEmpty(pattenString);
		//---------------------------------------------------------------------
		superClassMatcher = new RegExpMatcher(pattenString);
	}

	/** {@inheritDoc}*/
	public boolean isMatch(final CtClass input) {
		Assertion.notNull(input);
		try {
			return isParentMatch(input, true);
		} catch (final NotFoundException e) {
			System.err.println("Could not check inherit matchs of " + input.getName() + ",  exception : " + e.getMessage());
			throw new KRuntimeException(e);
		}
	}

	private boolean isParentMatch(final CtClass input, final boolean checkName) throws NotFoundException {
		if (OBJECT_CLASSNAME.equals(input.getName())) {
			return false; //le cas d'arret simple
		}
		if (checkName && superClassMatcher.isMatch(input.getName())) {
			return true; //le cas ou ca match
		}
		boolean isMatch = false;
		for (final CtClass ctInterface : input.getInterfaces()) {
			isMatch = isMatch || isParentMatch(ctInterface, true);
		}
		isMatch = isMatch || isParentMatch(input.getSuperclass(), true);
		return isMatch;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "<inherits class=\"" + superClassMatcher + "\"/>";
	}
}
