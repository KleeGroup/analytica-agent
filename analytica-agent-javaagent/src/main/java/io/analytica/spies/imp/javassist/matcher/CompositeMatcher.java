package io.analytica.spies.imp.javassist.matcher;

import java.util.List;

/**
 * OR.
 *
 * @author $Author: prahmoune $
 * @version $Revision: 1.1 $
 * @param <O> y
 */
public final class CompositeMatcher<O> implements Matcher<O> {

	private final List<Matcher<O>> matchers;

	/**
	 * Constructeur.
	 * @param matchers Matcher composés
	 */
	public CompositeMatcher(final List<Matcher<O>> matchers) {
		this.matchers = matchers;
	}

	/** {@inheritDoc}*/
	public boolean isMatch(final O input) {
		for (final Matcher<O> matcher : matchers) {
			if (matcher.isMatch(input)) {
				return true;
			}
		}
		return false;
	}

	/** {@inheritDoc}*/
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<or>");
		for (final Matcher<O> matcher : matchers) {
			sb.append(matcher);
		}
		sb.append("</or>");
		return sb.toString();
	}
}
