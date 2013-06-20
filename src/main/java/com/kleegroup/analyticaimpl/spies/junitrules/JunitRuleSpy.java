package com.kleegroup.analyticaimpl.spies.junitrules;

import javax.inject.Inject;

import kasper.kernel.Home;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.kleegroup.analytica.agent.AgentManager;

/**
 * Intercepteur pour la gestion des jUnit Rule.
 * @author npiedeloup
 * @version $Id: KTransactionInterceptor.java,v 1.1 2012/07/20 12:43:53 pchretien Exp $
 */
public class JunitRuleSpy implements MethodRule {
	private static final String PT_JUNIT = "JUNIT";
	private static final String ME_ERROR_PCT = "ERROR_PCT";
	private static final String ME_ERROR_HEADER = "ERROR_HEADER";

	//private final AgentManager agentManager;

	/**
	 * Constructeur.
	 * @param agentManager  Agent de récolte de process
	 */
	@Inject
	public JunitRuleSpy() {
		//Assertion.notNull();
		//---------------------------------------------------------------------

	}

	@Override
	public Statement apply(final Statement base, final FrameworkMethod meth, final Object target) {
		return new JunitRuleStatement(base, meth, Home.getContainer().getManager(AgentManager.class));
	}

	static class JunitRuleStatement extends Statement {

		private final AgentManager agentManager;
		private final Statement base;
		private final FrameworkMethod meth;

		public JunitRuleStatement(final Statement base, final FrameworkMethod meth, final AgentManager agentManager) {
			this.base = base;
			this.meth = meth;
			this.agentManager = agentManager;
		}

		@Override
		public void evaluate() throws Throwable {
			agentManager.startProcess(PT_JUNIT, meth.getType().getSimpleName(), meth.getName());
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
