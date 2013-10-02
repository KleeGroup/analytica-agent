package com.kleegroup.analyticaimpl.spies.javassist;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import kasper.AbstractTestCaseJU4;
import kasper.kernel.exception.KRuntimeException;
import kasper.kernel.util.ClassUtil;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.Assert;
import org.junit.Test;

import com.kleegroup.analytica.hcube.cube.HCube;
import com.kleegroup.analytica.hcube.cube.HMetric;
import com.kleegroup.analytica.hcube.cube.HMetricKey;
import com.kleegroup.analytica.hcube.dimension.HCategory;
import com.kleegroup.analytica.hcube.dimension.HTimeDimension;
import com.kleegroup.analytica.hcube.query.HQuery;
import com.kleegroup.analytica.hcube.query.HQueryBuilder;
import com.kleegroup.analytica.server.ServerManager;
import com.kleegroup.analyticaimpl.spies.javassist.agentloader.VirtualMachineAgentLoader;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

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
public final class AnalyticaSpyAgentTest extends AbstractTestCaseJU4 {

	private static final String TEST1_CLASS_NAME = "com.kleegroup.analyticaimpl.spies.javassist.TestAnalyse";
	private static final String TEST2_CLASS_NAME = "com.kleegroup.analyticaimpl.spies.javassist.TestAnalyse2";
	private static final String TEST3_CLASS_NAME = "com.kleegroup.analyticaimpl.spies.javassist.TestAnalyse3";
	//private static final String TEST3_PARENT_CLASS_NAME = "com.kleegroup.analyticaimpl.spies.javassist.ParentTestAnalyse";

	@Inject
	private ServerManager serverManager;

	/**
	 * Demarre l'agent de supervision des création d'instances.
	 */
	public static void startAgent() {
		final File agentJar = getFile("analyticaAgent-1.3.0.jar", AnalyticaSpyAgentTest.class);
		final File propertiesFile = getFile("testJavassistAnalyticaSpy.json", AnalyticaSpyAgentTest.class);

		VirtualMachineAgentLoader.loadAgent(agentJar.getAbsolutePath(), propertiesFile.getAbsolutePath());
	}

	/** {@inheritDoc} */
	@Override
	protected void doSetUp() throws Exception {
		//on charge la class avant pour s'assurer que le load fonctionne
		ClassUtil.classForName(TestAnalyse.class.getName());
		startAgent();
		httpServer = startServer();
		System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl", BASE_URI));
	}

	/** {@inheritDoc} */
	@Override
	protected void doTearDown() throws Exception {
		AnalyticaSpyAgent.stopAgent();
		httpServer.stop();
	}

