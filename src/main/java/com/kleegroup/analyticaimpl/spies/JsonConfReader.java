package com.kleegroup.analyticaimpl.spies;

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
 * Implémentation de ClassFileTransformer pour instrumenter les méthodes.
 * Necessite d'être placé dans un jar <b>avec</b> les class de Javassist, car il n'y a pas de gestion de classpath dans le javaagent
 * @author npiedeloup
 * @version $Id: MemoryLeakTransformer.java,v 1.1 2011/05/12 10:16:05 prahmoune Exp $
 */
public final class JsonConfReader {

	public static <D extends Object> D loadJsonConf(final URL confFileUrl, final Class<D> confClass) {
		try {
			final String confJson = readConf(confFileUrl);
			final String escapedConfJson = escapeComments(confJson);
			final String cleanConfJson = cleanEmptyElement(escapedConfJson);
			final D conf = new Gson().fromJson(cleanConfJson, confClass);
			return conf;
		} catch (final Exception e) {
			throw new IllegalArgumentException("Impossible de charger le fichier de configuration : " + confFileUrl, e);
		}
	}

	public static final <D extends Object> D loadJsonConf(final String configurationFileName, final Class<D> confClass) {
		try {
			return JsonConfReader.loadJsonConf(new File(configurationFileName).toURI().toURL(), confClass);
		} catch (final MalformedURLException e) {
			throw new RuntimeException("Erreur de lecture de la conf", e);
		}
	}

	private static String escapeComments(final String confJson) {
		String escapedConfJson = confJson.replaceAll("//.*", ""); //comment de type //
		escapedConfJson = escapedConfJson.replaceAll("/\\*(.|[\\r\\n])*?\\*/", ""); //comment de type /* */
		return escapedConfJson;
	}

	private static String cleanEmptyElement(final String confJson) {
		final String escapedConfJson = confJson.replaceAll(",[\\s]*([\\]\\}])", "$1"); //, <spaces> } ou ]
		return escapedConfJson;
	}

	private static String readConf(final URL url) {
		final StringBuilder sb = new StringBuilder();
		try {
			//on lit le fichier
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
			throw new RuntimeException("Erreur de lecture de la conf", e);
		}
		return sb.toString();
	}
}
