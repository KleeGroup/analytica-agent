package com.kleegroup.analyticaimpl.spies.junitrules;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import kasper.AbstractTestCaseJU4;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.kleegroup.analytica.agent.AgentManager;
import com.kleegroup.analytica.hcube.cube.HCube;
import com.kleegroup.analytica.hcube.cube.HMetric;
import com.kleegroup.analytica.hcube.cube.HMetricKey;
import com.kleegroup.analytica.hcube.dimension.HCategory;
import com.kleegroup.analytica.hcube.dimension.HTimeDimension;
import com.kleegroup.analytica.hcube.query.HQuery;
import com.kleegroup.analytica.hcube.query.HQueryBuilder;
import com.kleegroup.analytica.server.ServerManager;

/**
 * Implementation d'un agent de jvm.
 * Celui ci doit etre inclus dans un jar et passé en parametre à la jvm :
 * <code>-javaagent:"monjar.jar"=option</code>
 * Ce jar doit avoir un manifest qui contient la ligne suivante :
 * <code>Premain-Class: com.kleegroup.analyticaimpl.spies.javassist.AnalyticaSpyAgent</code>
 *
 * Cet agent ajoute un ClassFileTransformer spécifique qui a pour but d'instrumenter
 * les méthodes selon un paramétrage externe.
 * L'option de l'agent dans la ligne de commande représente le nom du fichier de paramétrage.
 *
 * @author npiedeloup
 * @version $Id: MemoryLeakAgent.java,v 1.2 2012/09/28 09:30:03 pchretien Exp $
 */
public final class AnalyticaSpyJunitRuleTest extends AbstractTestCaseJU4 {

	@Inject
	private AgentManager agentManager;
	@Inject
	private ServerManager serverManager;

	@Rule
	public JunitRuleSpy junitRule = new JunitRuleSpy();

	/** {@inheritDoc} */
	@Override
	protected void doSetUp() throws Exception {
		//
	}

	/**
	 * Test simple avec deux compteurs. 
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg. 
	 * Chaque article coute 10€.
	 */
	@Test
	public void testJunitRule() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			//rien
		}
		flushAgentToServer();
		//checkMetricCount("duration", 1, "JUNIT", "com.kleegroup.analyticaimpl.spies.junitrules.AnalyticaSpyJunitRuleTest", "testJunitRule");
	}

	protected void flushAgentToServer() {
		//rien en local pas d'attente
	}

	private HMetric getMetricInTodayCube(final String metricName, final String type, final String... subTypes) {
		final HQuery query = new HQueryBuilder() //
				.on(HTimeDimension.Day).from(new Date()).to(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) //
				.with(type, subTypes) //
				.build();
		final HCategory category = new HCategory(type, subTypes);
		final List<HCube> cubes = serverManager.execute(query).getSerie(category).getCubes();
		Assert.assertFalse("Le cube [" + category + "] n'apparait pas dans les cubes", cubes.isEmpty());
		final HCube firstCube = cubes.get(0);
		final HMetricKey metricKey = new HMetricKey(metricName, false);
		Assert.assertTrue("Le cube [" + firstCube + "] ne contient pas la metric: " + metricName, firstCube.getMetric(metricKey) != null);
		return firstCube.getMetric(metricKey);
	}

	private void checkMetricCount(final String metricName, final long countExpected, final String type, final String... subTypes) {
		final HCategory category = new HCategory(type, subTypes);
		final HMetric metric = getMetricInTodayCube(metricName, type, subTypes);
		Assert.assertEquals("Le cube [" + category + "] n'est pas peuplé correctement", countExpected, metric.getCount(), 0);
	}

	//	private void checkMetricMean(final String metricName, final double meanExpected, final String type, final String... subTypes) {
	//		final HCategory category = new HCategory(type, subTypes);
	//		final HMetric metric = getMetricInTodayCube(metricName, type, subTypes);
	//		Assert.assertEquals("Le cube [" + category + "] n'est pas peuplé correctement", meanExpected, metric.getMean(), 0);
	//	}

}
