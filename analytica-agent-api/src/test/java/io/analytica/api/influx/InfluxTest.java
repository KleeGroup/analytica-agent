package io.analytica.api.influx;

import io.analytica.api.KProcessCollector;
import io.analytica.api.KProcessConnector;

import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.junit.Assert;
import org.junit.Test;

public class InfluxTest {

	@Test
	public void testCollector() {
		final String appName = "alpha";
		final String location = "mexico";
		final InfluxDB influxDB = InfluxDBFactory.connect("http://kasper-redis:8086", "scott", "tiger");
		influxDB.createDatabase(appName);
		influxDB.enableBatch(2000, 5000, TimeUnit.MILLISECONDS);

		final KProcessConnector influxProcessConnector = new InfluxProcessConnector(influxDB);
		final KProcessCollector processCollector = new KProcessCollector(appName, "mexico", influxProcessConnector);

		for (int j = 0; j < 100000; j++) {
			//			final long delta = Double.valueOf(Math.random() * 3600 * 24 * 1000).longValue();
			//			final KProcess process = new KProcessBuilder(appName, "pages", new Date(new Date().getTime() - delta), Math.random() * 50)
			//					.withLocation("paris")
			//					.withCategory("config")
			//					.incMeasure("weight", 12 + Double.valueOf(Math.random() * 100).intValue())
			//					.addMetaData("keyConcept", "person")
			//					.build();
			//			processes.add(process);
			//			influxProcessConnector.add(processes);
			//		}

			final String category = j % 2 == 0 ? "welcome" : "search";
			processCollector
					.startProcess("pages", category)
					.incMeasure("weight", 100 + j % 2 * 100 + Math.sin(j * Math.PI / 100) * Double.valueOf(Math.random() * 100).intValue())
					.sleep(500)
					.addMetaData("keyConcept", "person")
					.stopProcess();
			//		influxDB.disableBatch();
		}
		final Query query = new Query("select * from pages", appName);

		final QueryResult queryResult = influxDB.query(query);
		Assert.assertEquals(1, queryResult.getResults().size());
		final Result result = queryResult.getResults().get(0);
		if (result.getError() != null) {
			throw new RuntimeException(result.getError());
		}
		Assert.assertEquals(1, result.getSeries().size());
		//		influxDB.deleteDatabase(dbName);
	}
}
