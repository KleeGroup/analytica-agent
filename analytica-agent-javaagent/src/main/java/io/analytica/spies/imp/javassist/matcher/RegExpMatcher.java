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
package io.analytica.spies.imp.javassist.matcher;

import java.util.StringTokenizer;

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
public final class RegExpMatcher implements Matcher<String> {

	private final char[][] patternParts;
	private final String pattenString;

	/**
	 * @param pattenString initializes the matcher with the glob patterm.
	 */
	public RegExpMatcher(final String pattenString) {
		//Assertion.notNull(pattenString);
		//---------------------------------------------------------------------
		this.pattenString = pattenString;
		patternParts = getPatternParts(pattenString);
	}

	/** {@inheritDoc}*/
	@Override
	public boolean isMatch(final String inputString) {
		//Assertion.notNull(inputString);
		//---------------------------------------------------------------------
		return isMatch(inputString, patternParts);
	}

	private static char[][] getPatternParts(final String pattenString) {
		final String[] sections = tokenize(pattenString, "*", true);
		final char[][] patternParts = new char[sections.length][];
		for (int i = 0; i < sections.length; i++) {
			patternParts[i] = sections[i].toCharArray();
		}
		return patternParts;
	}

	private static boolean isMatch(final String input, final char[][] patternParts) {

		final char[] in = input.toCharArray();
		boolean canSkip = false;
		int currentIndex = 0;
		for (int i = 0; i < patternParts.length; i++) {
			if (patternParts[i][0] == '*') {
				canSkip = true;
			} else {
				if (!canSkip) {
					if (!matchesFixed(in, currentIndex, patternParts[i])) {
						return false;
					}
					currentIndex = currentIndex + patternParts[i].length;
				} else {
					final int m = nextFixedMatch(in, currentIndex, patternParts[i]);
					if (m == -1) {
						return false;
					}
					currentIndex = m + patternParts[i].length;
				}
				canSkip = false;
			}
		}
		return canSkip || currentIndex == in.length;
	}

	private static int nextFixedMatch(final char[] cs, final int offSet, final char[] ps) {

		final int endIndex = cs.length - ps.length + 1;
		for (int i = offSet; i < endIndex; i++) {
			if (matchesFixed(cs, i, ps)) {
				return i;
			}
		}
		return -1;
	}

	private static boolean matchesFixed(final char[] cs, final int offSet, final char[] ps) {

		for (int i = 0; i < ps.length; i++) {
			final int c = i + offSet;
			if (c >= cs.length) {
				return false;
			}
			if (cs[c] != ps[i] && ps[i] != '?') {
				return false;
			}
		}
		return true;
	}

	private static String[] tokenize(final String str, final String delim, final boolean returnDelims) {
		//Assertion.notNull(str);
		//Assertion.notNull(delim);
		final StringTokenizer tokenizer = new StringTokenizer(str, delim, returnDelims);
		final String[] matches = new String[tokenizer.countTokens()];
		for (int i = 0; i < matches.length; i++) {
			matches[i] = tokenizer.nextToken();
		}
		return matches;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "<regexp pattern=\"" + pattenString + "\"/>";
	}
}
