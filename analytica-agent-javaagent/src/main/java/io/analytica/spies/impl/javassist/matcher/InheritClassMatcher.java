/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidi�re - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you permission to link this library
 * with independent modules to produce an executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the license of that module.
 * An independent module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version of the library,
 * but you are not obliged to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 */
package io.analytica.spies.impl.javassist.matcher;

import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Match une class h�ritant ou impl�mentant d'une autre class.
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
		//Assertion.notEmpty(pattenString);
		//---------------------------------------------------------------------
		superClassMatcher = new RegExpMatcher(pattenString);
	}

	/** {@inheritDoc}*/
	@Override
	public boolean isMatch(final CtClass input) {
		//Assertion.notNull(input);
		try {
			return isParentMatch(input, true);
		} catch (final NotFoundException e) {
			System.err.println("Could not check inherit matchs of " + input.getName() + ",  exception : " + e.getMessage());
			throw new RuntimeException(e);
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
