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
 * <code>Premain-Class: com.kleegroup.analyticaimpl.spies.javassist.AnalyticaSpyAgent</code>
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
		final List<Pattern> patterns = new ArrayList<Pattern>(testedPatterns.length);

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
