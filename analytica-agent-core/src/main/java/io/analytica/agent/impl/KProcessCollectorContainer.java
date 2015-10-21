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

public final class KProcessCollectorContainer {

	private static KProcessCollector INSTANCE = obtainProcessCollector();

	public static KProcessCollector getInstance() {
		return INSTANCE;
	}

	private static KProcessCollector obtainProcessCollector() {
		Context initCtx;
		try {
			initCtx = new InitialContext();
			final Context context = (Context) initCtx.lookup("java:comp/env");
			final String appName = (String) context.lookup("analytica.appName");
			final String location = (String) context.lookup("analytica.location");
			final String connectorType = (String) context.lookup("analytica.connector.type");

			Assertion.checkNotNull(appName, "appName is required");
			Assertion.checkNotNull(location, "location is required");
			Assertion.checkNotNull(connectorType, "connectorType is required");
			KProcessConnector processConnector;
			if ("file".equals(connectorType)) {
				final String fileName = (String) context.lookup("analytica.connector.file.fileName");
				Assertion.checkNotNull(fileName, "fileName is required");
				processConnector = new FileLogConnector(fileName);
			} else if ("remote".equals(connectorType)) {
				final String serverUrl = (String) context.lookup("analytica.connector.remote.serverUrl");
				final Integer sendPaquetSize = (Integer) context.lookup("analytica.connector.remote.sendPaquetSize");
				final Integer sendPaquetFrequencySeconds = (Integer) context.lookup("analytica.connector.remote.sendPaquetFrequencySeconds");
				Assertion.checkNotNull(serverUrl, "serverUrl is required");
				Assertion.checkNotNull(sendPaquetSize, "sendPaquetSize is required");
				Assertion.checkNotNull(sendPaquetFrequencySeconds, "sendPaquetFrequencySeconds is required");
				processConnector = new RemoteConnector(serverUrl, sendPaquetSize, sendPaquetFrequencySeconds);
			} else {
				System.err.println("Unknown Connector : " + connectorType + " fallback to DummyCollector (use one of : FileLog, RemoteHTTP, Dummy)");
				processConnector = new DummyConnector();
			}
			return new KProcessCollector(appName, location, processConnector);
		} catch (final NamingException e) {
			throw new RuntimeException("Unable to initialise the analytica collector", e);
		}
	}

}
