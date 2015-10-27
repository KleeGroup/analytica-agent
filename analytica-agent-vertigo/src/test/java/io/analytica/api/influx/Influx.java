package io.analytica.api.influx;

import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.junit.Test;

public class Influx {

	@Test
	public void test() {
		final InfluxDB influxDB = InfluxDBFactory.connect("http://kasper-redis:8086", "scott", "tiger");
		final String dbName = "aTimeSeries";
		influxDB.createDatabase(dbName);

		final BatchPoints batchPoints = BatchPoints
				.database(dbName)
				.tag("async", "true")
				.retentionPolicy("default")
				.consistency(ConsistencyLevel.ALL)
				.build();
		final Point point1 = Point.measurement("cpu")
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.field("idle", Long.valueOf(90L))
				.field("system", Long.valueOf(9L))
				.field("system", Long.valueOf(1L))
				.build();
		final Point point2 = Point.measurement("disk")
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.field("used", Long.valueOf(80L))
				.field("free", Long.valueOf(1L))
				.build();
		batchPoints.point(point1);
		batchPoints.point(point2);
		influxDB.write(batchPoints);
		final Query query = new Query("SELECT idle FROM cpu", dbName);
		final QueryResult queryResult = influxDB.query(query);
		System.out.println(queryResult);
		influxDB.deleteDatabase(dbName);
	}

}
