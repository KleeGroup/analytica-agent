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
package com.kleegroup.analyticaimpl.agent.plugins;

import java.util.Stack;

import kasper.kernel.util.Assertion;

import com.kleegroup.analytica.core.KProcess;
import com.kleegroup.analytica.core.KProcessBuilder;
import com.kleegroup.analyticaimpl.agent.AgentPlugin;

/**
 * Plugin de construction des process.
 * @author npiedeloup
 * @version $Id: AgentPluginImpl.java,v 1.7 2012/03/22 18:26:21 pchretien Exp $
 */
public final class AgentPluginImpl implements AgentPlugin {
	/**
	 * Processus bindé sur le thread courant. Le processus , recoit les notifications des sondes placées dans le code de
	 * l'application pendant le traitement d'une requête (thread).
	 */
	private static final ThreadLocal<Stack<KProcessBuilder>> THREAD_LOCAL_PROCESS = new ThreadLocal<Stack<KProcessBuilder>>();

	/**
	 * Retourne le premier élément de la pile (sans le retirer).
	 * @return Premier élément de la pile
	 */
	private static KProcessBuilder peek() {
		return getStack().peek();
	}

	/**
	 * Retire le premier élément de la pile.
	 * @return Premier élément de la pile
	 */
	private static KProcessBuilder pop() {
		return getStack().pop();
	}

	private static Stack<KProcessBuilder> getStack() {
		final Stack<KProcessBuilder> stack = THREAD_LOCAL_PROCESS.get();
		if (stack == null) {
			throw new IllegalArgumentException("Pile non initialisée : startProcess()");
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
		Assertion.invariant(stack.size() < 100, "La pile des KProcess atteind une profondeur de 100, il est probable qu'une fermeture de KProcess ait été oubliée.\nStack:{0}", stack);
		//---------------------------------------------------------------------
		stack.push(processBuilder);
	}

	/** {@inheritDoc} */
	public void startProcess(final String type, final String name) {
		final KProcessBuilder processBuilder = new KProcessBuilder(type, name);
		push(processBuilder);
	}

	/** {@inheritDoc} */
	public void incMeasure(final String measureType, final double value) {
		peek().incMeasure(measureType, value);
	}

	/** {@inheritDoc} */
	public void setMeasure(final String measureType, final double value) {
		peek().setMeasure(measureType, value);
	}

	/** {@inheritDoc} */
	public void addMetaData(final String metaDataName, final String value) {
		peek().setMetaData(metaDataName, value);
	}

	/** {@inheritDoc} */
	public KProcess stopProcess() {
		final KProcess process = pop().build();
		if (getStack().isEmpty()) {
			//On est au processus racine on le collecte
			THREAD_LOCAL_PROCESS.remove(); //Et on le retire du ThreadLocal
			return process;
		}
		peek().addSubProcess(process);
		//On n'est pas dans le cas de la racine/selon le contrat on renvoie null
		return null;
	}
}
