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
package com.kleegroup.analyticaimpl.agent.plugins.net;

import io.vertigo.kernel.component.Plugin;

import com.kleegroup.analytica.core.KProcess;
import com.kleegroup.analyticaimpl.agent.net.KProcessConnector;

/**
 * Producteur des messages.
 * Les messages sont compos�s des Processus et envoy�s ; un consommateur les traitera.
 * 
 * @author pchretien, npiedeloup
 * @version $Id: NetPlugin.java,v 1.1 2012/03/22 18:20:57 pchretien Exp $
 */
public interface NetPlugin extends Plugin, KProcessConnector {
	/**
	 * Ajout d'un process dans le syst�me.
	 * Cet ajout peut-�tre multi-thread�.
	 * @param process Process � ajouter
	 */
	void add(KProcess process);
}
