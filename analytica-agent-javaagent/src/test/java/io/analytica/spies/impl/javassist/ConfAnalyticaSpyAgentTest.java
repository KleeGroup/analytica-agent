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
package io.analytica.spies.impl.javassist;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Implementation d'un agent de jvm.
 * Celui ci doit etre inclus dans un jar et passé en parametre à la jvm :
 * <code>-javaagent:"monjar.jar"=option</code>
 * Ce jar doit avoir un manifest qui contient la ligne suivante :
 * <code>Premain-Class: io.analytica.spies.imp.javassist.AnalyticaSpyAgent</code>
 *
 * Cet agent ajoute un ClassFileTransformer spécifique qui a pour but d'instrumenter
 * les méthodes selon un paramétrage externe.
 * L'option de l'agent dans la ligne de commande représente le nom du fichier de paramétrage.
 *
 * @author npiedeloup
 * @version $Id: MemoryLeakAgent.java,v 1.2 2012/09/28 09:30:03 pchretien Exp $
 */
public final class ConfAnalyticaSpyAgentTest {

	/**
	 * Test simple avec deux compteurs.
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg.
	 * Chaque article coute 10€.
	 */
	@Test
	public void testEscapeComments() {
		final String[] testedTexts = { //
				"/* First comment \n first comment—line two*/\n/* Second comment */", //
				"start_code();\n/* First comment */\nmore_code(); \n/* Second comment */\nend_code();", //
				"/*\n * Common multi-line comment style.\n */\n/* Second comment */", //
				"start_code();\n/****\n * Common multi-line comment style.\n ****/\nmore_code(); \n/*\n * Another common multi-line comment style.\n */\nend_code();", //
				"/****\n * Common multi-line comment style.\n ****/\n/****\n * Another common multi-line comment style.\n */" //
		};
		final String[] testedPatterns = { //
				//"/\\*.*\\*/", //
				//	"/\\*(.|[\\r\\n])*\\*/",//
				"/\\*(.|[\\r\\n])*?\\*/",//
				"(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)",//
		};
		doTestParsing(testedTexts, testedPatterns);
	}

	private void doTestParsing(final String[] testedlogs, final String[] testedPatterns) {
		final List<Pattern> patterns = new ArrayList<>(testedPatterns.length);

		for (final String testedPattern : testedPatterns) {
			patterns.add(Pattern.compile(testedPattern));
		}

		for (final String testedlog : testedlogs) {
			System.out.println("\n\n" + testedlog);
			for (int i = 0; i < testedPatterns.length; i++) {
				System.out.println(i + ": " + testedlog.replaceAll(testedPatterns[i], ""));
			}
		}
	}
}
