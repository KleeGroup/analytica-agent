package io.analytical.spies.imp.javassist.matcher;

/**
 * Matcher static: toujours le même résultat. 
 *
 * @author npiedeloup
 * @version $Id: GlobMatcher.java,v 1.1 2011/05/12 10:16:12 prahmoune Exp $
 * @param <O> Type d'objet 
 */
public final class StaticMatcher<O> implements Matcher<O> {

	private final boolean result;

	/**
	 * @param result resultat du matching.
	 */
	public StaticMatcher(final boolean result) {
		this.result = result;
	}

	/** {@inheritDoc}*/
	public boolean isMatch(final O inputString) {
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "<static \"" + result + "\"/>";
	}
}
