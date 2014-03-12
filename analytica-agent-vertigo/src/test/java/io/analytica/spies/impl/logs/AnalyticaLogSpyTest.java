package io.analytica.spies.impl.logs;

import io.analytica.AbstractVertigoStartTestCaseJU4;
import io.analytica.agent.AgentManager;
import io.analytica.spies.impl.logs.LogPattern;
import io.analytica.spies.impl.logs.LogSpyConf;
import io.analytica.spies.impl.logs.LogSpyReader;
import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.kernel.lang.Assertion;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.junit.Test;

import com.google.gson.Gson;

/**
 * Test LogSpyReader.
 * Parse a log file, rebuild Processes and send them to AnalyticaServer.
 *
 * @author npiedeloup
 */
public final class AnalyticaLogSpyTest extends AbstractVertigoStartTestCaseJU4 {

	@Inject
	private AgentManager agentManager;
	@Inject
	private ResourceManager resourceManager;

	protected void doSetUp() throws Exception {
		startServer();
	}
	
	/**
	 * Test simple avec deux compteurs. 
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg. 
	 * Chaque article coute 10€.
	 * @throws ParseException 
	 */
	@Test
	public void testMiniLog() throws ParseException {
		final LogSpyReader logSpyReader = new LogSpyReader(agentManager, resourceManager, "file:///d:\\@GitHub\\analytica-agent-spark\\spark-130614\\tomcat7_spark-stdout.2013-06-05.log", "file:///d:/@GitHub/analytica-agent/spark-130614/sparkLogSpyConf-v1.1.json");
		logSpyReader.start();

		flushAgentToServer();
		checkMetricCount("duration", 7350, "REQUETE");
	}

	@Test
	public void testLog130809() throws ParseException {
		final LogSpyReader logSpyReader = new LogSpyReader(agentManager, resourceManager, "file:///d:\\@GitHub\\analytica-agent-spark\\spark-130614\\spark-130809-bis.perf.log ", "file:///d:/@GitHub/analytica-agent/spark-130614/sparkLogSpyConf-v1.2.json");
		logSpyReader.start();

		flushAgentToServer();
		//final Date date = new SimpleDateFormat("ddMMyyyy").parse("30052013");
		//checkMetricCount(date, "duration", 7350, "REQUETE");
	}

	@Test
	public void testLog130822() throws ParseException {
		final LogSpyReader logSpyReader = new LogSpyReader(agentManager, resourceManager, "file:///d:\\@GitHub\\analytica-agent-spark\\spark-130614\\spark-130822.perf.log ", "file:///d:/@GitHub/analytica-agent/spark-130614/sparkLogSpyConf-v1.2.json");
		logSpyReader.start();

		flushAgentToServer();
		//final Date date = new SimpleDateFormat("ddMMyyyy").parse("30052013");
		//checkMetricCount(date, "duration", 7350, "REQUETE");
	}

	@Test
	public void testLog130829() throws ParseException {
		final LogSpyReader logSpyReader = new LogSpyReader(agentManager, resourceManager, "file:///d:\\@GitHub\\analytica-agent-spark\\spark-130829\\all.log", "file:///d:/@GitHub/analytica-agent-spark/spark-130829/sparkLogSpyConf-v1.3.5.json");
		logSpyReader.start();

		flushAgentToServer();
		checkMetricCount("duration", 510, "REQUETE");
	}

	@Test
	public void testLog130829FromJson() throws ParseException {
		final LogSpyReader logSpyReader = new LogSpyReader(agentManager, resourceManager, "file:///d:\\logs\\spark130829FileLog.log", "io/analytica/spies/impl/logs/logSpyConf.json");
		logSpyReader.start();

		flushAgentToServer();
		checkMetricCount("duration", 510, "REQUETE");
	}

	@Test
	public void testLog131017FromJson() throws ParseException, IOException {
		startServer();
		final LogSpyReader logSpyReader = new LogSpyReader(agentManager, resourceManager, "file:///d:\\@GitHub\\analytica-agent-spark\\spark-131017\\analyticaFileLog.log", "file:///d:\\@GitHub/analytica-agent/analytica-client/src/test/java/com/kleegroup/analyticaimpl/spies/logs/logSpyConf.json");
		logSpyReader.start();

		flushAgentToServer();
		checkMetricCount("duration", 510, "REQUETE");
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

	/*private static final String translateFileName(final String fileName, final Class<?> relativeRootClass) {
		Assertion.checkArgNotEmpty(fileName);
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
	}*/
}
