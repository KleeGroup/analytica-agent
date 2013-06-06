package com.kleegroup.analyticaimpl.spies.logs;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import kasper.AbstractTestCaseJU4;
import kasper.kernel.util.Assertion;
import kasper.resource.ResourceManager;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.kleegroup.analytica.agent.AgentManager;
import com.kleegroup.analytica.hcube.cube.HCube;
import com.kleegroup.analytica.hcube.cube.HMetric;
import com.kleegroup.analytica.hcube.cube.HMetricKey;
import com.kleegroup.analytica.hcube.dimension.HCategory;
import com.kleegroup.analytica.hcube.dimension.HTimeDimension;
import com.kleegroup.analytica.hcube.query.HQuery;
import com.kleegroup.analytica.server.ServerManager;

import edu.emory.mathcs.backport.java.util.Arrays;

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
public final class AnalyticaLogSpyTest extends AbstractTestCaseJU4 {

	@Inject
	private AgentManager agentManager;
	@Inject
	private ResourceManager resourceManager;
	@Inject
	private ServerManager serverManager;

	/**
	 * Test simple avec deux compteurs. 
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg. 
	 * Chaque article coute 10€.
	 */
	@Test
	public void testMiniLog() {
		final LogSpyReader logSpyReader = new LogSpyReader(agentManager, resourceManager, translateFileName("./catalina.out", getClass()), translateFileName("./logSpyConf.json", getClass()));
		logSpyReader.start();

		flushAgentToServer();
		checkMetricCount("duration", 1, "REQUETE");
	}

	@Test
	public void testConf() {
		final String[] dateFormats = { "HH:mm:ss.SSS", "MMM d, yyyy h:mm:ss a" };
		final LogPattern[] PATTERNS = { //
		new LogPattern("REQUETE", "^INFO  ([0-9:\\.]+) \\[([a-zA-Z0-9-_]+)\\] spark.ui.web.LogFilter - Sortie de la ([\\u0000-\\uFFFF]+) [\\u0000-\\uFFFF]+/pages/([\\u0000-\\uFFFF]+) in ([0-9]+)ms$", //
				false, 1, 2, -1, 4, 5, true, false), //
				new LogPattern("REQUETE_RESOURCE", "^INFO  ([0-9:\\.]+) \\[([a-zA-Z0-9-_]+)\\] spark.ui.web.LogFilter - Sortie de la ([\\u0000-\\uFFFF]+) [\\u0000-\\uFFFF]+/javax\\.faces\\.resource/([\\u0000-\\uFFFF]+) in ([0-9]+)ms$", //
						false, 1, 2, -1, 4, 5, true, false), //
				new LogPattern("PERF", "^INFO  ([0-9:\\.]+) \\[([a-zA-Z0-9-]+)\\] Performance - >> ([a-zA-Z0-9]+) : ([a-zA-Z0-9-_]+) : time = ([0-9]+)$", //
						false, 1, 2, 3, 4, 5, false, false),//
				new LogPattern("RELOAD", "^([\\u0000-\\uFFFF]+) org.apache.catalina.startup.Catalina load$", //
						false, 1, -1, -1, -1, -1, false, true),//
		};

		final LogSpyConf conf = new LogSpyConf(Arrays.asList(dateFormats), Arrays.asList(PATTERNS));
		System.out.println(new Gson().toJson(conf));

	}

	private HMetric getMetricInTodayCube(final String metricName, final String type, final String... subTypes) {
		final HQuery query = serverManager.createQueryBuilder() //
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

	/**
	 * Envoi les données vers le serveur.
	 */
	protected void flushAgentToServer() {
		//rien en local pas de flush
	}

	private static final String translateFileName(final String fileName, final Class<?> relativeRootClass) {
		Assertion.notEmpty(fileName);
		//---------------------------------------------------------------------
		if (fileName.startsWith(".")) {
			//soit en relatif
			return "/" + getRelativePath(relativeRootClass) + "/" + fileName;
		}

		//soit en absolu		
		if (fileName.startsWith("/")) {
			return fileName;
		}
		return "/" + fileName;
	}

	private static final String getRelativePath(final Class<?> relativeRootClass) {
		return relativeRootClass.getPackage().getName().replace('.', '/');
	}
}
