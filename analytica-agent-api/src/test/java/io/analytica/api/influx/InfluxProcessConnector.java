package io.analytica.api.influx;

import io.analytica.api.KProcess;
import io.analytica.api.KProcessConnector;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

public class InfluxProcessConnector implements KProcessConnector {

	private final InfluxDB influxDB;

	public InfluxProcessConnector(final InfluxDB influxDB) {
		this.influxDB = influxDB;
	}

	@Override
	public void add(final KProcess process) {

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
