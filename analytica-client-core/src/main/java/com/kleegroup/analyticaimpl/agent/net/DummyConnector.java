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
package com.kleegroup.analyticaimpl.agent.net;

import com.kleegroup.analytica.core.KProcess;

/**
 * @author npiedeloup
 * @version $Id: RemoteNetPlugin.java,v 1.4 2012/06/14 13:49:17 npiedeloup Exp $
 */
public final class DummyConnector implements KProcessConnector {
	/** {@inheritDoc} */
	public void add(final KProcess process) {
		//rien
	}

	/** {@inheritDoc} */
	public void start() {
		//rien
	}

	/** {@inheritDoc} */
	public void stop() {
		//rien
	}
}
