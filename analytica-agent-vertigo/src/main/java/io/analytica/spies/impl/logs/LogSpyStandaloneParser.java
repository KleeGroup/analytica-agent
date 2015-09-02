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
package io.analytica.spies.impl.logs;

import io.analytica.agent.AgentManager;
import io.analytica.agent.Starter;
import io.vertigo.core.Home;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.Properties;

/**
 * Parser de fichier de log et injection dans Analytica.
 * @author npiedeloup
 * @version $Id: CounterProxy.java,v 1.5 2010/11/23 09:49:33 npiedeloup Exp $
 */
public final class LogSpyStandaloneParser {

	/**
	 * Lance l'environnement et attend indéfiniment.
	 * @param args "Usage: java kasper.kernel.Starter managers.xml <conf.properties>"
	 */
	public static void main(final String[] args) {
		final String usageMsg = "Usage: java io.analytica.spies.impl.logs.LogSpyStandaloneParser \"http://analyticaServer:port/analytica/rest/process\" confPattern.json logFile.out";
		Assertion.checkArgument(args.length == 3, usageMsg + " (nombre d'arguments incorrect : " + args.length + ")");
		Assertion.checkArgument(args[0].contains("http://"), usageMsg + " (" + args[0] + ")");
		Assertion.checkArgument(args[1].endsWith(".json"), usageMsg + " (" + args[1] + ")");
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
			//final Container container = new DualContainer(Home.getComponentSpace(), new ParamsContainer((Map) defaultProperties));
			final AgentManager agentManager = Home.getComponentSpace().resolve(AgentManager.class);
			final ResourceManager resourceManager = Home.getComponentSpace().resolve(ResourceManager.class);
			final String logFileUrl = defaultProperties.getProperty("logFileUrl");
			final String confFileUrl = defaultProperties.getProperty("confFileUrl");
			final LogSpyReader logSpyReader = new LogSpyReader(agentManager, resourceManager, logFileUrl, confFileUrl);
			//INJECTOR.newInstance(LogSpyReader.class, Home.getComponentSpace());
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
