package com.kleegroup.analyticaimpl.spies.javassist;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

/**
 * Configuration du JavassistSpy, qui d�finie les points d'encrage et les sp�cificit�s du branchement de l'agent.
 * @author npiedeloup
 * @version $Id: MemoryLeakTransformer.java,v 1.1 2011/05/12 10:16:05 prahmoune Exp $
 */
public final class AnalyticaSpyConf {
	private final String collectorName;
	private final Map<String, String> collectorParams;
	private final List<String> fastExcludedPackages;
	private final List<String> fastIncludedPackages;
	private final List<AnalyticaSpyHookPoint> hookPoints;
	private final Map<String, String> localVariables;
	private final List<String> methodBefore;
	private final List<String> methodAfter;
	private final Map<String, List<String>> methodCatchs;
	private final List<String> methodFinally;

	/**
	 * Constructeur.
	 * @param collectorName Nom du plugin a utiliser
	 * @param collectorParams Param�tres du plugin
	 * @param excludedPackages Liste de package � exclure
	 * @param includedPackages Liste de package � inclure
	 * @param hookPoints Liste de point d'accroche de l'agent
	 * @param localVariables Liste de variables locales (Nom => Class)
	 * @param methodBefore Code ins�r� en before
	 * @param methodAfter Code ins�r� en after
	 * @param methodCatchs Liste des catchs (ExceptionClass => code)
	 * @param methodFinally Code ins�r� en finally
	 */
	public AnalyticaSpyConf(final String collectorName, final Map<String, String> collectorParams, final List<String> excludedPackages, final List<String> includedPackages, final List<AnalyticaSpyHookPoint> hookPoints, final Map<String, String> localVariables, final List<String> methodBefore, final List<String> methodAfter, final Map<String, List<String>> methodCatchs, final List<String> methodFinally) {
		this.collectorName = collectorName;
		this.collectorParams = collectorParams;
		fastExcludedPackages = excludedPackages;
		fastIncludedPackages = includedPackages;
		this.hookPoints = hookPoints;
		this.localVariables = localVariables;
		this.methodBefore = methodBefore;
		this.methodAfter = methodAfter;
		this.methodCatchs = methodCatchs;
		this.methodFinally = methodFinally;
	}

	/**
	 * @return Nom du collector
	 */
	public String getCollectorName() {
		return collectorName;
	}

	/**
	 * @return Parametre du collector
	 */
	public Map<String, String> getCollectorParams() {
		return collectorParams;
	}

	/**
	 * @return Code ins�r� en before
	 */
	public List<String> getMethodBefore() {
		return methodBefore;
	}

	/**
	 * @return Code ins�r� en after
	 */
	public List<String> getMethodAfter() {
		return methodAfter;
	}

	/**
	 * @return Liste des catchs (ExceptionClass => code)
	 */
	public Map<String, List<String>> getMethodCatchs() {
		return methodCatchs;
	}

	/**
	 * Attention : ne voit pas les variables locales.
	 * @return Code ins�r� en finally
	 */
	public List<String> getMethodFinally() {
		return methodFinally;
	}

	/**
	 * @return Liste de variables locales (Nom => Class)
	 */
	public Map<String, String> getLocalVariables() {
		return localVariables;
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

	/**
	 * @return Conf au format Json.
	 */
	public String toJson() {
		return new Gson().toJson(this);
	}
}