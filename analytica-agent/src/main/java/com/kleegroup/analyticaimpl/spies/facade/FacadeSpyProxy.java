package com.kleegroup.analyticaimpl.spies.facade;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import kasper.kernel.util.Assertion;
import kasper.kernel.util.ClassUtil;

import com.kleegroup.analytica.agent.AgentManager;

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
		Assertion.notNull(object);
		Assertion.notEmpty(facadeType);
		Assertion.notEmpty(facadeName);
		Assertion.notNull(agentManager);
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
		Assertion.notNull(object);
		Assertion.notNull(facadeType);
		// ---------------------------------------------------------------------
		final Class<?>[] interfaceArray = getAllInterfaces(object.getClass());
		Assertion.precondition(interfaceArray.length > 0, "L''object {0} doit avoir au moins une interface à monitorer pour poser le spy.", object.getClass().getSimpleName());
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
		final Set<Class> interfaces = new HashSet<Class>();
		Class<?> superClass = myClass;
		while (superClass != null) {
			interfaces.addAll(Arrays.asList(superClass.getInterfaces()));
			superClass = superClass.getSuperclass();
		}
		return interfaces.toArray(new Class[interfaces.size()]);
	}

}
