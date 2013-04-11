package com.kleegroup.analyticaimpl.spies.javassist;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import kasper.AbstractTestCaseJU4;
import kasper.kernel.exception.KRuntimeException;

import org.junit.Test;

import com.kleegroup.analytica.agent.AgentManager;
import com.kleegroup.analytica.hcube.cube.DataKey;
import com.kleegroup.analytica.hcube.cube.DataType;
import com.kleegroup.analytica.hcube.cube.MetricKey;
import com.kleegroup.analytica.hcube.dimension.TimeDimension;
import com.kleegroup.analytica.hcube.dimension.WhatDimension;
import com.kleegroup.analytica.hcube.query.Query;
import com.kleegroup.analytica.hcube.query.QueryBuilder;
import com.kleegroup.analytica.server.ServerManager;
import com.kleegroup.analytica.server.data.Data;
import com.kleegroup.analyticaimpl.spies.javassist.agentloader.VirtualMachineAgentLoader;

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

	private static final String TEST_CLASS_NAME = "com.kleegroup.analyticaimpl.spies.javassist.TestAnalyse";

	@Inject
	private AgentManager agentManager;
	@Inject
	private ServerManager serverManager;

	/**
	 * Demarre l'agent de supervision des création d'instances.
	 */
	public static void startAgent() {
		final File agentJar = getFile("analyticaAgent-1.0.0.jar", AnalyticaSpyAgentTest.class);
		final File propertiesFile = getFile("testJavassistAnalyticaSpy.properties", AnalyticaSpyAgentTest.class);

		VirtualMachineAgentLoader.loadAgent(agentJar.getAbsolutePath(), propertiesFile.getAbsolutePath());
	}

	/** {@inheritDoc} */
	@Override
	protected void doSetUp() throws Exception {
		startAgent();
		//il faut charger la class, sans la référencer pour avoir le temps de démarrer l'agent
		//ClassUtil.classForName(TEST_CLASS_NAME);
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
		printDatas("DURATION");
	}

	@Test
	public void testJavassistWorkError() {
		new TestAnalyse().workError();
		printDatas("DURATION");
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

}
