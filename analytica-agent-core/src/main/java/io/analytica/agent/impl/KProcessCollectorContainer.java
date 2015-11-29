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
package io.analytica.agent.impl;

import io.analytica.agent.api.KProcessCollector;
import io.analytica.agent.api.KProcessConnector;
import io.analytica.agent.impl.net.DummyConnector;
import io.analytica.agent.impl.net.FileLogConnector;
import io.analytica.agent.impl.net.RemoteConnector;
import io.analytica.api.Assertion;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author dslobozian
 * @version $Id: KProcessCollectorContainer.java,v 1.0 2015/10/27 13:49:17 dslobozian Exp $
 */
public final class KProcessCollectorContainer {


	private static KProcessCollector INSTANCE = createProcessCollector();

	public static KProcessCollector getInstance() {
		return INSTANCE;
	}

	private static KProcessCollector createProcessCollector() {
		String appName = (String) AnalyticaConfigurationStore.getInstace().getConfiguration(AnalyticaConfigurationType.ANALYTICA_APP_NAME);
		String location =(String) AnalyticaConfigurationStore.getInstace().getConfiguration(AnalyticaConfigurationType.ANALYTICA_LOCATION);
		String connectorType = (String) AnalyticaConfigurationStore.getInstace().getConfiguration(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_TYPE);
		
		if(		appName.equalsIgnoreCase(AnalyticaConfigurationStore.DEFAULT_APP_NAME)||
				location.equalsIgnoreCase(AnalyticaConfigurationStore.DEFAULT_LOCATION)||
				connectorType.equalsIgnoreCase(AnalyticaConfigurationStore.DEFAULT_CONNECTOR_TYPE))
		{
			System.err.println("Unable to locate Analytica's configuration. Fallback to the dummy implementation");
			return new KProcessCollector(appName, location, new DummyConnector());
		}
		if ("file".equals(connectorType)) {
			final String fileName = (String) AnalyticaConfigurationStore.getInstace().getConfiguration(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_FILE_FILE_NAME);
			Assertion.checkArgument(!fileName.equalsIgnoreCase(AnalyticaConfigurationStore.DEFAULT_CONNECTOR_FILE_FILE_NAME), "Analytica : " + AnalyticaConfigurationType.ANALYTICA_CONNECTOR_FILE_FILE_NAME + " is required for the file connector");
			return new KProcessCollector(appName, location, new FileLogConnector(fileName));
		}
		
		if ("remote".equals(connectorType)) {
			final String serverUrl = (String) AnalyticaConfigurationStore.getInstace().getConfiguration(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SERVER_URL);
			final Integer sendPaquetSize = (Integer) AnalyticaConfigurationStore.getInstace().getConfiguration(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SEND_PAQUET_SIZE);
			final Integer sendPaquetFrequencySeconds = (Integer) AnalyticaConfigurationStore.getInstace().getConfiguration(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SEND_PAQUET_FREQUENCY_SECONDS);
			Assertion.checkArgument(!serverUrl.equalsIgnoreCase(AnalyticaConfigurationStore.DEFAULT_CONNECTOR_FILE_FILE_NAME), "Analytica : " + AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SERVER_URL + " is required for the remote connector");
			Assertion.checkArgument(sendPaquetSize!=AnalyticaConfigurationStore.DEFAULT_CONNECTOR_REMOTE_SEND_PAQUET_SIZE, "Analytica : " + AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SEND_PAQUET_SIZE + " is required for the remote connector");
			Assertion.checkArgument(sendPaquetFrequencySeconds!=AnalyticaConfigurationStore.DEFAULT_CONNECTOR_REMOTE_SEND_PAQUET_FREQUENCY_SECONDS, "Analytica : " + AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SEND_PAQUET_FREQUENCY_SECONDS + " is required for the remote connector");
			return new KProcessCollector(appName, location,new RemoteConnector(serverUrl, sendPaquetSize, sendPaquetFrequencySeconds));
		} 

		System.err.println("Unknown connector. Fallback to the dummy implementation");
		return new KProcessCollector(appName, location, new DummyConnector());
	}
}
