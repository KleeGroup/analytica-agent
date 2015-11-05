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
import io.analytica.api.Assertion;
import io.analytica.api.KMeasureType;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * jdk 1.5 implemetation of FacadeSpyProxy. For jdk 7 or above look into the project analytica-agent-spies.
 * Facade monitoring using an automatic Proxy on interfaces.
 * @author npiedeloup
 * @version $Id: CounterProxy.java,v 1.5 2010/11/23 09:49:33 npiedeloup Exp $
 */
public final class FacadeSpyProxy implements InvocationHandler {
	private static final String ME_ERROR_HEADER = "ERROR_HEADER";

	private final Object object;
	private final String facadeType;
	private final String facadeName;

	/**
	 * .
	 * @param object 
	 * @param facadeType 
	 * @param facadeName 
	 */
	FacadeSpyProxy(final Object object, final String facadeType, final String facadeName) {
		Assertion.checkNotNull(object);
		Assertion.checkArgNotEmpty(facadeType);
		Assertion.checkArgNotEmpty(facadeName);
		//----------------------------------------------------------------------
		this.object = object;
		this.facadeType = facadeType;
		this.facadeName = facadeName;
	}

	/** {@inheritDoc} */
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		KProcessCollectorContainer.getInstance().startProcess(facadeType, facadeName, method.getName());
		try {
			return method.invoke(object, args);
		} catch (final Throwable th) {
			KProcessCollectorContainer.getInstance().setMeasure(KMeasureType.OTHER_ERROR.toString(), 100);
			KProcessCollectorContainer.getInstance().addMetaData(ME_ERROR_HEADER, th.getMessage());
			throw th;
		} finally {
			KProcessCollectorContainer.getInstance().stopProcess();
		}
	}

	/**
	 * Connecting to all the methodes
	 * @param <O> Type of the object on witch we will add the proxy
	 * @param object object on witch we will add the proxy
	 * @param facadeType
	 * @return Proxy
	 */

	public static <O extends Object> O plugSpy(final O object, final String facadeType) {
		Assertion.checkNotNull(object);
		Assertion.checkNotNull(facadeType);
		// ---------------------------------------------------------------------
		final Class<?>[] interfaceArray = getAllInterfaces(object.getClass());
		Assertion.checkState(interfaceArray.length > 0, "The object {0} must have at least one interface to monitor in orde to insert the spy.", object.getClass().getSimpleName());
		final String facadeName = interfaceArray.length == 1 ? interfaceArray[0].getClass().getSimpleName() : object.getClass().getSimpleName();
		final InvocationHandler handler = new FacadeSpyProxy(object, facadeType, facadeName);
		return (O) Proxy.newProxyInstance(object.getClass().getClassLoader(), interfaceArray, handler);
	}

	/**
	 * Retriving all the intefaces of a class
	 * @param myClass C
	 * @return Class[]
	 */
	private static Class[] getAllInterfaces(final Class myClass) {
		final Set<Class> interfaces = new HashSet<Class>();
		Class<?> superClass = myClass;
		while (superClass != null) {
			interfaces.addAll(Arrays.asList(superClass.getInterfaces()));
			superClass = superClass.getSuperclass();
		}
		return interfaces.toArray(new Class[interfaces.size()]);
	}

}
