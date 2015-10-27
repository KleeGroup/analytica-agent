package io.analytica.api.influx;

import io.analytica.agent.api.KProcessCollector;
import io.analytica.agent.impl.KProcessCollectorContainer;

import org.junit.Test;

public class InfluxTest {

	@Test
	public void testCollector() {
		final KProcessCollector agent = KProcessCollectorContainer.getProcessCollector();

		for (int j = 0; j < 1; j++) {
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
			agent.startProcess("pages", category);
			agent.incMeasure("weight", 100 + j % 2 * 100 + Math.sin(j * Math.PI / 100) * Double.valueOf(Math.random() * 100).intValue());
			try {
				Thread.sleep(500);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			agent.addMetaData("keyConcept", "person");
			agent.stopProcess();
		}
		//		final Query query = new Query("select * from pages", appName);
		//
		//		final QueryResult queryResult = influxDB.query(query);
		//		Assert.assertEquals(1, queryResult.getResults().size());
		//		final Result result = queryResult.getResults().get(0);
		//		if (result.getError() != null) {
		//			throw new RuntimeException(result.getError());
		//		}
		//		Assert.assertEquals(1, result.getSeries().size());
		//		//		influxDB.deleteDatabase(dbName);
	}
}
