package com.kleegroup.analyticaimpl.spies.junitrules;

import kasper.kernel.Home;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.kleegroup.analytica.agent.AgentManager;

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
			final AgentManager agentManager = Home.getContainer().getManager(AgentManager.class);
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
