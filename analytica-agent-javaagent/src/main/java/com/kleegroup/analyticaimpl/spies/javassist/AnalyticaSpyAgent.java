package com.kleegroup.analyticaimpl.spies.javassist;

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
 * <code>Premain-Class: com.kleegroup.analyticaimpl.spies.asm.AnalyticaSpyAgent</code>
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
			 
			inst.addTransformer(transformer, canRetransform);
			// code < jdk1.6 : inst.addTransformer(transformer);
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
				//code < jdk1.6 : can't reload 
				instrumentation.retransformClasses(clazz);
			} catch (final Throwable e) {
				System.err.println("Erreur retransformClasses " + clazz.getName() + " : (" + e.getClass().getName() + ") " + e.getMessage());
				//throw e;
			}
		}
	}
}
