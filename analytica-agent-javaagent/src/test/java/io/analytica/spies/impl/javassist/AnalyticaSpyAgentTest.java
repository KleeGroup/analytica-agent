package io.analytica.spies.impl.javassist;

import io.analytica.AbstractAnalyticaTestCaseJU4;
import io.analytical.spies.imp.javassist.AnalyticaSpyAgent;
import io.analytical.spies.imp.javassist.agentloader.VirtualMachineAgentLoader;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

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
public final class AnalyticaSpyAgentTest extends AbstractAnalyticaTestCaseJU4 {

	private static final String TEST1_CLASS_NAME = "com.kleegroup.analyticaimpl.spies.javassist.TestAnalyse";
	private static final String TEST2_CLASS_NAME = "com.kleegroup.analyticaimpl.spies.javassist.TestAnalyse2";
	private static final String TEST3_CLASS_NAME = "com.kleegroup.analyticaimpl.spies.javassist.TestAnalyse3";

	//private static final String TEST3_PARENT_CLASS_NAME = "com.kleegroup.analyticaimpl.spies.javassist.ParentTestAnalyse";

	/**
	 * Demarre l'agent de supervision des création d'instances.
	 */
	public static void startAgent() {
		final File agentJar = getFile("analyticaAgent-1.4.1.jar", AnalyticaSpyAgentTest.class);
		final File propertiesFile = getFile("testJavassistAnalyticaSpy.json", AnalyticaSpyAgentTest.class);

		VirtualMachineAgentLoader.loadAgent(agentJar.getAbsolutePath(), propertiesFile.getAbsolutePath());
	}

	/** {@inheritDoc} */
	@Override
	public void doSetUp() throws Exception {
		//on charge la class avant pour s'assurer que le load fonctionne
		classForName(TestAnalyse.class.getName());
		startAgent();
		startServer();
	}

	/**
	 * Récupération d'une classe non typée à partir de son nom.
	 * 
	 * @param javaClassName Nom de la classe
	 * @return Classe java
	 */
	private static Class<?> classForName(final String javaClassName) {
		try {
			return Class.forName(javaClassName);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("Class not found : " + javaClassName, e);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void doTearDown() throws Exception {
		AnalyticaSpyAgent.stopAgent();
	}

	@Override
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
			throw new RuntimeException(e);
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

}
