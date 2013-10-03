/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidi�re - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package com.kleegroup.analyticaimpl.agent;

import java.util.Stack;

import com.kleegroup.analytica.core.KProcess;
import com.kleegroup.analytica.core.KProcessBuilder;
import com.kleegroup.analyticaimpl.agent.net.KProcessConnector;

/**
 * Agent de collecte des donn�es.
 * Collecte automatique des Process (les infos sont port�es par le thread courant).
 * 
 * @author pchretien, npiedeloup
 * @version $Id: AgentManagerImpl.java,v 1.7 2012/03/29 08:48:19 npiedeloup Exp $
 */
public final class KProcessCollector {
	private final KProcessConnector processConnector;

	/**
	 * Constructeur.
	 * @param processConnector Connecteur de communication
	 */
	public KProcessCollector(final KProcessConnector processConnector) {
		if (processConnector == null) {
			throw new NullPointerException("processConnector is required");
		}
		//-----------------------------------------------------------------
		this.processConnector = processConnector;
	}

	/**
	 * Processus bind� sur le thread courant. Le processus , recoit les notifications des sondes plac�es dans le code de
	 * l'application pendant le traitement d'une requ�te (thread).
	 */
	private static final ThreadLocal<Stack<KProcessBuilder>> THREAD_LOCAL_PROCESS = new ThreadLocal<Stack<KProcessBuilder>>();

	/**
	 * Retourne le premier �l�ment de la pile (sans le retirer).
	 * @return Premier �l�ment de la pile
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
	 * Enregistre dans le thread courant le d�marrage d'un process.
	 * Doit respecter les r�gles sur le nom d'un process.
	 * @param type Type de process
	 * @param names Nom du process
	 */
	public void startProcess(final String type, final String... names) {
		final KProcessBuilder processBuilder = new KProcessBuilder(type, names);
		push(processBuilder);
	}

	/**
	 * Incr�mentation d'une mesure du process courant (set si pas pr�sente).
	 * @param measureType Nom de la mesure
	 * @param value Valeur
	 */
	public void incMeasure(final String measureType, final double value) {
		peek().incMeasure(measureType, value);
	}

	/**
	 * Annule et remplace une mesure du process courant.
	 * @param measureType Nom de la mesure
	 * @param value Valeur
	 */
	public void setMeasure(final String measureType, final double value) {
		peek().setMeasure(measureType, value);
	}

	/**
	 * Ajoute une m�ta-donn�e du process courant (set si pas pr�sente).
	 * TODO V0+ : voir si mutlivalu�e int�ressante.
	 * @param metaDataName Nom de la m�ta donn�e
	 * @param value Valeur
	 */
	public void addMetaData(final String metaDataName, final String value) {
		peek().setMetaData(metaDataName, value);
	}

	/**
	* Termine le process courant.
	* Le processus courant devient alors le processus parent le cas �ch�ant.
	* @return Processus uniquement dans le cas ou c'est le processus parent.
	*/
	private KProcess doStopProcess() {
		final KProcess process = pop().build();
		if (getStack().isEmpty()) {
			//On est au processus racine on le collecte
			THREAD_LOCAL_PROCESS.remove(); //Et on le retire du ThreadLocal
			return process;
		}
		peek().addSubProcess(process);
		//On n'est pas dans le cas de la racine : selon le contrat on renvoie null
		return null;
	}

	/**
	 * Termine le process courant.
	 * Le processus courant devient alors le processus parent le cas �ch�ant.
	 */
	public void stopProcess() {
		final KProcess process = doStopProcess();
		if (process != null) {
			processConnector.add(process);
		}
	}

	/**
	 * Ajout d'un process d�j� assembl� par une sonde.
	 * Cet ajout peut-�tre multi-thread�.
	 * @param process Process � ajouter
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
