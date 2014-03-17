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
package io.analytica.spies.imp.javassist.agentloader;

import java.lang.management.ManagementFactory;

import org.apache.log4j.Logger;

/**
 * Class utilitaire de chargement d'un agent de la VM, après son démarrage.
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
	 * Charge un agent à chaud.
	 * @param agentPath Chemin vers le jar de l'agent
	 * @param option option de l'agent
	 */
	public static void loadAgent(final String agentPath, final String option) {
		LOG.info("dynamically loading javaagent: " + agentPath + "=" + option);
		final String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
		final int p = nameOfRunningVM.indexOf('@');
		final String pid = nameOfRunningVM.substring(0, p);
		try {
			//on check, pour indiquer où trouver ce jar
			VirtualMachineAgentLoader.class.getClassLoader().loadClass(VIRTUAL_MACHINE_CLASS_NAME);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("La class " + VIRTUAL_MACHINE_CLASS_NAME + " est utilisée pour ajout l'agent à la VM. Ajouter le tools.jar du jdk 1.6+ dans le classpath", e);
		}
		try {
			final com.sun.tools.attach.VirtualMachine vm = com.sun.tools.attach.VirtualMachine.attach(pid);
			vm.loadAgent(agentPath, option);
			vm.detach();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
