package com.kleegroup.analyticamock;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.kleegroup.analytica.core.KProcess;

/**
 * ServerManager de test.
 * @author npiedeloup
 * @version $Id: RestNetApiPlugin.java,v 1.3 2012/10/16 12:39:27 npiedeloup Exp $
 */
public final class MockServerManager {
	private static final Logger LOG = Logger.getLogger(MockServerManager.class);
	private final Map<String, List<KProcess>> processesMap = new HashMap<String, List<KProcess>>();
	private static WeakReference<MockServerManager> INSTANCE = new WeakReference<MockServerManager>(null);

	/**
	 * Constructeur simple pour instanciation par TU.
	 */
	public MockServerManager() {
		INSTANCE = new WeakReference<MockServerManager>(this); //WeakRef pour etre libéré automatiquement
	}

	/**
	 * @return Instance
	 */
	public static MockServerManager getInstance() {
		final MockServerManager instance = INSTANCE.get();
		if (instance == null) {
			throw new NullPointerException("MockServerManager wasn't initialized or keep by tests");
		}
		return instance;
	}

	/**
	 * @param json json du process recu
	 */
	public void push(final String json) {
		final KProcess[] processes = new Gson().fromJson(json, KProcess[].class);
		LOG.info("PUSH " + processes.length + " processes.");
		//LOG.info("-> " + json);
		for (final KProcess process : processes) {
			pushProcess(process);
		}

	}

	private void pushProcess(final KProcess process) {
		obtainProcesses(process.getType()).add(process);
		for (final KProcess subProcess : process.getSubProcesses()) {
			pushProcess(subProcess);
		}
	}

	private List<KProcess> obtainProcesses(final String type) {
		List<KProcess> processes = processesMap.get(type);
		if (processes == null) {
			processes = new ArrayList<KProcess>();
			processesMap.put(type, processes);
		}
		return processes;
	}

	/**
	 * @param type Type
	 * @return Liste des processes de ce type
	 */
	public List<KProcess> readProcesses(final String type) {
		return obtainProcesses(type);
	}

	/**
	 * @param metricName Nom de la metric
	 * @param type Type du process
	 * @param subTypes Sous type du process
	 * @return Liste des measures
	 */
	public List<Double> getMeasures(final String metricName, final String type, final String... subTypes) {
		final List<KProcess> processByType = obtainProcesses(type);
		final List<Double> measures = new ArrayList<Double>();
		for (final KProcess process : processByType) {
			if (toTree(process.getSubTypes()).startsWith(toTree(subTypes))) {
				final Double measure = process.getMeasures().get(metricName);
				if (measure != null) {
					measures.add(measure);
				}
			}
		}
		return measures;
	}

	private String toTree(final String[] subTypes) {
		final StringBuilder sb = new StringBuilder();
		for (final String subType : subTypes) {
			sb.append("/");
			sb.append(subType);
		}
		return sb.toString();
	}
}
