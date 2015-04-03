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
package io.analytica.agent.plugins.net.direct;

import io.analytica.agent.plugins.net.NetPlugin;
import io.analytica.api.KProcess;
import io.analytica.server.ServerManager;
import io.vertigo.lang.Assertion;

import javax.inject.Inject;

/**
 * Direct call to Analytica Server.
 * No buffering. Design for tests purpose.
 * @author npiedeloup
 * @version $Id: DirectNetPlugin.java,v 1.4 2012/10/16 08:30:56 pchretien Exp $
 */
public final class DirectNetPlugin implements NetPlugin {
	private final ServerManager serverManager;

	/**
	 * Constructor.
	 * @param serverManager Analytica ServerManager
	 */
	@Inject
	public DirectNetPlugin(final ServerManager serverManager) {
		Assertion.checkNotNull(serverManager);
		//---------------------------------------------------------------------
		this.serverManager = serverManager;

	}

	/** {@inheritDoc} */
	@Override
	public void add(final KProcess process) {
		serverManager.push(process);
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		//rien
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		//rien
	}
}
