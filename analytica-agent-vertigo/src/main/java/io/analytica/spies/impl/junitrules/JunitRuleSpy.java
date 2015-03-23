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

import io.analytica.agent.AgentManager;
import io.vertigo.core.Home;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Intercepteur pour la gestion des jUnit Rule.
 * @author npiedeloup
 * @version $Id: KTransactionInterceptor.java,v 1.1 2012/07/20 12:43:53 pchretien Exp $
 */
public class JunitRuleSpy implements TestRule {
	private static final String PT_JUNIT = "JUNIT";
	private static final String ME_ERROR_PCT = "ERROR_PCT";
	private static final String ME_ERROR_HEADER = "ERROR_HEADER";

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new JunitRuleStatement(base, description);
	}

	static class JunitRuleStatement extends Statement {

		private final Statement base;
		private final Description description;

		public JunitRuleStatement(final Statement base, final Description description) {
			this.base = base;
			this.description = description;
		}

		@Override
		public void evaluate() throws Throwable {
			final AgentManager agentManager = Home.getComponentSpace().resolve(AgentManager.class);
			agentManager.startProcess(PT_JUNIT, description.getTestClass().getSimpleName(), description.getMethodName());
			try {
				base.evaluate();
			} catch (final Throwable th) {
				agentManager.setMeasure(ME_ERROR_PCT, 100);
				agentManager.addMetaData(ME_ERROR_HEADER, th.getMessage());
				throw th;
			} finally {
				agentManager.stopProcess();//La mesure Duration est settée implicitement par le stop
			}
		}
	}
}
