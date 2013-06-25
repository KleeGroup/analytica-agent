package com.kleegroup.analyticaimpl.spies.javassist;

import java.util.List;

/**
 * Configuration du JavassistSpy, qui définie les points d'encrage et les spécificités du branchement de l'agent.
 * @author npiedeloup
 * @version $Id: MemoryLeakTransformer.java,v 1.1 2011/05/12 10:16:05 prahmoune Exp $
 */
public final class AnalyticaSpyConf {

	private final List<String> fastExcludedPackages;
	private final List<String> fastIncludedPackages;
	private final List<AnalyticaSpyHookPoint> hookPoints;

	/**
	 * Constructeur.
	 * @param excludedPackages Liste de package à exclure
	 * @param includedPackages Liste de package à inclure
	 * @param hookPoints Liste de point d'accroche de l'agent
	 */
	public AnalyticaSpyConf(final List<String> excludedPackages, final List<String> includedPackages, final List<AnalyticaSpyHookPoint> hookPoints) {
		fastExcludedPackages = excludedPackages;
		fastIncludedPackages = includedPackages;
		this.hookPoints = hookPoints;
	}

	/**
	 * @return Pattern de package exclus, si vide pas d'exclusion
	 */
	public List<String> getFastExcludedPackages() {
		return fastExcludedPackages;
	}

	/**
	 * @return Pattern de package inclus, tout est inclus
	 */
	public List<String> getFastIncludedPackages() {
		return fastIncludedPackages;
	}

	/**
	 * @return Patterns de lecture du log
	 */
	public List<AnalyticaSpyHookPoint> getHookPoints() {
		return hookPoints;
	}
}
