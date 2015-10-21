/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidi�re - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.analytica.spies.impl.facade;

import io.analytica.agent.impl.KProcessCollectorContainer;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Intercepteur pour la gestion des Process au niveau de la couche service.
 * @author npiedeloup
 * @version $Id: KTransactionInterceptor.java,v 1.1 2012/07/20 12:43:53 pchretien Exp $
 */
public class FacadeSpyInterceptor implements MethodInterceptor {
	private static final String PT_FACADE = "FACADE";
	private static final String ME_ERROR_PCT = "ERROR_PCT";
	private static final String ME_ERROR_HEADER = "ERROR_HEADER";

	/**
	 * Constructeur.
	 * @param agentManager  Agent de r�colte de process
	 */
	@Inject
	public FacadeSpyInterceptor() {
	}

	/** {@inheritDoc} */
	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		KProcessCollectorContainer.getInstance().startProcess(PT_FACADE, invocation.getThis().getClass().getSimpleName(), invocation.getMethod().getName());
		try {
			return invocation.proceed();
		} catch (final Throwable th) {
			KProcessCollectorContainer.getInstance().setMeasure(ME_ERROR_PCT, 100);
			KProcessCollectorContainer.getInstance().addMetaData(ME_ERROR_HEADER, th.getMessage());
			throw th;
		} finally {
			KProcessCollectorContainer.getInstance().stopProcess();//La mesure Duration est sett�e implicitement par le stop
		}
	}
}
