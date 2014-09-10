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

import io.analytica.agent.AgentManager;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.ClassUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Monitoring de facade par Proxy automatique sur les interfaces.
 * @author npiedeloup
 * @version $Id: CounterProxy.java,v 1.5 2010/11/23 09:49:33 npiedeloup Exp $
 */
public final class FacadeSpyProxy implements InvocationHandler {
	private static final String ME_ERROR_PCT = "ERROR_PCT";
	private static final String ME_ERROR_HEADER = "ERROR_HEADER";

	private final Object object;
	private final String facadeType;
	private final String facadeName;
	private final AgentManager agentManager;

	/**
	 * Contruction du proxy.
	 * @param object Objet proxisé
	 * @param facadeType Type de la facade
	 * @param facadeName Nom de la facade
	 * @param agentManager Agent de récolte de process
	 */
	FacadeSpyProxy(final Object object, final String facadeType, final String facadeName, final AgentManager agentManager) {
		Assertion.checkNotNull(object);
		Assertion.checkArgNotEmpty(facadeType);
		Assertion.checkArgNotEmpty(facadeName);
		Assertion.checkNotNull(agentManager);
		//----------------------------------------------------------------------
		this.object = object;
		this.facadeType = facadeType;
		this.facadeName = facadeName;
		this.agentManager = agentManager;
	}

	/** {@inheritDoc} */
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		agentManager.startProcess(facadeType, facadeName, method.getName());
		try {
			return ClassUtil.invoke(object, method, args);
		} catch (final Throwable th) {
			agentManager.setMeasure(ME_ERROR_PCT, 100);
			agentManager.addMetaData(ME_ERROR_HEADER, th.getMessage());
			throw th;
		} finally {
			agentManager.stopProcess();//La mesure Duration est settée implicitement par le stop
		}
	}

	/**
	 * Instrumentation automatique de toutes les méthodes d'un objet. 
	 * 
	 * @param <O> Type de l'objet sur lequel ajouter le proxy.
	 * @param object Object sur lequel ajouter le Spy
	 * @param facadeType Pseudo-type de la facade (Service, WS, Mail, ...)
	 * @param agentManager Agent de récolte de process
	 * @return Proxy de l'objet passé avec le spy placé sur les méthodes de ses interfaces.
	 */

	public static <O extends Object> O plugSpy(final O object, final String facadeType, final AgentManager agentManager) {
		Assertion.checkNotNull(object);
		Assertion.checkNotNull(facadeType);
		// ---------------------------------------------------------------------
		final Class<?>[] interfaceArray = getAllInterfaces(object.getClass());
		Assertion.checkState(interfaceArray.length > 0, "L''object {0} doit avoir au moins une interface à monitorer pour poser le spy.", object.getClass().getSimpleName());
		final String facadeName = interfaceArray.length == 1 ? interfaceArray[0].getClass().getSimpleName() : object.getClass().getSimpleName();
		final InvocationHandler handler = new FacadeSpyProxy(object, facadeType, facadeName, agentManager);
		return (O) Proxy.newProxyInstance(object.getClass().getClassLoader(), interfaceArray, handler);
	}

	/**
	 * Récupère l'ensemble des interfaces implémentées par cette class.
	 * Cette méthode est utilisé pour la création dynamique des Proxy.
	 * @param myClass Classe dont on veut les interfaces
	 * @return Class[] Tableau des interfaces de cette classe (il peut y avoir des doublons)
	 */
	private static Class[] getAllInterfaces(final Class myClass) {
		final Set<Class> interfaces = new HashSet<>();
		Class<?> superClass = myClass;
		while (superClass != null) {
			interfaces.addAll(Arrays.asList(superClass.getInterfaces()));
			superClass = superClass.getSuperclass();
		}
		return interfaces.toArray(new Class[interfaces.size()]);
	}

}
