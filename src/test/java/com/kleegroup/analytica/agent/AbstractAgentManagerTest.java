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
 */
package com.kleegroup.analytica.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.management.Query;
import javax.xml.crypto.Data;

import kasper.AbstractTestCaseJU4;
import kasper.kernel.metamodel.DataType;
import kasper.kernel.util.Assertion;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.kleegroup.analytica.hcube.dimension.HTimeDimension;
import com.kleegroup.analytica.hcube.query.HQuery;
import com.kleegroup.analytica.hcube.query.HQueryBuilder;
import com.kleegroup.analytica.hcube.result.HResult;
import com.kleegroup.analytica.server.ServerManager;

/**
 * Cas de Test JUNIT de l'API Analytics.
 * 
 * @author pchretien, npiedeloup
 * @version $Id: AgentManagerTest.java,v 1.3 2012/03/29 08:48:19 npiedeloup Exp $
 */
public abstract class AbstractAgentManagerTest extends AbstractTestCaseJU4 {

	/** Base de données gérant les articles envoyés dans une commande. */
	private static final String PROCESS1_TYPE = "ARTICLE";
	private static final String PROCESS2_TYPE = "COMMANDE";

	/** Logger. */
	private final Logger log = Logger.getLogger(getClass());

	@Inject
	private AgentManager agentManager;
	@Inject
	private ServerManager serverManager;

	//-------------------------------------------------------------------------

	/**
	 * Test simple avec deux compteurs. 
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg. 
	 * Chaque article coute 10€.
	 */
	@Test
	public void test1000Articles() {
		doNArticles(1000);
		printDatas("MONTANT");
	}

	/**
	 * Test pour vérifier que l'on peut se passer des processus si et seulement si le mode Analytics est désactivé.
	 */
	@Test
	public void testNoProcess() {
		try {
			agentManager.setMeasure("POIDS", 25);
			Assert.fail();
		} catch (final Exception e) {
			// Ce cas de test est réussi s'il remonte une exception
			// OK
			Assert.assertTrue(true);
		}
	}

	/**
	 * Même test après désactivation.
	 */
	@Test
	public void testOff() {
		doNArticles(50);
		printDatas("MONTANT");
	}

	/**
	 * Test de récursivité. 
	 * Test sur l'envoi de 500 commandes contenant chacune 500 articles d'un poids de 25 kg. 
	 * Chaque article coute 10€. 
	 * Les frais d'envoi sont de 5€.
	 */
	@Test
	public void test500Commandes() {
		final long start = System.currentTimeMillis();
		doNCommande(5, 15);
		log.trace("elapsed = " + (System.currentTimeMillis() - start));
		printDatas("MONTANT");
	}

	/**
	 * Test de parallélisme. 
	 * Test sur l'envoi de 500 commandes contenant chacune 1000 articles d'un poids de 25 kg.
	 * L'envoi est simuler avec 20 clients (thread).
	 * Chaque article coute 10€. 
	 * Les frais d'envoi sont de 5€.
	 * @throws InterruptedException Interruption
	 */
	@Test
	public void testMultiThread() throws InterruptedException {
		final long start = System.currentTimeMillis();
		final ExecutorService workersPool = Executors.newFixedThreadPool(20);

		for (int i = 0; i < 50; i++) {
			workersPool.execute(new CommandeTask(String.valueOf(i), 5));
		}
		workersPool.shutdown();
		workersPool.awaitTermination(2, TimeUnit.MINUTES); //On laisse 2 minute pour vider la pile   
		Assertion.invariant(workersPool.isTerminated(), "Les threads ne sont pas tous stoppés");

		log.trace("elapsed = " + (System.currentTimeMillis() - start));
		printDatas("MONTANT");
		//System.out.println(analyticaUIManager.toString(serverManager.getProcesses()));
	}

	//	@Test
	//	public void testMetaData() {
	//		agentManager.startProcess("TEST_META_DATA", "Process1");
	//		agentManager.addMetaData("TEST_META_DATA_1", "MD1");
	//		agentManager.stopProcess();
	//		agentManager.startProcess("TEST_META_DATA", "Process2");
	//		agentManager.addMetaData("TEST_META_DATA_1", "MD2");
	//		agentManager.addMetaData("TEST_META_DATA_2", "MD3");
	//		agentManager.stopProcess();
	//		//---------------------------------------------------------------------
	//		final DataKey[] metrics = new DataKey[] { new DataKey("TEST_META_DATA_1", DataType.metaData), new DataKey("TEST_META_DATA_2", DataType.metaData) };
	//
	//		final List<Data> datas = getCubeToday("TEST_META_DATA", metrics);
	//		Set<String> value = getMetaData(datas, "TEST_META_DATA_1");
	//		Assert.assertTrue("Le cube ne contient pas la metaData attendue : MD1\n" + datas, value.contains("MD1"));
	//		Assert.assertTrue("Le cube ne contient pas la metaData attendue : MD2\n" + datas, value.contains("MD2"));
	//		//---------------------------------------------------------------------
	//		value = getMetaData(datas, "TEST_META_DATA_2");
	//		Assert.assertTrue("Le cube ne contient pas la metaData attendue\n" + datas, value.contains("MD3"));
	//	}

