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
 * Utilitaire de lecture de fichier de conf JSon.
 * @author npiedeloup
 */
public final class JsonConfReader {

	/**
	 * Chargement d'une configuration.
	 * @param confFileUrl URL du fichier de conf
	 * @param confClass Class de l'objet portant la conf
	 * @return Instance de la configuration chargée à partir du fichier
	 */
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

	/**
	 * Chargement d'une configuration.
	 * @param configurationFileName Chemin du fichier de conf
	 * @param confClass Class de l'objet portant la conf
	 * @return Instance de la configuration chargée à partir du fichier
	 */
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
