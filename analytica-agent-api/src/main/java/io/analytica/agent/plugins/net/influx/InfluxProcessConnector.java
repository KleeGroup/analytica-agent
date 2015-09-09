package io.analytica.agent.plugins.net.influx;

import io.analytica.agent.impl.NetPlugin;
import io.analytica.api.KProcess;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

public final class InfluxProcessConnector implements NetPlugin {
	private Boolean connected = null;
	private InfluxDB influxDB;

	private static boolean ping(final String host) {
		try {
			final InetAddress inet = InetAddress.getByName(host);
			return inet.getAddress() != null;
		} catch (final IOException e) {
			return false;
		}
	}

	private synchronized boolean isConnected(final String appName) {
		if (connected == null) {
			if (ping("kasper-redis")) {
				influxDB = InfluxDBFactory.connect("http://kasper-redis:8086", "scott", "tiger");
				influxDB.createDatabase(appName);
				influxDB.enableBatch(2000, 5000, TimeUnit.MILLISECONDS);
				connected = true;
			} else {
				connected = false;
			}
		}
		return connected;
	}

	@Override
	public void add(final KProcess process) {
		if (isConnected(process.getAppName())) {
			final BatchPoints batchPoints = BatchPoints
					.database(process.getAppName())
					.retentionPolicy("default")
					.consistency(ConsistencyLevel.ALL)
					.build();
			batchPoints.point(processToPoint(process));
			for (final KProcess subProcess : process.getSubProcesses()) {
				batchPoints.point(processToPoint(subProcess));
			}
			influxDB.write(batchPoints);
		}
	}

	private static Point processToPoint(final KProcess process) {
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
