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
 */
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
