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
package io.analytica.agent.impl;

import io.analytica.agent.AgentManager;
import io.analytica.api.KProcess;
import io.analytica.api.KProcessCollector;
import io.vertigo.lang.Assertion;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Agent de collecte des données.
 * Collecte automatique des Process (les infos sont portées par le thread courant).
 * 
 * @author pchretien, npiedeloup
 * @version $Id: AgentManagerImpl.java,v 1.7 2012/03/29 08:48:19 npiedeloup Exp $
 */
public final class AgentManagerImpl implements AgentManager {
	private final KProcessCollector processCollector;

	/**
	 * Constructeur.
	 * @param systemName System name
	 * @param systemLocation System location (Environment, Server, Jvm, ..)
	 * @param netPlugin Plugin de communication
	 */
	@Inject
	public AgentManagerImpl(@Named("appName") final String appName, /*@Named("location") final String location,*/final NetPlugin netPlugin) {
		super();
		Assertion.checkNotNull(netPlugin);
		//-----------------------------------------------------------------
		String location;
		try {
			location = InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			location = "unknown";
		}

		processCollector = new KProcessCollector(appName, location, netPlugin);
	}

	/** {@inheritDoc} */
	@Override
	public void startProcess(final String type, final String category) {
		processCollector.startProcess(type, category);
	}

	/** {@inheritDoc} */
	@Override
	public void incMeasure(final String measureType, final double value) {
		processCollector.incMeasure(measureType, value);
	}

	/** {@inheritDoc} */
	@Override
	public void setMeasure(final String measureType, final double value) {
		processCollector.setMeasure(measureType, value);
	}

	/** {@inheritDoc} */
	@Override
	public void addMetaData(final String metaDataName, final String value) {
		processCollector.addMetaData(metaDataName, value);
	}

	/** {@inheritDoc} */
	@Override
	public void stopProcess() {
		processCollector.stopProcess();
	}

	/** {@inheritDoc} */
	@Override
	public void add(final KProcess process) {
		processCollector.add(process);
	}
}
