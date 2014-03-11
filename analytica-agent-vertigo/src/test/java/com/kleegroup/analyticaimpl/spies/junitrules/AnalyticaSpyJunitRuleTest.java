package com.kleegroup.analyticaimpl.spies.junitrules;

import org.junit.Rule;
import org.junit.Test;

import vertigo.AbstractTestCaseJU4;

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
public final class AnalyticaSpyJunitRuleTest extends AbstractTestCaseJU4 {

	@Rule
	public JunitRuleSpy junitRule = new JunitRuleSpy();

	/** {@inheritDoc} */
	@Override
	protected void doSetUp() throws Exception {
		//
	}

	/**
	 * Test simple avec deux compteurs. 
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg. 
	 * Chaque article coute 10€.
	 */
	@Test
	public void testJunitRule() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			//rien
		}
		flushAgentToServer();
		checkMetricCount("duration", 1, "JUNIT", "com.kleegroup.analyticaimpl.spies.junitrules.AnalyticaSpyJunitRuleTest", "testJunitRule");
	}

	@Override
	protected void flushAgentToServer() {
		//rien en local pas d'attente
	}

}
