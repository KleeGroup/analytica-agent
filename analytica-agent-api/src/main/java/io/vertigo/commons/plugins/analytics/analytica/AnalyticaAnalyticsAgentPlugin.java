package io.vertigo.commons.plugins.analytics.analytica;

import io.analytica.agent.plugins.net.influx.InfluxProcessConnector;
import io.analytica.api.KProcessCollector;
import io.vertigo.commons.impl.analytics.AnalyticsAgentPlugin;
import io.vertigo.lang.Assertion;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.inject.Named;

public final class AnalyticaAnalyticsAgentPlugin implements AnalyticsAgentPlugin {
	private final KProcessCollector processCollector;

	/**
	 * Constructeur.
	 * @param appName 
	 * @param location System location (Environment, Server, Jvm, ..)
	 */
	@Inject
	public AnalyticaAnalyticsAgentPlugin(@Named("appName") final String appName /*, @Named("location") final String location*/) {
		Assertion.checkArgNotEmpty(appName);
		//-----------------------------------------------------------------
		String location;
		try {
			location = InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			location = "unknown";
		}

		processCollector = new KProcessCollector(appName, location, new InfluxProcessConnector(appName));
	}

	/** {@inheritDoc} */
	@Override
	public void startProcess(final String type, final String category) {
		processCollector.startProcess(type, category);
	}

	/** {@inheritDoc} */
	@Override
	public void incMeasure(final String measureType, final double value) {
		processCollector.incMeasure(measureType, value);
	}

	/** {@inheritDoc} */
	@Override
	public void setMeasure(final String measureType, final double value) {
		processCollector.setMeasure(measureType, value);
	}

	/** {@inheritDoc} */
	@Override
	public void addMetaData(final String metaDataName, final String value) {
		processCollector.addMetaData(metaDataName, value);
	}

	@Override
	public void stopProcess() {
		processCollector.stopProcess();
	}

}
