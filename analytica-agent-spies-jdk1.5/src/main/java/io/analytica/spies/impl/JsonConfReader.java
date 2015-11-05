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
package io.analytica.spies.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;

/**
 * jdk 1.5 implemetation of JsonConfReader. For jdk 7 or above look into the project analytica-agent-spies.
 * Util class for reading a json configuration fileS 
 * @author npiedeloup
 */
public final class JsonConfReader {

	/**
	 * Loading the cofuration.
	 * @param confFileUrl  
	 * @param confClass 
	 * @return Configuration container
	 */
	public static <D extends Object> D loadJsonConf(final URL confFileUrl, final Class<D> confClass) {
		try {
			final String confJson = readConf(confFileUrl);
			final String escapedConfJson = escapeComments(confJson);
			final String cleanConfJson = cleanEmptyElement(escapedConfJson);
			final D conf = new Gson().fromJson(cleanConfJson, confClass);
			return conf;
		} catch (final Exception e) {
			throw new IllegalArgumentException("Unable to load the configuration file : " + confFileUrl, e);
		}
	}

	/**
	 * Loading the cofuration.
	 * @param configurationFileName
	 * @param confClass 
	 * @return Configuration container
	 */
	public static final <D extends Object> D loadJsonConf(final String configurationFileName, final Class<D> confClass) {
		try {
			return JsonConfReader.loadJsonConf(new File(configurationFileName).toURI().toURL(), confClass);
		} catch (final MalformedURLException e) {
			throw new RuntimeException("Erreur de lecture de la conf", e);
		}
	}

	private static String escapeComments(final String confJson) {
		String escapedConfJson = confJson.replaceAll("//.*", ""); // type //
		escapedConfJson = escapedConfJson.replaceAll("/\\*(.|[\\r\\n])*?\\*/", ""); //type /* */
		return escapedConfJson;
	}

	private static String cleanEmptyElement(final String confJson) {
		final String escapedConfJson = confJson.replaceAll(",[\\s]*([\\]\\}])", "$1"); //, <spaces> } or ]
		return escapedConfJson;
	}

	private static String readConf(final URL url) {
		final StringBuilder sb = new StringBuilder();
		try {
			final InputStream in = url.openStream();
			try {
				final Reader isr = new InputStreamReader(in);
				try {
					final BufferedReader br = new BufferedReader(isr);
					try {
						String currentLine;
						while ((currentLine = br.readLine()) != null) {
							sb.append(currentLine).append('\n');
						}
					} finally {
						br.close();
					}
				} finally {
					isr.close();
				}
			} finally {
				in.close();
			}
		} catch (final IOException e) {
			throw new RuntimeException("Error while reading the configuration file", e);
		}
		return sb.toString();
	}
}
