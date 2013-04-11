package com.kleegroup.analyticaimpl.spies.javassist.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * OR.
 *
 * @author $Author: prahmoune $
 * @version $Revision: 1.1 $
 */
public final class CompositeMatcher implements Matcher {

	private final List<Matcher> matchers;

	private CompositeMatcher(final List<Matcher> matchers) {
		this.matchers = matchers;
	}

	/**
	 * @param patterns  Properties contenant les pattern ou liste de pattern avec joker *
	 * @return un Matcher pour valider l'appartenance d'une chaine à un pattern
	 */
	public static Matcher buildCompositeGlobMatcher(final List<String> patterns) {
		Matcher matcher;
		final List<Matcher> matchers = new ArrayList<Matcher>(10);
		for (final String pattern : patterns) {
			matchers.add(new GlobMatcher(pattern));
		}
		if (matchers.size() == 1) {
			matcher = matchers.get(0);
		} else {
			matcher = new CompositeMatcher(matchers);
		}
		return matcher;
	}

	/**
	 * @param pattenString pattern ou liste de pattern avec joker *, séparés par ,
	 * @return un Matcher pour valider l'appartenance d'une chaine à un pattern
	 */
	public static Matcher buildCompositeGlobMatcher(final String pattenString) {
		Matcher matcher;
		final List<Matcher> matchers = new ArrayList<Matcher>(10);
		final StringTokenizer tokenizer = new StringTokenizer(pattenString, ",");
		while (tokenizer.hasMoreTokens()) {
			final String token = tokenizer.nextToken();
			matchers.add(new GlobMatcher(token));
		}
		if (matchers.size() == 1) {
			matcher = matchers.get(0);
		} else {
			matcher = new CompositeMatcher(matchers);
		}
		return matcher;
	}

	/** {@inheritDoc}*/
	public boolean isMatch(final String inputString) {

		for (final Matcher matcher : matchers) {
			if (matcher.isMatch(inputString)) {
				return true;
			}
		}
		return false;
	}

	/** {@inheritDoc}*/
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<or-matcher>");
		for (final Matcher matcher : matchers) {
			sb.append(matcher);
		}
		sb.append("</or-matcher>");
		return sb.toString();
	}
}
