package io.analytica.api.museum;

import io.analytica.agent.api.KProcessConnector;
import io.analytica.agent.impl.InfluxProcessConnector;
import io.analytica.api.KProcess;
import io.analytica.museum.Museum;
import io.analytica.museum.PageListener;

public class Snippet {

	public static void main(final String[] args) {
		final String appName = "museum";

		final KProcessConnector processConnector = new InfluxProcessConnector(appName);

		final Museum museum = new Museum(new PageListener() {

			public void onPage(final KProcess process) {
				processConnector.add(process);
			}
		});

		final int days = 10;
		final int visitsByDay = 10;
		museum.load(days, visitsByDay);
	}
}
