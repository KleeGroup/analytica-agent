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
package io.analytica.agent.remote;

import io.analytica.agent.AbstractAgentManagerTest;

import java.io.IOException;

/**
 * Cas de Test JUNIT de l'API Analytics.
 * Dans le cas ou le serveur n'est pas toujours joignable.
 * 
 * @author pchretien, npiedeloup
 * @version $Id: AgentManagerOutOfSyncTest.java,v 1.2 2012/06/14 13:52:26 npiedeloup Exp $
 */
public final class AgentManagerOutOfSyncTest extends AbstractAgentManagerTest {
	/**
	 * Initialisation du test pour implé spécifique.
	 * @throws Exception Erreur
	 */
	@Override
	protected void doSetUp() {
		//on ne démarre pas le serveur
	}

	/** {@inheritDoc} */
	@Override
	protected void flushAgentToServer() {
		try {
			Thread.sleep(5000);//on attend 5s que le process soit conservé coté client.
			try {
				startServer();
			} catch (final IOException e1) {
				throw new RuntimeException("Impossible de lancer le server jersey");
			}
			Thread.sleep(2000);//on attend 2s que le process soit envoyé au serveur.
		} catch (final InterruptedException e) {
			//rien on stop juste l'attente
		}
	}
}
