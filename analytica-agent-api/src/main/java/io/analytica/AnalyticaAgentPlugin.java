package io.analytica;

import io.analytica.agent.AgentManager;
import io.vertigo.commons.impl.analytics.AnalyticsAgentPlugin;
import io.vertigo.lang.Assertion;

import javax.inject.Inject;

/**
 * Implémentation de l'agent de collecte avec redirection vers Analytica.
 * @author pchretien, npiedeloup
 * @version $Id: AnalyticaAgentPlugin.java,v 1.6 2012/05/10 09:38:14 npiedeloup Exp $
 */
public final class AnalyticaAgentPlugin implements AnalyticsAgentPlugin {
	private final AgentManager analyticaManager;

	/**
	 * Constructeur.
	 * @param analyticaManager Manager Analytica
	 */
	@Inject
	public AnalyticaAgentPlugin(final AgentManager analyticaManager) {
		Assertion.checkNotNull(analyticaManager);
		//---------------------------------------------------------------------
		this.analyticaManager = analyticaManager;
	}

	/** {@inheritDoc} */
	@Override
	public void startProcess(final String processType, final String category) {
		analyticaManager.startProcess(processType, category);
	}

	/** {@inheritDoc} */
	@Override
	public void incMeasure(final String measureType, final double value) {
		analyticaManager.incMeasure(measureType, value);
	}

	/** {@inheritDoc} */
	@Override
	public void setMeasure(final String measureType, final double value) {
		analyticaManager.setMeasure(measureType, value);
	}

	/** {@inheritDoc} */
	@Override
	public void addMetaData(final String metaDataName, final String value) {
		analyticaManager.addMetaData(metaDataName, value);
	}

	/** {@inheritDoc} */
	@Override
	public void stopProcess() {
		analyticaManager.stopProcess();
	}
}
