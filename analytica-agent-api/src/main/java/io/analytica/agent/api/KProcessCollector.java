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
package io.analytica.agent.api;

import io.analytica.api.Assertion;
import io.analytica.api.KProcess;
import io.analytica.api.KProcessBuilder;

import java.util.Stack;

/**
 * Datas collector agent.
 * Collect and build Process.
 * Building process are bind to current thread.
 *
 * @author pchretien, npiedeloup
 * @version $Id: AgentManagerImpl.java,v 1.7 2012/03/29 08:48:19 npiedeloup Exp $
 */
public final class KProcessCollector {
	private final KProcessConnector processConnector;
	private final String appName;
	private final String location;

	/**
	 * Constructor.
	 * Should be created only once.
	 * @param processConnector Collector output connector
	 */

	public KProcessCollector(final String appName, final String location, final KProcessConnector processConnector) {
		Assertion.checkNotNull(appName, "appName is required");
		Assertion.checkNotNull(location, "location is required");
		Assertion.checkNotNull(processConnector, "processConnector is required");
		//-----------------------------------------------------------------
		this.location = location;
		this.appName = appName;
		this.processConnector = processConnector;
		this.processConnector.start();
	}

	public String getAppName() {
		return appName;
	}

	public String getLocation() {
		return location;
	}
	
	/**
	 * Processus binde sur le thread courant. Le processus , recoit les notifications des sondes placees dans le code de
	 * l'application pendant le traitement d'une requete (thread).
	 */
	private static final ThreadLocal<Stack<KProcessBuilder>> THREAD_LOCAL_PROCESS = new ThreadLocal<Stack<KProcessBuilder>>();

	/**
	 * Retourne le premier element de la pile (sans le retirer).
	 * @return Premier element de la pile
	 */
	private static KProcessBuilder peek() {
		return getStack().peek();
	}

	/**
	 * Retire le premier �l�ment de la pile.
	 * @return Premier �l�ment de la pile
	 */
	private static KProcessBuilder pop() {
		return getStack().pop();
	}

	private static Stack<KProcessBuilder> getStack() {
		final Stack<KProcessBuilder> stack = THREAD_LOCAL_PROCESS.get();
		if (stack == null) {
			throw new IllegalArgumentException("Pile non initialis�e : startProcess()");
		}
		return stack;
	}

	private static void push(final KProcessBuilder processBuilder) {
		Stack<KProcessBuilder> stack = THREAD_LOCAL_PROCESS.get();
		if (stack == null) {
			stack = new Stack<KProcessBuilder>();
			THREAD_LOCAL_PROCESS.set(stack);
		}
		//---------------------------------------------------------------------
		if (stack.size() >= 100) {
			throw new IllegalStateException("La pile des KProcess atteind une profondeur de 100, il est probable qu'une fermeture de KProcess ait �t� oubli�e.\nStack:" + stack);
		}
		//---------------------------------------------------------------------
		stack.push(processBuilder);
	}

	/**
	 * Enregistre dans le thread courant le demarrage d'un process.
	 * Doit respecter les regles sur le nom d'un process.
	 * @param type Type de process
	 */
	public KProcessCollector startProcess(final String type, final String category) {
		final KProcessBuilder processBuilder = new KProcessBuilder(appName, type)
		.withLocation(location)
		.withCategory(category);
		push(processBuilder);
		return this;
	}

	public KProcessCollector startProcess(final String type, final String... categories) {
		final KProcessBuilder processBuilder = new KProcessBuilder(appName, type)
		.withLocation(location)
		.withCategory(categories);
		push(processBuilder);
		return this;
	}

	/**
	 * Incr�mentation d'une mesure du process courant (set si pas pr�sente).
	 * @param measureType Nom de la mesure
	 * @param value Valeur
	 */
	public KProcessCollector incMeasure(final String measureType, final double value) {
		peek().incMeasure(measureType, value);
		return this;
	}

	/**
	 * Annule et remplace une mesure du process courant.
	 * @param measureType Nom de la mesure
	 * @param value Valeur
	 */
	public KProcessCollector setMeasure(final String measureType, final double value) {
		peek().setMeasure(measureType, value);
		return this;
	}

	/**
	 * Ajoute une meta-donnee du process courant (set si pas presente).
	 * TODO V0+ : voir si mutlivaluee interessante.
	 * @param metaDataName Nom de la meta donnee
	 * @param value Valeur
	 */
	public KProcessCollector addMetaData(final String metaDataName, final String value) {
		peek().addMetaData(metaDataName, value);
		return this;
	}

	/**
	 * Termine le process courant.
	 * Le processus courant devient alors le processus parent le cas �ch�ant.
	 */
	public KProcessCollector stopProcess() {
		final KProcess process = doStopProcess();
		if (process != null) {
			processConnector.add(process);
		}
		return this;
	}

	/**
	 * Termine le process courant.
	 * Le processus courant devient alors le processus parent le cas echeant.
	 * @return Processus uniquement dans le cas ou c'est le processus parent.
	 */
	private static KProcess doStopProcess() {
		final KProcess process = pop().build();
		if (getStack().isEmpty()) {
			//On est au processus racine on le collecte
			THREAD_LOCAL_PROCESS.remove(); //Et on le retire du ThreadLocal
			return process;
		}
		peek().addSubProcess(process);
		//On n'est pas dans le cas de la racine : conformement au contrat on renvoie null
		return null;
	}

	/**
	 * Ajout d'un process deja assemble par une sonde.
	 * Cet ajout peut-etre multi-thread.
	 * @param process Process a ajouter
	 */
	public void add(final KProcess process) {
		if (THREAD_LOCAL_PROCESS.get() != null) {
			throw new IllegalStateException("A process is already have started. You can't add a new full process tree at this time.");
		}
		if (process == null) {
			throw new NullPointerException("process is required");
		}
		//---------------------------------------------------------------------
		processConnector.add(process);
	}
}
