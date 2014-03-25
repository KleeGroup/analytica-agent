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
package io.analytica.spies.imp.javassist;

import io.analytica.agent.impl.KProcessCollector;
import io.analytica.agent.impl.net.DummyConnector;
import io.analytica.agent.impl.net.FileLogConnector;
import io.analytica.agent.impl.net.KProcessConnector;
import io.analytica.agent.impl.net.RemoteConnector;

import java.util.Map;

/**
 * 
 *
 * @author npiedeloup (2012/09/28 09:30:03)
 */
public final class Container {

	private static KProcessCollector PROCESS_COLLECTOR_INSTANCE;
	/**
	 * KProcessConnector instance.
	 */
	static KProcessConnector PROCESS_CONNECTOR_INSTANCE;

	/**
	 * Initialize KProcessCollector and KProcessConnector.
	 * @param analyticaSpyConf Configuration file
	 */
	public static void initCollector(final AnalyticaSpyConf analyticaSpyConf) {
		final String collectorName = analyticaSpyConf.getCollectorName();
		final Map<String, String> collectorParams = analyticaSpyConf.getCollectorParams();
		final KProcessConnector processConnector;
		if ("FileLog".equals(collectorName)) {
			processConnector = new FileLogConnector(collectorParams.get("fileName"));
		} else if ("RemoteHTTP".equals(collectorName)) {
			processConnector = new RemoteConnector(collectorParams.get("serverUrl"), Integer.parseInt(collectorParams.get("sendPaquetSize")), Integer.parseInt(collectorParams.get("sendPaquetFrequencySeconds")));
		} else {
			System.err.println("Unknown Connector : " + collectorName + " fallback to DummyCollector (use one of : FileLog, RemoteHTTP, Dummy)");
			processConnector = new DummyConnector();
		}

		PROCESS_COLLECTOR_INSTANCE = new KProcessCollector(analyticaSpyConf.getSystemName(), analyticaSpyConf.getSystemLocation(), processConnector);
		processConnector.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (PROCESS_CONNECTOR_INSTANCE != null) {
					PROCESS_CONNECTOR_INSTANCE.stop();
				}
			}
		});
	}

	public static KProcessCollector getProcessCollector() {
		if (PROCESS_COLLECTOR_INSTANCE == null) {
			throw new NullPointerException("Le ProcessCollector n'a pas été configuré. Choisir le KProcessConnector : FileLog ou Remote");
		}
		return PROCESS_COLLECTOR_INSTANCE;
	}
}
