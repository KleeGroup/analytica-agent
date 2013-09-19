package com.kleegroup.analyticaimpl.spies.javassist;

import java.util.Map;

import com.kleegroup.analyticaimpl.agent.KProcessCollector;
import com.kleegroup.analyticaimpl.agent.plugins.net.NetPlugin;
import com.kleegroup.analyticaimpl.agent.plugins.net.filelog.FileLogNetPlugin;
import com.kleegroup.analyticaimpl.agent.plugins.net.remote.RemoteNetPlugin;

/**
 * 
 *
 * @author npiedeloup
 * @version $Id: MemoryLeakAgent.java,v 1.2 2012/09/28 09:30:03 pchretien Exp $
 */
public final class AnalyticaSpyAgentContainer {

	private static KProcessCollector PROCESS_COLLECTOR_INSTANCE;
	private static NetPlugin NET_PLUGIN_INSTANCE;

	public static void initNetPlugin(final AnalyticaSpyConf analyticaSpyConf) {
		final String pluginName = analyticaSpyConf.getPluginName();
		final Map<String, String> pluginParams = analyticaSpyConf.getPluginParams();
		if (FileLogNetPlugin.class.getSimpleName().equals(pluginName)) {
			NET_PLUGIN_INSTANCE = new FileLogNetPlugin(pluginParams.get("logName"));
			PROCESS_COLLECTOR_INSTANCE = new KProcessCollector(NET_PLUGIN_INSTANCE);
			NET_PLUGIN_INSTANCE.start();
		} else if (RemoteNetPlugin.class.getSimpleName().equals(pluginName)) {
			NET_PLUGIN_INSTANCE = new RemoteNetPlugin(pluginParams.get("serverUrl"), Integer.parseInt(pluginParams.get("sendPaquetSize")), Integer.parseInt(pluginParams.get("sendPaquetFrequencySeconds")));
			PROCESS_COLLECTOR_INSTANCE = new KProcessCollector(NET_PLUGIN_INSTANCE);
			NET_PLUGIN_INSTANCE.start();
		} else {
			NET_PLUGIN_INSTANCE = null;
			PROCESS_COLLECTOR_INSTANCE = null;
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (NET_PLUGIN_INSTANCE != null) {
					NET_PLUGIN_INSTANCE.stop();
				}
			}
		});
	}

	public static NetPlugin getNetPlugin() {
		if (NET_PLUGIN_INSTANCE == null) {
			throw new NullPointerException("Le NetPlugin n'a pas été configurer : FileLogNetPlugin ou RemoteNetPlugin");
		}
		return NET_PLUGIN_INSTANCE;
	}

	public static KProcessCollector getProcessCollector() {
		if (PROCESS_COLLECTOR_INSTANCE == null) {
			throw new NullPointerException("Le ProcessCollector n'a pas été configurer. Choisir le NetPlugin : FileLogNetPlugin ou RemoteNetPlugin");
		}
		return PROCESS_COLLECTOR_INSTANCE;
	}
}
