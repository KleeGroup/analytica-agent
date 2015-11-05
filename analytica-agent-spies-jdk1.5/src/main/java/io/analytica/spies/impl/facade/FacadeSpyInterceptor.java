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
package io.analytica.spies.impl.facade;

import io.analytica.agent.impl.KProcessCollectorContainer;
import io.analytica.api.KMeasureType;
import io.analytica.api.KProcessType;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * jdk 1.5 implemetation of FacadeSpyInterceptor. For jdk 7 or above look into the project analytica-agent-spies.
 * Service Layer Interceptor
 * @author npiedeloup
 * @version $Id: KTransactionInterceptor.java,v 1.1 2012/07/20 12:43:53 pchretien Exp $
 */
public class FacadeSpyInterceptor implements MethodInterceptor {
	private static final String ME_ERROR_HEADER = "ERROR_HEADER";

	@Inject
	public FacadeSpyInterceptor() {
	}

	/** {@inheritDoc} */
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		KProcessCollectorContainer.getInstance().startProcess(KProcessType.SERVICE.toString(), invocation.getThis().getClass().getSimpleName(), invocation.getMethod().getName());
		try {
			return invocation.proceed();
		} catch (final Throwable th) {
			KProcessCollectorContainer.getInstance().setMeasure(KMeasureType.OTHER_ERROR.toString(), 100);
			KProcessCollectorContainer.getInstance().addMetaData(ME_ERROR_HEADER, th.getMessage());
			throw th;
		} finally {
			KProcessCollectorContainer.getInstance().stopProcess();
		}
	}
}
