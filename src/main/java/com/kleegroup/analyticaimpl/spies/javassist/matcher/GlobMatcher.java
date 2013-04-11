package com.kleegroup.analyticaimpl.spies.javassist.matcher;

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
public final class GlobMatcher implements Matcher {

	private final char[][] patternParts;
	private final String pattenString;

	/**
	 * @param pattenString initializes the matcher with the glob patterm.
	 */
	public GlobMatcher(final String pattenString) {
		//Assertion.notNull(pattenString);
		this.pattenString = pattenString;
		patternParts = getPatternParts(pattenString);
	}

	/** {@inheritDoc}*/
	public boolean isMatch(final String inputString) {
		//Assertion.notNull(inputString);
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
		return true;
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
		return "<glob pattern=\"" + pattenString + "\"/>";
	}
}
