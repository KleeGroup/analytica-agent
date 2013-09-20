package com.kleegroup.analyticaimpl.spies.javassist;

import java.util.Map;

import com.kleegroup.analyticaimpl.agent.KProcessCollector;
import com.kleegroup.analyticaimpl.agent.net.DummyConnector;
import com.kleegroup.analyticaimpl.agent.net.FileLogConnector;
import com.kleegroup.analyticaimpl.agent.net.KProcessConnector;
import com.kleegroup.analyticaimpl.agent.net.RemoteConnector;

/**
 * 
 *
 * @author npiedeloup
 * @version $Id: MemoryLeakAgent.java,v 1.2 2012/09/28 09:30:03 pchretien Exp $
 */
public final class Container {

	private static KProcessCollector PROCESS_COLLECTOR_INSTANCE;
	private static KProcessConnector PROCESS_CONNECTOR_INSTANCE;

	public static void initCollector(final AnalyticaSpyConf analyticaSpyConf) {
		final String pluginName = analyticaSpyConf.getPluginName();
		final Map<String, String> pluginParams = analyticaSpyConf.getPluginParams();
		final KProcessConnector processConnector;
		if ("FileLog".equals(pluginName)) {
			processConnector = new FileLogConnector(pluginParams.get("fileName"));
		} else if ("Remote".equals(pluginName)) {
			processConnector = new RemoteConnector(pluginParams.get("serverUrl"), Integer.parseInt(pluginParams.get("sendPaquetSize")), Integer.parseInt(pluginParams.get("sendPaquetFrequencySeconds")));
		} else {
			processConnector = new DummyConnector();
		}

		PROCESS_COLLECTOR_INSTANCE = new KProcessCollector(processConnector);
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
			throw new NullPointerException("Le ProcessCollector n'a pas été configurer. Choisir le KProcessConnector : FileLog ou Remote");
		}
		return PROCESS_COLLECTOR_INSTANCE;
	}
}
