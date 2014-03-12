package io.analytica.spies.impl.junitrules;

import io.analytica.AbstractVertigoStartTestCaseJU4;
import io.analytica.spies.impl.junitrules.JunitRuleSpy;

import org.junit.Rule;
import org.junit.Test;

/**
 * Test of JunitRule Agent.
 * Make a process around all tests (before setUp and after tearDown).
 * @author npiedeloup
 */
public final class AnalyticaSpyJunitRuleTest extends AbstractVertigoStartTestCaseJU4 {

	@Rule
	public JunitRuleSpy junitRule = new JunitRuleSpy();

	/** {@inheritDoc} */
	@Override
	protected void doSetUp() throws Exception {
		startServer();
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
		//check is useless : process will be close AFTER this test.
		//checkMetricCount("duration", 1, "JUNIT", "io.analytica.spies.impl.junitrules.AnalyticaSpyJunitRuleTest", "testJunitRule");
	}

}
