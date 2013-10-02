package com.kleegroup.analyticaimpl.spies.logs;

import java.util.Map;
import java.util.Properties;

import kasper.kernel.Home;
import kasper.kernel.di.container.Container;
import kasper.kernel.di.container.DualContainer;
import kasper.kernel.di.container.ParamsContainer;
import kasper.kernel.di.injector.Injector;
import kasper.kernel.lang.Option;
import kasper.kernel.util.Assertion;

/**
 * Parser de fichier de log et injection dans Analytica.
 * @author npiedeloup
 * @version $Id: CounterProxy.java,v 1.5 2010/11/23 09:49:33 npiedeloup Exp $
 */
public final class LogSpyStandaloneParser {

	private static final Injector INJECTOR = new Injector();

	/**
	 * Lance l'environnement et attend indéfiniment.
	 * @param args "Usage: java kasper.kernel.Starter managers.xml <conf.properties>"
	 */
	public static void main(final String[] args) {
		final String usageMsg = "Usage: java com.kleegroup.analyticaimpl.spies.logs.LogSpyStandaloneParser \"http://analyticaServer:port/analytica/rest/process\" confPattern.json logFile.out";
		Assertion.precondition(args.length == 3, usageMsg + " (nombre d'arguments incorrect : " + args.length + ")");
		Assertion.precondition(args[0].contains("http://"), usageMsg + " (" + args[0] + ")");
		Assertion.precondition(args[1].endsWith(".json"), usageMsg + " (" + args[1] + ")");
		//---------------------------------------------------------------------
		final String managersXmlFileName = "./managers.xml";
		final Option<String> propertiesFileName = args.length == 2 ? Option.<String> some(args[1]) : Option.<String> none();
		final Properties defaultProperties = new Properties();
		defaultProperties.setProperty("serverUrl", args[0]);
		defaultProperties.setProperty("confFileUrl", args[1]);
		defaultProperties.setProperty("logFileUrl", args[2]);

		final Starter starter = new Starter(managersXmlFileName, propertiesFileName, LogSpyStandaloneParser.class, Option.some(defaultProperties), 0);
		starter.start();
		try {
			final Container container = new DualContainer(Home.getContainer().getRootContainer(), new ParamsContainer((Map) defaultProperties));
			final LogSpyReader logSpyReader = INJECTOR.newInstance(LogSpyReader.class, container);
			try {
				logSpyReader.start();
			} finally {
				flushAgentToServer();
			}
		} finally {
			starter.stop();
		}
	}

	private static void flushAgentToServer() {
		try {
			Thread.sleep(2000);//on attend 2s que les processes soit envoyés au serveur.
		} catch (final InterruptedException e) {
			//rien on stop juste l'attente
		}
	}

}
