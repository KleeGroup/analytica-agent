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
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you permission to link this library
 * with independent modules to produce an executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the license of that module.
 * An independent module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version of the library,
 * but you are not obliged to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 */
package io.analytica.spies.imp.javassist;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementation d'un agent de jvm.
 * Celui ci doit etre inclus dans un jar et passé en parametre à la jvm :
 * <code>-javaagent:"monjar.jar"=option</code>
 * Ce jar doit avoir un manifest qui contient la ligne suivante :
 * <code>Premain-Class: io.analytica.spies.imp.javassist.AnalyticaSpyAgent</code>
 *
 * Cet agent ajoute un ClassFileTransformer spécifique qui a pour but d'instrumenter
 * les méthodes selon un paramétrage externe.
 * L'option de l'agent dans la ligne de commande représente le nom du fichier de paramétrage.
 *
 * @author npiedeloup
 * @version $Id: MemoryLeakAgent.java,v 1.2 2012/09/28 09:30:03 pchretien Exp $
 */
public final class AnalyticaSpyAgent {
	private static Instrumentation instrumentation;
	private static ClassFileTransformer transformer;

	private AnalyticaSpyAgent() {
		//rien
	}

	/**
	 * Methode respectant l'API (sans interface java) pour l'utilisation de -javaagent.
	 * @param agentArgs parametre <code>option</code> de la ligne de commande
	 * @param inst Instrumentation modifiable fournit par la JVM
	 */
	public static void premain(final String agentArgs, final Instrumentation inst) {
		//System.out.println("premain method invoked with args: {" + agentArgs + "} and inst: {" + inst + "}");
		addTransformer(agentArgs, inst, false);
	}

	/**
	 * Methode respectant l'API (sans interface java) pour le chargement de l'agent apres démarrage de la JVM.
	 * @param agentArgs parametre <code>option</code> de la ligne de commande
	 * @param inst Instrumentation modifiable fournit par la JVM
	 */
	public static void agentmain(final String agentArgs, final Instrumentation inst) {
		//System.out.println("agentmain method invoked with args: {" + agentArgs + "} and inst: {" + inst + "}");
		addTransformer(agentArgs, inst, true);
	}

	public static void stopAgent() {
		if (instrumentation != null && transformer != null) {
			instrumentation.removeTransformer(transformer);
			reloadAll();
			instrumentation = null;
			transformer = null;
			System.out.println("AnalyticaAgent Stop at " + new Date());
		}
	}

	public static void reloadAll() {
		//code < jdk1.6 : can't reload 
		System.out.println("AnalyticaAgent reload All");
		doReload(obtainInstrumentedClasses());
	}

	private static void addTransformer(final String agentArgs, final Instrumentation inst, final boolean canRetransform) {
		if (instrumentation == null) {
			instrumentation = inst;
			System.out.println("AnalyticaAgent prepare at " + new Date());
			transformer = new AnalyticaSpyTransformer(agentArgs);

			//code jdk1.6+ inst.addTransformer(transformer, canRetransform);
			// code jdk1.5 : inst.addTransformer(transformer);
			inst.addTransformer(transformer, canRetransform);
			System.out.println("AnalyticaAgent Start at " + new Date());
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					AnalyticaSpyAgent.stopAgent();
				}
			});
			reloadAll();
		}
	}

	private static Class<?>[] obtainInstrumentedClasses() {
		final Class<?>[] allLoadedClasses = instrumentation.getAllLoadedClasses();
		final List<Class<?>> classes = new ArrayList<Class<?>>(allLoadedClasses.length);
		for (final Class<?> clazz : allLoadedClasses) {
			if (((AnalyticaSpyTransformer) transformer).shouldTransform(clazz.getClassLoader(), clazz.getName())) {
				classes.add(clazz);
			}
		}
		return classes.toArray(new Class[classes.size()]);
	}

	//	private static void reload(final Class<?>... classes) {
	//		System.out.println("AnalyticaAgent reload " + Arrays.asList(classes));
	//		doReload(classes);
	//	}

	private static void doReload(final Class<?>... classes) {
		for (final Class<?> clazz : classes) {
			try {
				//code jdk1.6+ : instrumentation.retransformClasses(clazz);
				//code jdk1.5 : can't reload
				instrumentation.retransformClasses(clazz);
			} catch (final Throwable e) {
				System.err.println("Erreur retransformClasses " + clazz.getName() + " : (" + e.getClass().getName() + ") " + e.getMessage());
				//throw e;
			}
		}
	}
}
