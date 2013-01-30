package com.kleegroup.analyticaimpl.spies.httprequest;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Implémentation de ServletOutputStream qui fonctionne avec le HttpRequestSpy.
 * @author npiedeloup
 * @version $Id: CounterResponseStream.java,v 1.1 2010/02/11 15:35:39 pchretien Exp $
 */
final class HttpRequestSpyResponseStream extends ServletOutputStream {
	private final ServletOutputStream output;
	private int dataLength; //implicite = 0;

	/**
	 * Construit un servlet output stream associé avec la réponse spécifiée.
	 * @param response javax.servlet.http.HttpServletResponse
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	HttpRequestSpyResponseStream(final HttpServletResponse response) throws IOException {
		super();
		output = response.getOutputStream();
	}

	/**
	 * Retourne la valeur de la propriété dataLength.
	 * @return int
	 */
	public int getDataLength() {
		return dataLength;
	}

	/**
	 * Ferme cet output stream (et flushe les données bufferisées).
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public void close() throws IOException {
		output.close();
	}

	/**
	 * Flushe les données bufferisées de cet output stream.
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public void flush() throws IOException {
		output.flush();
	}

	/**
	 * Ecrit l'octet spécifié dans l'output stream
	 * @param i int
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public void write(final int i) throws IOException {
		output.write(i);
		dataLength += 1;
	}

	/**
	 * Ecrit les octets spécifiés dans l'output stream.
	 * @param bytes bytes[]
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public void write(final byte[] bytes) throws IOException {
		output.write(bytes);
		final int len = bytes.length;
		dataLength += len;
	}

	/**
	 * Ecrit <code>len</code> octets du tableau d'octets spécifiés, en commençant à la position spécifiée,
	 * dans l'output stream.
	 * @param bytes bytes[]
	 * @param off int
	 * @param len int
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public void write(final byte[] bytes, final int off, final int len) throws IOException {
		output.write(bytes, off, len);
		dataLength += len;
	}
}
