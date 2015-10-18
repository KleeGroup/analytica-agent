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
package io.analytica.agent;

import io.analytica.AbstractAnalyticaTestCaseJU4;
import io.analytica.agent.impl.net.RemoteConnector;
import io.analytica.api.KProcessCollector;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * Cas de Test JUNIT de l'API Analytics.
 *
 * @author pchretien, npiedeloup
 * @version $Id: AgentManagerTest.java,v 1.3 2012/03/29 08:48:19 npiedeloup Exp $
 */
public abstract class AbstractProcessCollectorTest extends AbstractAnalyticaTestCaseJU4 {

	/** Base de données gérant les articles envoyés dans une commande. */
	private static final String PROCESS1_TYPE = "ARTICLE";
	private static final String PROCESS2_TYPE = "COMMANDE";

	/** Logger. */
	private final Logger log = Logger.getLogger(getClass());

	private KProcessCollector processCollector;
	private RemoteConnector remoteConnector;

	//-------------------------------------------------------------------------

	@Override
	protected final void doSetUp() throws Exception {
		remoteConnector = new RemoteConnector("http://localhost:9998/process", 20, 1);
		//		processCollector = new KProcessCollector(this.getClass().getSimpleName(), new String[] { "test", InetAddress.getLocalHost().getHostName() }, remoteConnector);
		final String location = "myServer";
		processCollector = new KProcessCollector("myPrettyApp", location, remoteConnector);
		remoteConnector.start();
		afterSetUp();
	}

	protected abstract void afterSetUp() throws IOException;

	@Override
	protected final void doTearDown() throws Exception {
		if (remoteConnector != null) {
			remoteConnector.stop();
		}
	}

	/**
	 * Test simple avec un compteur.
	 * Test sur l'envoi d'un process
	 * Chaque article coute 10€.
	 */
	@Test
	public void testOneProcess() {
		processCollector.startProcess(PROCESS1_TYPE, "1 Article 25 Kg");
		processCollector.setMeasure("POIDS", 25);
		processCollector.incMeasure("MONTANT", 10);
		processCollector.stopProcess();
		flushAgentToServer();
		checkMetricCount("MONTANT", 1, PROCESS1_TYPE);
	}

	/**
	 * Test simple avec deux compteurs.
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg.
	 * Chaque article coute 10€.
	 */
	@Test
	public void test1000Articles() {
		doNArticles(1000);
		flushAgentToServer();
		checkMetricCount("MONTANT", 1000, PROCESS1_TYPE);
	}

	/**
	 * Test pour vérifier que l'on peut se passer des processus si et seulement si le mode Analytics est désactivé.
	 */
	@Test
	public void testNoProcess() {
		try {
			processCollector.setMeasure("POIDS", 25);
			Assert.fail();
		} catch (final Exception e) {
			// Ce cas de test est réussi s'il remonte une exception
			// OK
			Assert.assertTrue(true);
		}
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
		flushAgentToServer();
		checkMetricCount("MONTANT", 5, PROCESS2_TYPE); //nombre de commande
		checkMetricCount("MONTANT", 5 * 15, PROCESS1_TYPE); //nombre d'article
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
		final long nbCommandes = 200;
		for (int i = 0; i < nbCommandes; i++) {
			workersPool.execute(new CommandeTask(String.valueOf(i), 5));
		}
		workersPool.shutdown();
		workersPool.awaitTermination(2 * 60, TimeUnit.SECONDS); //On laisse 2 minute pour vider la pile
		if (!workersPool.isTerminated()) {
			throw new IllegalStateException("Les threads ne sont pas tous stoppés");
		}

		log.trace("elapsed = " + (System.currentTimeMillis() - start));

		flushAgentToServer();
		checkMetricCount("MONTANT", nbCommandes, PROCESS2_TYPE); //nombre de commande
		checkMetricCount("MONTANT", nbCommandes * 5, PROCESS1_TYPE); //nombre d'article
	}

	/**
	 * Passe N commandes.
	 * @param nbCommandes Numero de la commande
	 * @param nbArticles Nombre d'article
	 */
	void doNCommande(final int nbCommandes, final int nbArticles) {
		processCollector.startProcess(PROCESS2_TYPE, nbCommandes + " Commandes");
		for (int i = 0; i < nbCommandes; i++) {
			doOneCommande(String.valueOf(i), nbArticles);
		}
		processCollector.stopProcess();
	}

	/**
	 * Passe une commande.
	 * @param numCommande Numero de la commande
	 * @param nbArticles Nombre d'article
	 */
	void doOneCommande(final String numCommande, final int nbArticles) {
		processCollector.startProcess(PROCESS2_TYPE, "1 Commande");
		processCollector.incMeasure("MONTANT", 5);
		processCollector.addMetaData("NUMERO", numCommande);
		doNArticles(nbArticles);
		processCollector.stopProcess();
	}

	/**
	 * Ajoute N articles.
	 * @param nbArticles Nombre d'article
	 */
	void doNArticles(final int nbArticles) {
		processCollector.startProcess(PROCESS1_TYPE, nbArticles + " Articles 25 Kg");
		for (int i = 0; i < nbArticles; i++) {
			processCollector.startProcess(PROCESS1_TYPE, "1 Article 25 Kg");
			processCollector.setMeasure("POIDS", 25);
			processCollector.incMeasure("MONTANT", 10);
			processCollector.stopProcess();
		}
		processCollector.stopProcess();
	}

	private final class CommandeTask implements Runnable {
		private final String numCommande;
		private final int nbArticles;

		public CommandeTask(final String numCommande, final int nbArticles) {
			this.numCommande = numCommande;
			this.nbArticles = nbArticles;
		}

		@Override
		public void run() {
			doOneCommande(numCommande, nbArticles);
			System.out.println("Finish commande n°" + numCommande);
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				//rien
			}
		}
	}
}
