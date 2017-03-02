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

import java.awt.Point;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;

import io.analytica.agent.api.KProcessConnector;
import io.analytica.api.AProcess;

public final class InfluxProcessConnector implements KProcessConnector {
	private final String appName;
	private final List<AProcess> processes = new ArrayList<>(); //buffer
	private final InfluxDB influxDB;

	private static boolean ping(final String host) {
		try {
			final InetAddress inet = InetAddress.getByName(host);
			return inet.getAddress() != null;
		} catch (final IOException e) {
			return false;
		}
	}

	public InfluxProcessConnector(final String appName) {
		//		Assertion.checkArgNotEmpty(appName);
		//-----
		this.appName = appName;
		if (ping("kasper-redis")) {
			influxDB = InfluxDBFactory.connect("http://kasper-redis:8086", "scott", "tiger");
			//influxDB.deleteDatabase(appName);
			influxDB.createDatabase(appName);
			influxDB.enableBatch(2000, 5000, TimeUnit.MILLISECONDS);
		} else {
			influxDB = null;
		}
	}

	@Override
	public synchronized void add(final AProcess process) {
		if (influxDB != null) {
			processes.add(process);
			if (processes.size() > 5000) {
				flush();
			}
		}
	}

	private void flush() {
		final BatchPoints batchPoints = BatchPoints
				.database(appName)
				.retentionPolicy("default")
				.consistency(ConsistencyLevel.ALL)
				.build();
		for (final AProcess process : processes) {
			batchPoints.point(processToPoint(process));
			for (final AProcess subProcess : process.getSubProcesses()) {
				batchPoints.point(processToPoint(subProcess));
			}
		}
		influxDB.write(batchPoints);
		processes.clear();
	}

	private static Point processToPoint(final AProcess process) {
		final Map measures = process.getMeasures();
		return Point.measurement(process.getType())
				.time(process.getStartDate().getTime(), TimeUnit.MILLISECONDS)
				.tag("category", process.getCategory())
				.tag("location", process.getLocation())
				.tag(process.getMetaDatas())
				.fields(measures)
				.build();
	}

}
