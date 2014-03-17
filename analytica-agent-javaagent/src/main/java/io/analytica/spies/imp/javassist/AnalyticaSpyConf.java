/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidière - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you permission to link this library
 * with independent modules to produce an executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the license of that module.
 * An independent module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version of the library,
 * but you are not obliged to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 */
package io.analytica.spies.imp.javassist;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

/**
 * Configuration du JavassistSpy, qui définie les points d'encrage et les spécificités du branchement de l'agent.
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
	 * @param collectorParams Paramètres du plugin
	 * @param excludedPackages Liste de package à exclure
	 * @param includedPackages Liste de package à inclure
	 * @param hookPoints Liste de point d'accroche de l'agent
	 * @param localVariables Liste de variables locales (Nom => Class)
	 * @param methodBefore Code inséré en before
	 * @param methodAfter Code inséré en after
	 * @param methodCatchs Liste des catchs (ExceptionClass => code)
	 * @param methodFinally Code inséré en finally
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
	 * @return Code inséré en before
	 */
	public List<String> getMethodBefore() {
		return methodBefore;
	}

	/**
	 * @return Code inséré en after
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
	 * @return Code inséré en finally
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
