/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiére - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.analytica.mock;

import io.analytica.api.AProcess;
import io.analytica.api.KProcessJsonCodec;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * ServerManager de test.
 * @author npiedeloup
 * @version $Id: RestNetApiPlugin.java,v 1.3 2012/10/16 12:39:27 npiedeloup Exp $
 */
public final class MockServerManager {
	private static final Logger LOG = Logger.getLogger(MockServerManager.class);
	private final Map<String, List<AProcess>> processesMap = new HashMap<String, List<AProcess>>();
	private static WeakReference<MockServerManager> INSTANCE = new WeakReference<MockServerManager>(null);

	/**
	 * Constructeur simple pour instanciation par TU.
	 */
	public MockServerManager() {
		INSTANCE = new WeakReference<MockServerManager>(this);
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
	public synchronized void push(final String json) {
		final List<AProcess> processes = KProcessJsonCodec.fromJson(json);
		LOG.info("PUSH " + processes.size() + " processes.");
		//LOG.info("-> " + json);
		for (final AProcess process : processes) {
			pushProcess(process);
		}

	}

	private void pushProcess(final AProcess process) {
		obtainProcesses(process.getType()).add(process);
		for (final AProcess subProcess : process.getSubProcesses()) {
			pushProcess(subProcess);
		}
	}

	private List<AProcess> obtainProcesses(final String type) {
		List<AProcess> processes = processesMap.get(type);
		if (processes == null) {
			processes = new ArrayList<AProcess>();
			processesMap.put(type, processes);
		}
		return processes;
	}

	/**
	 * @param type Type
	 * @return Liste des processes de ce type
	 */
	public synchronized List<AProcess> readProcesses(final String type) {
		return obtainProcesses(type);
	}

	/**
	 * @param metricName Nom de la metric
	 * @param type Type du process
	 * @param subTypes Sous type du process
	 * @return Liste des measures
	 */
	public synchronized List<Double> getMeasures(final String metricName, final String type, final String... subTypes) {
		final List<AProcess> processByType = obtainProcesses(type);
		final List<Double> measures = new ArrayList<Double>();
		for (final AProcess process : processByType) {
			if (process.getCategory().startsWith(toTree(subTypes))) {
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
