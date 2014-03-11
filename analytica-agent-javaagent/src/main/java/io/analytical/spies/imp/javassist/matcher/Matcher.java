package io.analytical.spies.imp.javassist.matcher;

/**
 * @author npiedeloup
 * @version $Id: Matcher.java,v 1.1 2011/05/12 10:16:12 prahmoune Exp $
 * @param <O> Type d'objet de ce Matcher
 */
public interface Matcher<O> {

	/**
	 * @param input objet � tester
	 * @return Si ld'entr�e est valide vis-�-vis du ou des patterns de ce Matcher
	 */
	boolean isMatch(O input);
}
