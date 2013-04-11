package com.kleegroup.analyticaimpl.spies.javassist;

import java.lang.instrument.Instrumentation;
import java.util.Date;

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
		addTransformer(agentArgs, inst);
	}

	/**
	 * Methode respectant l'API (sans interface java) pour le chargement de l'agent apres démarrage de la JVM.
	 * @param agentArgs parametre <code>option</code> de la ligne de commande
	 * @param inst Instrumentation modifiable fournit par la JVM
	 */
	public static void agentmain(final String agentArgs, final Instrumentation inst) {
		//System.out.println("agentmain method invoked with args: {" + agentArgs + "} and inst: {" + inst + "}");
		addTransformer(agentArgs, inst);
	}

	private static void addTransformer(final String agentArgs, final Instrumentation inst) {
		inst.addTransformer(new AnalyticaSpyTransformer(agentArgs));
		System.out.println("Start at " + new Date());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Stop at " + new Date());
			}
		});
	}
}
