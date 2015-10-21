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
package io.analytica.api;

import java.util.Collection;
import java.util.regex.Pattern;

public final class Assertion {
	public static void checkNotNull(final Object value, final String msg) {
		if (value == null) {
			throw new NullPointerException(msg);
		}
	}

	public static void checkNotNull(final Object value) {
		if (value == null) {
			throw new NullPointerException();
		}
	}

	/**
	 * Permet de tester le caractère renseigné (non vide) d'une chaine.
	 * @param str String Chaine non vide
	 */
	public static void checkArgNotEmpty(final String str) {
		checkNotNull(str);
		if (StringUtil.isEmpty(str)) {
			throw new IllegalArgumentException("String must not be empty");
		}
	}

	public static void checkArgument(final boolean test, final String msg, final Object... params) {
		if (!test) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void ckeckRegex(final String s, final Pattern pattern, final String info) {
		if (!pattern.matcher(s).matches()) {
			throw new IllegalArgumentException(info + " " + s + " must match regex :" + pattern.pattern());
		}
	}

	public static void ckeckNotEmpty(final Collection<?> collection, final String msg) {
		if (collection == null) {
			throw new NullPointerException(msg);
		}
		if (collection.isEmpty()) {
			throw new NullPointerException(msg);
		}
	}

	/**
	 * Vérification d'un état.
	 * S'utilise de maniére courante dans les calculs pour vérifer les états de variables au cours du traitement.
	 * S'utilise comme postCondition
	 *
	 * @param test Expression booléenne qui doit être vérifiée
	 * @param msg Message affiché si le test <b>n'est pas</b> vérifié.
	 * @param params paramètres du message
	 */
	public static void checkState(final boolean test, final String msg, final Object... params) {
		if (!test) {
			throw new IllegalStateException(StringUtil.format(msg, params));
		}
	}
}
