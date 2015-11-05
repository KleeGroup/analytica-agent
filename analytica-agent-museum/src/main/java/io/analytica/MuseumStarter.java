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
package io.analytica;

import io.analytica.agent.impl.net.RemoteConnector;
import io.analytica.api.Assertion;
import io.analytica.api.KProcess;
import io.analytica.museum.Museum;
import io.analytica.museum.PageListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author pchretien, npiedeloup
 */
public final class MuseumStarter {

	public MuseumStarter() {
	}

	/**
	 * Charging the properties file
	 */
	private static final void appendFileProperties(final Properties properties, final String propertiesFileName) {
		//---------------------------------------------------------------------
		try (final InputStream in = new File(propertiesFileName).toURI().toURL().openStream()) {
			properties.load(in);
		} catch (final IOException e) {
			throw new IllegalArgumentException("Unable to charge the properties file : " + propertiesFileName, e);
		}
	}

	public static void main(final String[] args) {
		final String usageMsg = "Usage: java io.analytica.MuseumStarter <conf.properties>";
		Assertion.checkArgument(args.length == 1, usageMsg);
		Assertion.checkArgument(args[0].endsWith(".properties"), usageMsg + " ( waiting for a .properties extention. found : " + args[0] + ")");
		//---------------------------------------------------------------------
		final String propertiesFileName = args[0];
		final Properties properties = new Properties();
		appendFileProperties(properties, propertiesFileName);
		final RemoteConnector remoteConnector = new RemoteConnector(properties.getProperty("serverURL"),//
				Integer.parseInt(properties.getProperty("sendPaquetSize")),//
				Integer.parseInt(properties.getProperty("sendPaquetFrequencySeconds")));
		remoteConnector.start();
		final int days = Integer.parseInt(properties.getProperty("days"));
		final int visitsByDay = Integer.parseInt(properties.getProperty("visitsByDay"));
		Museum museum= new Museum(new PageListener() {
			@Override
			public void onPage(final KProcess process) {
				remoteConnector.add(process);
			}
		});
//		museum.load(days, visitsByDay);
		try {
			museum.constantLoad(visitsByDay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
}