	@Test
	public void testMean() {
		agentManager.startProcess("TEST_MEAN1", "Process1");
		agentManager.incMeasure("TEST_MEAN_VALUE", 100);
		agentManager.stopProcess();
		agentManager.startProcess("TEST_MEAN1", "Process2");
		agentManager.incMeasure("TEST_MEAN_VALUE", 50);
		agentManager.stopProcess();
		//---------------------------------------------------------------------
		final DataKey[] metrics = new DataKey[] { new DataKey(new MetricKey("TEST_MEAN_VALUE"), DataType.mean) };
		final List<Data> datas = getCubeToday("TEST_MEAN1", metrics);
		final double valueMean = getMean(datas, "TEST_MEAN_VALUE");
		Assert.assertEquals("Le cube ne contient pas la moyenne attendue\n" + datas, 75.0, valueMean, 0);
	}

	@Test
	public void testMeanZero() {
		agentManager.startProcess("TEST_MEAN2", "Process1");
		agentManager.incMeasure("TEST_MEAN_VALUE", 90);
		agentManager.stopProcess();
		agentManager.startProcess("TEST_MEAN2", "Process2");
		//TEST_MEAN_VALUE = 0 implicite
		agentManager.stopProcess();
		//---------------------------------------------------------------------
		final DataKey[] metrics = new DataKey[] { new DataKey(new MetricKey("TEST_MEAN_VALUE"), DataType.mean) };
		final List<Data> datas = getCubeToday("TEST_MEAN2", metrics);
		final double valueMean = getMean(datas, "TEST_MEAN_VALUE");
		Assert.assertEquals("Le cube ne contient pas la moyenne attendue\n" + datas, 45.0, valueMean, 0);
	}

	private double getMean(final List<Data> datas, final String measureName) {
		for (final Data data : datas) {
			if (data.getKey().getType() == DataType.mean && ("metric:" + measureName).equals(data.getKey().getMetricKey().id())) {
				return data.getValue();
			}
		}
		throw new IllegalArgumentException("La mesure " + measureName + " n'est pas trouvée dans le module \n" + datas);
	}

	private Set<String> getMetaData(final List<Data> datas, final String metadataName) {
		final Set<String> metaDatas = new HashSet<String>();
		for (final Data data : datas) {
			if (metadataName.equals(data.getKey().getMetricKey().id())) {
				metaDatas.addAll(data.getStringValues());
			}
		}
		Assert.assertTrue("La metaData " + metadataName + " n'est pas trouvée dans le module\n" + datas, metaDatas.size() >= 1);
		return metaDatas;
	}

	private HResult getCubeToday(final String module) {
		final HQuery query = new HQueryBuilder().on(HTimeDimension.Minute).from(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)).to(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) //
				.with(module) //
				.build();

		//Accès au serveur pour valider les résultats injectés
		final HResult datas = serverManager.execute(query);
		return datas;
	}

	private List<DataKey> asList(final DataKey... dataKey) {
		return Arrays.asList(dataKey);
	}

	void printDatas(final String... metrics) {
		final List<DataKey> keys = new ArrayList<DataKey>(metrics.length * 2);
		for (final String metric : metrics) {
			keys.add(new DataKey(new MetricKey(metric), DataType.count));
			keys.add(new DataKey(new MetricKey(metric), DataType.mean));
		}
		final Query query = new QueryBuilder(keys) //
				.on(TimeDimension.Day).from(new Date()).to(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) //
				.on(WhatDimension.SimpleName).with("/") //
				.build();

		//Accès au serveur pour valider les résultats injectés
		final List<Data> datas = serverManager.getData(query);
		for (final Data data : datas) {
			System.out.println(data);
		}
	}

	/**
	 * Passe N commandes.
	 * @param nbCommandes Numero de la commande
	 * @param nbArticles Nombre d'article
	 */
	void doNCommande(final int nbCommandes, final int nbArticles) {
		agentManager.startProcess(PROCESS2_TYPE, nbCommandes + " Commandes");
		for (int i = 0; i < nbCommandes; i++) {
			doOneCommande(String.valueOf(i), nbArticles);
		}
		agentManager.stopProcess();
	}

	/**
	 * Passe une commande.
	 * @param numCommande Numero de la commande
	 * @param nbArticles Nombre d'article
	 */
	void doOneCommande(final String numCommande, final int nbArticles) {
		agentManager.startProcess(PROCESS2_TYPE, "1 Commande");
		agentManager.incMeasure("MONTANT", 5);
		agentManager.addMetaData("NUMERO", numCommande);
		doNArticles(nbArticles);
		agentManager.stopProcess();
	}

	/**
	 * Ajoute N articles.
	 * @param nbArticles Nombre d'article
	 */
	void doNArticles(final int nbArticles) {
		agentManager.startProcess(PROCESS1_TYPE, nbArticles + " Articles 25 Kg");
		for (int i = 0; i < nbArticles; i++) {
			agentManager.startProcess(PROCESS1_TYPE, "1 Article 25 Kg");
			agentManager.setMeasure("POIDS", 25);
			agentManager.incMeasure("MONTANT", 10);
			agentManager.stopProcess();
		}
		agentManager.stopProcess();
	}

	private final class CommandeTask implements Runnable {
		private final String numCommande;
		private final int nbArticles;

		public CommandeTask(final String numCommande, final int nbArticles) {
			this.numCommande = numCommande;
			this.nbArticles = nbArticles;
		}

		public void run() {
			doOneCommande(numCommande, nbArticles);
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				//rien
			}
			System.out.println("Finish commande n°" + numCommande);
		}
	}
}