	private HttpServer httpServer;

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/").port(9998).build();
	}

	public static final URI BASE_URI = getBaseURI();

	protected static HttpServer startServer() throws IOException {
		System.out.println("Starting grizzly...");
		final ResourceConfig rc = new PackagesResourceConfig("com.kleegroup.analyticaimpl.server.plugins.api.rest");
		rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, com.sun.jersey.api.container.filter.GZIPContentEncodingFilter.class.getName());
		rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, com.sun.jersey.api.container.filter.GZIPContentEncodingFilter.class.getName());
		return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
	}

	protected void flushAgentToServer() {
		try {
			Thread.sleep(5000);//on attend 2s que le process soit envoyé au serveur.
		} catch (final InterruptedException e) {
			//rien on stop juste l'attente
		}
	}

	/**
	 * Récupère un File (pointeur de fichier) vers un fichier relativement à une class.
	 * @param fileName Nom/path du fichier
	 * @param baseClass Class de base pour le chemin relatif  
	 * @return File
	 */
	private static File getFile(final String fileName, final Class<?> baseClass) {
		final URL fileURL = baseClass.getResource(fileName);
		try {
			return new File(fileURL.toURI());
		} catch (final URISyntaxException e) {
			throw new KRuntimeException(e);
		}
	}

	/**
	 * Test simple avec deux compteurs. 
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg. 
	 * Chaque article coute 10€.
	 */
	@Test
	public void testJavassistWork1s() {
		new TestAnalyse().work1s();
		flushAgentToServer();
		checkMetricCount("duration", 1, "JAVASSIST", TEST1_CLASS_NAME, "work1s");
	}

	/**
	 * Test d'un traitement qui fait une erreur.
	 * Le process doit-être capturé malgré tout
	 */
	@Test
	public void testJavassistWorkError() {
		try {
			new TestAnalyse().workError();
			Assert.fail("workError n'a pas lancée d'exception");
		} catch (final Exception e) {
			//on veut vérifier que le process est renseigné après l'exception
		}
		flushAgentToServer();
		checkMetricCount("duration", 1, "JAVASSIST", TEST1_CLASS_NAME, "workError");
		checkMetricMean("ME_ERROR_PCT", 100, "JAVASSIST", TEST1_CLASS_NAME, "workError");
	}

	@Test
	public void testJavassistWorkResult() {
		final int result = new TestAnalyse().workResult();
		Assert.assertEquals(1, result);
		flushAgentToServer();
		checkMetricCount("duration", 1, "JAVASSIST", TEST1_CLASS_NAME, "workResult");
	}

	@Test
	public void testJavassistWorkReentrant() {
		new TestAnalyse().workReentrant();
		flushAgentToServer();
		checkMetricCount("duration", 1, "JAVASSIST", TEST1_CLASS_NAME, "workReentrant");
		checkMetricCount("duration", 1, "JAVASSIST", TEST1_CLASS_NAME, "work1s");
	}

	@Test
	public void testJavassistWorkInterface() {
		new TestAnalyse2().workInterface();
		flushAgentToServer();
		checkMetricCount("duration", 1, "JAVASSIST", TEST2_CLASS_NAME, "workInterface");
	}

	@Test
	public void testJavassistWorkParent() {
		new TestAnalyse3().workParent();
		flushAgentToServer();
		checkMetricCount("duration", 1, "JAVASSIST", TEST3_CLASS_NAME, "workParent");
	}

	@Test
	public void testJavassistWorkParentAbstract() {
		new TestAnalyse3().workParentAbstract();
		flushAgentToServer();
		checkMetricCount("duration", 1, "JAVASSIST", TEST3_CLASS_NAME, "workParentAbstract");
	}

	/**
	 * Test simple avec deux compteurs. 
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg. 
	 * Chaque article coute 10€.
	 */
	@Test
	public void testJavassistWorkStatic() {
		TestAnalyse.workStatic();
		flushAgentToServer();
		checkMetricCount("duration", 1, "JAVASSIST", TEST1_CLASS_NAME, "workStatic");
	}

	/**
	 * Test simple avec deux compteurs. 
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg. 
	 * Chaque article coute 10€.
	 */
	@Test
	public void testInstrumentPerf() {
		for (int i = 0; i < 10; i++) {
			new TestAnalyse().workFastest();
			new TestAnalyse().workFastestNotInstrumented();
		}

		final int nbLoop = 100000;
		final long start = System.currentTimeMillis();
		for (int i = 0; i < nbLoop; i++) {
			new TestAnalyse().workFastest();
		}
		final long timeInstrumented = System.currentTimeMillis() - start;

		final long startNotInstrumented = System.currentTimeMillis();
		for (int i = 0; i < nbLoop; i++) {
			new TestAnalyse().workFastestNotInstrumented();
		}
		final long timeNotInstrumented = System.currentTimeMillis() - startNotInstrumented;
		final long delta = timeInstrumented - timeNotInstrumented;
		final long percent = timeNotInstrumented > 0 ? delta * 100 / timeNotInstrumented : 0;
		System.out.println("Time Instrumentation : " + delta + " ms pour " + nbLoop + " soit " + percent + "% de " + timeNotInstrumented + " ms (" + delta * 1000 / nbLoop / 1000d + "ms par appel)");
		flushAgentToServer();
		checkMetricCount("duration", nbLoop + 10, "JAVASSIST", TEST1_CLASS_NAME, "workFastest");
	}

	/**
	 * Test simple avec deux compteurs. 
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg. 
	 * Chaque article coute 10€.
	 */
	@Test
	public void testActivateDesactivate() {
		new TestAnalyse().work1s();
		flushAgentToServer();
		checkMetricCount("duration", 1, "JAVASSIST", TEST1_CLASS_NAME, "work1s");

		AnalyticaSpyAgent.stopAgent();
		new TestAnalyse().work1s();
		flushAgentToServer();
		checkMetricCount("duration", 1, "JAVASSIST", TEST1_CLASS_NAME, "work1s");

		startAgent();
		new TestAnalyse().work1s();
		flushAgentToServer();
		checkMetricCount("duration", 2, "JAVASSIST", TEST1_CLASS_NAME, "work1s");
	}

	/**
	 * Test simple avec deux compteurs. 
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg. 
	 * Chaque article coute 10€.
	 */
	@Test
	public void testActivateDesactivateSameInstance() {
		final TestAnalyse testAnalyse = new TestAnalyse();
		testAnalyse.work1s();
		flushAgentToServer();
		checkMetricCount("duration", 1, "JAVASSIST", TEST1_CLASS_NAME, "work1s");

		AnalyticaSpyAgent.stopAgent();
		testAnalyse.work1s();
		flushAgentToServer();
		checkMetricCount("duration", 1, "JAVASSIST", TEST1_CLASS_NAME, "work1s");

		startAgent();
		testAnalyse.work1s();
		flushAgentToServer();
		checkMetricCount("duration", 2, "JAVASSIST", TEST1_CLASS_NAME, "work1s");
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
		System.out.println("Cube OK :" + category + " metric " + metric);
	}

	private void checkMetricMean(final String metricName, final double meanExpected, final String type, final String... subTypes) {
		final HCategory category = new HCategory(type, subTypes);
		final HMetric metric = getMetricInTodayCube(metricName, type, subTypes);
		Assert.assertEquals("Le cube [" + category + "] n'est pas peuplé correctement", meanExpected, metric.getMean(), 0);
	}

}
