package com.kleegroup.analyticaimpl.spies.httprequest;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Impl�mentation de ServletOutputStream qui fonctionne avec le HttpRequestSpy.
 * @author npiedeloup
 * @version $Id: CounterResponseStream.java,v 1.1 2010/02/11 15:35:39 pchretien Exp $
 */
final class HttpRequestSpyResponseStream extends ServletOutputStream {
	private final ServletOutputStream output;
	private int dataLength; //implicite = 0;

	/**
	 * Construit un servlet output stream associ� avec la r�ponse sp�cifi�e.
	 * @param response javax.servlet.http.HttpServletResponse
	 * @throws java.io.IOException   Erreur d'entr�e/sortie
	 */
	HttpRequestSpyResponseStream(final HttpServletResponse response) throws IOException {
		super();
		output = response.getOutputStream();
	}

	/**
	 * Retourne la valeur de la propri�t� dataLength.
	 * @return int
	 */
	public int getDataLength() {
		return dataLength;
	}

	/**
	 * Ferme cet output stream (et flushe les donn�es bufferis�es).
	 * @throws java.io.IOException   Erreur d'entr�e/sortie
	 */
	@Override
	public void close() throws IOException {
		output.close();
	}

	/**
	 * Flushe les donn�es bufferis�es de cet output stream.
	 * @throws java.io.IOException   Erreur d'entr�e/sortie
	 */
	@Override
	public void flush() throws IOException {
		output.flush();
	}

	/**
	 * Ecrit l'octet sp�cifi� dans l'output stream
	 * @param i int
	 * @throws java.io.IOException   Erreur d'entr�e/sortie
	 */
	@Override
	public void write(final int i) throws IOException {
		output.write(i);
		dataLength += 1;
	}

	/**
	 * Ecrit les octets sp�cifi�s dans l'output stream.
	 * @param bytes bytes[]
	 * @throws java.io.IOException   Erreur d'entr�e/sortie
	 */
	@Override
	public void write(final byte[] bytes) throws IOException {
		output.write(bytes);
		final int len = bytes.length;
		dataLength += len;
	}

	/**
	 * Ecrit <code>len</code> octets du tableau d'octets sp�cifi�s, en commen�ant � la position sp�cifi�e,
	 * dans l'output stream.
	 * @param bytes bytes[]
	 * @param off int
	 * @param len int
	 * @throws java.io.IOException   Erreur d'entr�e/sortie
	 */
	@Override
	public void write(final byte[] bytes, final int off, final int len) throws IOException {
		output.write(bytes, off, len);
		dataLength += len;
	}
}
