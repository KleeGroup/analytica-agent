package com.kleegroup.analyticaimpl.spies.facade;

import javax.inject.Inject;

import kasper.kernel.util.Assertion;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.kleegroup.analytica.agent.AgentManager;
import com.kleegroup.analytica.server.data.WhatDimension;

/**
 * Intercepteur pour la gestion des Process au niveau de la couche service.
 * @author npiedeloup
 * @version $Id: KTransactionInterceptor.java,v 1.1 2012/07/20 12:43:53 pchretien Exp $
 */
public class FacadeSpyInterceptor implements MethodInterceptor {
	private static final String PT_FACADE = "FACADE";
	private static final String ME_ERROR_PCT = "ERROR_PCT";
	private static final String ME_ERROR_HEADER = "ERROR_HEADER";

	private final AgentManager agentManager;

	/**
	 * Constructeur.
	 * @param agentManager  Agent de récolte de process
	 */
	@Inject
	public FacadeSpyInterceptor(final AgentManager agentManager) {
		Assertion.notNull(agentManager);
		//---------------------------------------------------------------------
		this.agentManager = agentManager;
	}

	/** {@inheritDoc} */
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		agentManager.startProcess(PT_FACADE, invocation.getClass().getSimpleName() + WhatDimension.SEPARATOR + invocation.getMethod().getName());
		try {
			return invocation.proceed();
		} catch (final Throwable th) {
			agentManager.setMeasure(ME_ERROR_PCT, 100);
			agentManager.addMetaData(ME_ERROR_HEADER, th.getMessage());
			throw th;
		} finally {
			agentManager.stopProcess();//La mesure Duration est settée implicitement par le stop
		}
	}
}
