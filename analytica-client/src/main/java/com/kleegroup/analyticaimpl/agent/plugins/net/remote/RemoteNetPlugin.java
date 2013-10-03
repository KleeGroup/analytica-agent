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
package com.kleegroup.analyticaimpl.agent.plugins.net.remote;

import javax.inject.Inject;
import javax.inject.Named;

import vertigo.kernel.lang.Activeable;

import com.kleegroup.analytica.core.KProcess;
import com.kleegroup.analyticaimpl.agent.net.RemoteConnector;
import com.kleegroup.analyticaimpl.agent.plugins.net.NetPlugin;

/**
 * @author npiedeloup
 * @version $Id: RemoteNetPlugin.java,v 1.4 2012/06/14 13:49:17 npiedeloup Exp $
 */
public final class RemoteNetPlugin implements NetPlugin, Activeable {
	private final RemoteConnector remoteConnector;

	/**
	 * @param serverUrl Url du serveur Analytica
	 * @param sendPaquetSize Taille des paquets déclenchant l'envoi anticipé
	 * @param sendPaquetFrequencySeconds Frequence normal d'envoi des paquets (en seconde)
	 */
	@Inject
	public RemoteNetPlugin(@Named("serverUrl") final String serverUrl, @Named("sendPaquetSize") final int sendPaquetSize, @Named("sendPaquetFrequencySeconds") final int sendPaquetFrequencySeconds) {
		remoteConnector = new RemoteConnector(serverUrl, sendPaquetSize, sendPaquetFrequencySeconds);
	}

	/** {@inheritDoc} */
	public void add(final KProcess process) {
		remoteConnector.add(process);
	}

	/** {@inheritDoc} */
	public void start() {
		remoteConnector.start();
	}

	/** {@inheritDoc} */
	public void stop() {
		remoteConnector.stop();
	}
}
