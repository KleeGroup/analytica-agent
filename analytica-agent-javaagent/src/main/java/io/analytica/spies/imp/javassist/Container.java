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
 * @author npiedeloup
 * @version $Id: MemoryLeakAgent.java,v 1.2 2012/09/28 09:30:03 pchretien Exp $
 */
public final class Container {

	private static KProcessCollector PROCESS_COLLECTOR_INSTANCE;
	private static KProcessConnector PROCESS_CONNECTOR_INSTANCE;

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
