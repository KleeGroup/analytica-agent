package com.kleegroup.analyticaimpl.spies.logs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	 * @throws ParseException 
	 */
	@Test
	public void testMiniLog() throws ParseException {
		final LogSpyReader logSpyReader = new LogSpyReader(agentManager, resourceManager, "file:///d:\\@GitHub\\analytica-agent\\spark-130614\\tomcat7_spark-stdout.2013-06-05.log", "file:///d:/@GitHub/analytica-agent/spark-130614/sparkLogSpyConf-v1.1.json");
		logSpyReader.start();

		flushAgentToServer();
		final Date date = new SimpleDateFormat("ddMMyyyy").parse("30052013");
		checkMetricCount(date, "duration", 7350, "REQUETE");
	}

	@Test
	public void testLog130809() throws ParseException {
		final LogSpyReader logSpyReader = new LogSpyReader(agentManager, resourceManager, "file:///d:\\@GitHub\\analytica-agent\\spark-130614\\spark-130809-bis.perf.log ", "file:///d:/@GitHub/analytica-agent/spark-130614/sparkLogSpyConf-v1.2.json");
		logSpyReader.start();

		flushAgentToServer();
		//final Date date = new SimpleDateFormat("ddMMyyyy").parse("30052013");
		//checkMetricCount(date, "duration", 7350, "REQUETE");
	}

	@Test
	public void testLog130822() throws ParseException {
		final LogSpyReader logSpyReader = new LogSpyReader(agentManager, resourceManager, "file:///d:\\@GitHub\\analytica-agent\\spark-130614\\spark-130822.perf.log ", "file:///d:/@GitHub/analytica-agent/spark-130614/sparkLogSpyConf-v1.2.json");
		logSpyReader.start();

		flushAgentToServer();
		//final Date date = new SimpleDateFormat("ddMMyyyy").parse("30052013");
		//checkMetricCount(date, "duration", 7350, "REQUETE");
	}

	@Test
	public void testLog130829() throws ParseException {
		final LogSpyReader logSpyReader = new LogSpyReader(agentManager, resourceManager, "file:///d:\\@GitHub\\analytica-agent\\spark-130829\\all.log", "file:///d:/@GitHub/analytica-agent/spark-130829/sparkLogSpyConf-v1.3.5.json");
		logSpyReader.start();

		flushAgentToServer();
		final Date date = new SimpleDateFormat("ddMMyyyy").parse("29082013");
		checkMetricCount(date, "duration", 510, "REQUETE");
	}

	@Test
	public void testLog130829FromJson() throws ParseException {
		final LogSpyReader logSpyReader = new LogSpyReader(agentManager, resourceManager, "file:///d:\\logs\\spark130829FileLog.log", "file:///d:\\@GitHub\\analytica-agent/src/test/java/com/kleegroup/analyticaimpl/spies/logs/logSpyConf.json");
		logSpyReader.start();

		flushAgentToServer();
		final Date date = new SimpleDateFormat("ddMMyyyy").parse("29082013");
		checkMetricCount(date, "duration", 510, "REQUETE");
	}

	@Test
	public void testParsing() throws ParseException {
		final String[] testedlogs = { "INFO  14:45:00.071 [SparkScheduler_Worker-2] spark.perf - {\"category\":\"service\",\"operation\":\"transaction\",\"resource\":\"public spark.services.domain.util.TaskLog spark.services.taskLog.TaskLogService.create(spark.commons.data.id.UID,java.lang.String,long)\",\"nanos\":20483609,\"elapsed\":\"20.48 ms\",\"thread\":\"SparkScheduler_Worker-2\",\"timestamp\":\"2013-06-05T14:45:00.055+02:00\"}", };
		final String[] testedPatterns = { "^INFO  ([0-9:\\.]+) \\[([a-zA-Z0-9-_]+)\\] spark\\.perf - .*\"request\"[\\u0000-\\uFFFF]+/pages/([\\u0000-\\uFFFF]+)(;jsessionid=.*)?\",\"nanos\":([0-9]+)[0-9]{6},.*\"timestamp\":\"([0-9T\\:\\.\\-]+)\\+[0-9\\:]+\".*$", //
				"^INFO  ([0-9:\\.]+) .*\"timestamp\":\"([0-9T\\:\\.\\-]+)\\+[0-9\\:]+\".*$", //
				"^INFO  ([0-9:\\.]+) .*\"nanos\":([0-9]+)[0-9]{6},.*\"timestamp\":\"([0-9T\\:\\.\\-]+)\\+[0-9\\:]+\".*$", //
				"^INFO  ([0-9:\\.]+) .*(;jsessionid=.*)?\",\"nanos\":([0-9]+)[0-9]{6},.*\"timestamp\":\"([0-9T\\:\\.\\-]+)\\+[0-9\\:]+\".*$", //
				"^INFO  ([0-9:\\.]+) .*\"request\"[.]+/pages/([.]+)(;jsessionid=.*)?\",\"nanos\":([0-9]+)[0-9]{6},.*\"timestamp\":\"([0-9T\\:\\.\\-]+)\\+[0-9\\:]+\".*$", //
				"^INFO  ([0-9:\\.]+) .*spark\\.perf - .*\"request\"[\\u0000-\\uFFFF]+/pages/([\\u0000-\\uFFFF]+)(;jsessionid=.*)?\",\"nanos\":([0-9]+)[0-9]{6},.*\"timestamp\":\"([0-9T\\:\\.\\-]+)\\+[0-9\\:]+\".*$" };
		doTestParsing(testedlogs, testedPatterns);
	}

	@Test
	public void testParsing2() throws ParseException {
		final String[] testedlogs = { // 
		"DEBUG 2013-08-29T18:30:15.598 [http-bio-8080-exec-2] spark.perf - {\"category\":\"http\",\"operation\":\"request\",\"resource\":\"http://spark-perf-spark.dev.klee.lan.net:8080/spark/pages/home/login.jsf;jsessionid=82FC81D1CF7D2493983859B1033A67BD\",\"thread\":\"http-bio-8080-exec-2\",\"startDate\":\"2013-08-29T18:30:14.122+02:00\",\"startDateMs\":1377793814122,\"endDate\":\"2013-08-29T18:30:15.598+02:00\",\"endDateMs\":1377793815598,\"deltaMs\":1476}", //
		};
		final String[] testedPatterns = { // 
		"^DEBUG ([0-9T:\\.\\-]+) \\[([a-zA-Z0-9-_\"]+)\\] spark\\.perf - .*\"request\"[\\u0000-\\uFFFF]+/pages/([^;]+)?(;jsessionid=.*)?\",\"thread\":.*\"endDate\":\"([0-9T\\:\\.\\-]+)\\+[0-9\\:]+\".*\"deltaMs\":([0-9]*).*$" //
		};
		doTestParsing(testedlogs, testedPatterns);
	}

	@Test
	public void testParsing3() throws ParseException {
		final String[] testedlogs = { // 
		"20130918 15:41:39 [AnalyticaSpoolProcessThread] INFO  FILELOG-AGENT - [{\"type\":\"REQUETE\",\"names\":[\"home/login.jsf\"],\"startDate\":\"Aug 29, 2013 6:29:59 PM\",\"measures\":{\"sub-duration\":10939.0,\"duration\":11031.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:29:59 PM\",\"measures\":{\"duration\":1.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]},{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:29:59 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]},{\"type\":\"JSF\",\"names\":[\"RESTORE_VIEW 1\"],\"startDate\":\"Aug 29, 2013 6:29:59 PM\",\"measures\":{\"duration\":8955.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]},{\"type\":\"JSF\",\"names\":[\"RENDER_RESPONSE 6\"],\"startDate\":\"Aug 29, 2013 6:30:08 PM\",\"measures\":{\"sub-duration\":1982.0,\"duration\":1983.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"RENDERRESPONSEPHASE\",\"names\":[\"execute\"],\"startDate\":\"Aug 29, 2013 6:30:08 PM\",\"measures\":{\"sub-duration\":1981.0,\"duration\":1982.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"FACELETVIEWHANDLINGSTRATEGY\",\"names\":[\"buildView\"],\"startDate\":\"Aug 29, 2013 6:30:08 PM\",\"measures\":{\"duration\":1097.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]},{\"type\":\"FACELETVIEWHANDLINGSTRATEGY\",\"names\":[\"renderView\"],\"startDate\":\"Aug 29, 2013 6:30:09 PM\",\"measures\":{\"duration\":884.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]}]}]}]},{\"type\":\"REQUETE_RESOURCE\",\"names\":[\"primefaces.js.jsf;jsessionid\u003d82FC81D1CF7D2493983859B1033A67BD\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"sub-duration\":2.0,\"duration\":36.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"duration\":2.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]},{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]}]},{\"type\":\"REQUETE_RESOURCE\",\"names\":[\"jquery/jquery.js.jsf;jsessionid\u003d82FC81D1CF7D2493983859B1033A67BD\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"sub-duration\":0.0,\"duration\":55.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]},{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]}]},{\"type\":\"REQUETE_RESOURCE\",\"names\":[\"primefaces.css.jsf;jsessionid\u003d82FC81D1CF7D2493983859B1033A67BD\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"sub-duration\":0.0,\"duration\":103.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]},{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]}]},{\"type\":\"REQUETE_RESOURCE\",\"names\":[\"theme.css.jsf\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"sub-duration\":1.0,\"duration\":200.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"duration\":1.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]},{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]}]},{\"type\":\"REQUETE_RESOURCE\",\"names\":[\"general/logo_new.png.jsf\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"sub-duration\":0.0,\"duration\":11.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]},{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:10 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]}]},{\"type\":\"REQUETE_RESOURCE\",\"names\":[\"images/sep_header_param.gif.jsf\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"sub-duration\":0.0,\"duration\":27.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]},{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]}]},{\"type\":\"REQUETE_RESOURCE\",\"names\":[\"images/arrow_header_param.png.jsf\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"sub-duration\":1.0,\"duration\":27.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"duration\":1.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]},{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]}]},{\"type\":\"REQUETE_RESOURCE\",\"names\":[\"login/loginbox.png.jsf\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"sub-duration\":0.0,\"duration\":25.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]},{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]}]},{\"type\":\"REQUETE_RESOURCE\",\"names\":[\"images/bg_header.jpg.jsf\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"sub-duration\":13.0,\"duration\":29.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"duration\":13.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]},{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]}]},{\"type\":\"REQUETE_RESOURCE\",\"names\":[\"images/bg_header_param.jpg.jsf\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"sub-duration\":1.0,\"duration\":40.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"sub-duration\":1.0,\"duration\":1.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"duration\":1.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]}]}]},{\"type\":\"REQUETE_RESOURCE\",\"names\":[\"login/bg_login.jpg.jsf\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"sub-duration\":3.0,\"duration\":39.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"duration\":3.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]},{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"duration\":0.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]}]},{\"type\":\"REQUETE_RESOURCE\",\"names\":[\"images/puce_lien.png.jsf\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"sub-duration\":1.0,\"duration\":19.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"sub-duration\":1.0,\"duration\":1.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"duration\":1.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]}]}]},{\"type\":\"REQUETE_RESOURCE\",\"names\":[\"login/bt_connexion.png.jsf\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"sub-duration\":1.0,\"duration\":26.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"sub-duration\":1.0,\"duration\":1.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[{\"type\":\"SERVICE\",\"names\":[\"LoginService.autoLogin\"],\"startDate\":\"Aug 29, 2013 6:30:11 PM\",\"measures\":{\"duration\":1.0,\"ERROR_PCT\":0.0},\"metaDatas\":{},\"subProcesses\":[]}]}]}]  ", //
		};
		final String[] testedPatterns = { // 
		"^.*\\[([a-zA-Z0-9-_]+)\\] INFO  FILELOG-AGENT - (\\[.*\\]) *$" //
		};
		doTestParsing(testedlogs, testedPatterns);
	}

	private void doTestParsing(final String[] testedlogs, final String[] testedPatterns) {
		final List<Pattern> patterns = new ArrayList<Pattern>(testedPatterns.length);

		for (final String testedPattern : testedPatterns) {
			patterns.add(Pattern.compile(testedPattern));
		}

		for (final String testedlog : testedlogs) {
			for (final Pattern pattern : patterns) {
				final Matcher matcher = pattern.matcher(testedlog);
				if (matcher.find()) {
					System.out.println("Found Log " + testedlog + "\n in : " + pattern);
					for (int i = 0; i <= matcher.groupCount(); i++) {
						System.out.println(i + " = " + matcher.group(i));
					}
				} else {
					System.out.println("NOT Found Log " + testedlog + "\n in : " + pattern);
				}
			}
		}
	}

	@Test
	public void testConf() {
		final String[] dateFormats = { "HH:mm:ss.SSS", "MMM d, yyyy h:mm:ss a" };
		final LogPattern[] PATTERNS = { //
		new LogPattern("REQUETE", "^INFO  ([0-9:\\.]+) \\[([a-zA-Z0-9-_]+)\\] spark.ui.web.LogFilter - Sortie de la ([\\u0000-\\uFFFF]+) [\\u0000-\\uFFFF]+/pages/([\\u0000-\\uFFFF]+) in ([0-9]+)ms$", //
				false, -1, 1, 2, -1, 4, 5, true, false), //
				new LogPattern("REQUETE_RESOURCE", "^INFO  ([0-9:\\.]+) \\[([a-zA-Z0-9-_]+)\\] spark.ui.web.LogFilter - Sortie de la ([\\u0000-\\uFFFF]+) [\\u0000-\\uFFFF]+/javax\\.faces\\.resource/([\\u0000-\\uFFFF]+) in ([0-9]+)ms$", //
						false, -1, 1, 2, -1, 4, 5, true, false), //
				new LogPattern("PERF", "^INFO  ([0-9:\\.]+) \\[([a-zA-Z0-9-]+)\\] Performance - >> ([a-zA-Z0-9]+) : ([a-zA-Z0-9-_]+) : time = ([0-9]+)$", //
						false, -1, 1, 2, 3, 4, 5, false, false),//
				new LogPattern("RELOAD", "^([\\u0000-\\uFFFF]+) org.apache.catalina.startup.Catalina load$", //
						false, -1, 1, -1, -1, -1, -1, false, true),//
		};

		final LogSpyConf conf = new LogSpyConf(Arrays.asList(dateFormats), Arrays.asList(PATTERNS));
		System.out.println(new Gson().toJson(conf));

	}

	private HMetric getMetricInDateCube(final Date date, final String metricName, final String type, final String... subTypes) {
		final HQuery query = serverManager.createQueryBuilder() //
				.on(HTimeDimension.Day).from(date).to(new Date(date.getTime() + 24 * 60 * 60 * 1000)) //
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

	private void checkMetricCount(final Date date, final String metricName, final long countExpected, final String type, final String... subTypes) {
		final HCategory category = new HCategory(type, subTypes);
		final HMetric metric = getMetricInDateCube(date, metricName, type, subTypes);
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
