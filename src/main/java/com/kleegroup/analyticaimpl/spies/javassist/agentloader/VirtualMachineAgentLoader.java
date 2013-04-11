package com.kleegroup.analyticaimpl.spies.javassist.agentloader;

import java.lang.management.ManagementFactory;

import kasper.kernel.exception.KRuntimeException;

import org.apache.log4j.Logger;

/**
 * Class utilitaire de chargement d'un agent de la VM, apr�s son d�marrage.
 * @author npiedeloup
 * @version $Id: VirtualMachineAgentLoader.java,v 1.1 2011/05/12 10:16:12 prahmoune Exp $
 */
public final class VirtualMachineAgentLoader {

	private static final Logger LOG = Logger.getLogger(VirtualMachineAgentLoader.class);
	private static final String VIRTUAL_MACHINE_CLASS_NAME = "com.sun.tools.attach.VirtualMachine";

	private VirtualMachineAgentLoader() {
		//rien
	}

	/**
	 * Charge un agent � chaud.
	 * @param agentPath Chemin vers le jar de l'agent
	 * @param option option de l'agent
	 */
	public static void loadAgent(final String agentPath, final String option) {
		LOG.info("dynamically loading javaagent: " + agentPath + "=" + option);
		final String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
		final int p = nameOfRunningVM.indexOf('@');
		final String pid = nameOfRunningVM.substring(0, p);
		try {
			//on check, pour indiquer o� trouver ce jar
			VirtualMachineAgentLoader.class.getClassLoader().loadClass(VIRTUAL_MACHINE_CLASS_NAME);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("La class " + VIRTUAL_MACHINE_CLASS_NAME + " est utilis�e pour ajout l'agent � la VM. Ajouter le tools.jar du jdk 1.6+ dans le classpath", e);
		}
		try {
					final com.sun.tools.attach.VirtualMachine vm = com.sun.tools.attach.VirtualMachine.attach(pid);
						vm.loadAgent(agentPath, option);
						vm.detach();
		} catch (final Exception e) {
			throw new KRuntimeException(e);
		}
	}
}
