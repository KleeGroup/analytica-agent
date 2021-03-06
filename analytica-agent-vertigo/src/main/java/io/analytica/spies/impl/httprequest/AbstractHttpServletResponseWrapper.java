/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidi�re - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.analytica.spies.impl.httprequest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Impl�mentation de HttpServletResponseWrapper pour �viter warnings � la compilation.
 * @author Emeric Vernat
 * @version $Id: AbstractHttpServletResponseWrapper.java,v 1.1 2012/10/23 15:30:29 pchretien Exp $
 */
public abstract class AbstractHttpServletResponseWrapper extends javax.servlet.http.HttpServletResponseWrapper {
	private ServletOutputStream stream;
	private PrintWriter writer;
	private final HttpServletResponse response;
	private int status;

	/**
	 * Constructeur.
	 * @param response javax.servlet.HttpServletResponse
	 */
	protected AbstractHttpServletResponseWrapper(final HttpServletResponse response) {
		super(response);
		this.response = response;
	}

	/**
	 * @return Flux de sortie
	 */
	protected final ServletOutputStream getStream() {
		return stream;
	}

	/**
	 * @throws IOException Erreur de fermeture
	 */
	protected final void close() throws IOException {
		if (writer != null) {
			writer.close();
		} else if (stream != null) {
			stream.close();
		}
	}

	/**
	 * Surcharge de addHeader pour fixer le header m�me si la r�ponse est incluse (contrairement � tomcat).
	 * @param name String
	 * @param value String
	 */
	@Override
	public final void addHeader(final String name, final String value) {
		// n�cessaire pour header gzip du filtre de compression
		response.addHeader(name, value);
	}

	/**
	 * Surcharge de setHeader pour fixer le header m�me si la r�ponse est incluse (contrairement � tomcat).
	 * @param name String
	 * @param value String
	 */
	@Override
	public final void setHeader(final String name, final String value) {
		response.setHeader(name, value);
	}

	/**
	 * Retourne le status d�finit par setStatus ou sendError.
	 * @return int
	 */
	public final int getStatus() {
		return status;
	}

	/**
	 * D�finit le status de la r�ponse http (SC_OK, SC_NOT_FOUND, SC_INTERNAL_SERVER_ERROR ...).
	 * @param status int
	 */
	@Override
	public final void setStatus(final int status) {
		super.setStatus(status);
		this.status = status;
	}

	/**
	 * Envoie une erreur comme r�ponse http (SC_OK, SC_NOT_FOUND, SC_INTERNAL_SERVER_ERROR ...).
	 * @param error int
	 * @throws IOException   Exception d'entr�e/sortie
	 */
	@Override
	public final void sendError(final int error) throws IOException {
		super.sendError(error);
		status = error;
	}

	/**
	 * Cr�e et retourne un ServletOutputStream pour �crire le contenu dans la response associ�e.
	 * @return javax.servlet.ServletOutputStream
	 * @throws java.io.IOException   Erreur d'entr�e/sortie
	 */
	public abstract ServletOutputStream createOutputStream() throws IOException;

	/**
	 * Retourne le servlet output stream associ� avec cette response.
	 * @return javax.servlet.ServletOutputStream
	 * @throws java.io.IOException   Erreur d'entr�e/sortie
	 */
	@Override
	public final ServletOutputStream getOutputStream() throws IOException {
		if (writer != null) {
			throw new IllegalStateException("getWriter() has already been called for this response");
		}

		if (stream == null) {
			stream = createOutputStream();
		}
		return stream;
	}

	/**
	 * Retourne le writer associ� avec cette response.
	 * @return java.io.PrintWriter
	 * @throws java.io.IOException   Erreur d'entr�e/sortie
	 */
	@Override
	public final PrintWriter getWriter() throws IOException {
		if (stream != null) {
			throw new IllegalStateException("getOutputStream() has already been called for this response");
		}
		if (writer == null) {
			final ServletOutputStream outputStream = createOutputStream();
			final String charEnc = getResponse().getCharacterEncoding();
			// HttpServletResponse.getCharacterEncoding() shouldn't return null
			// according the spec, so feel free to remove that "if"
			final PrintWriter result;
			if (charEnc != null) {
				result = new PrintWriter(new OutputStreamWriter(outputStream, charEnc));
			} else {
				result = new PrintWriter(outputStream);
			}
			writer = result;
		}
		return writer;
	}

	/**
	 * Flushe le buffer et commite la response.
	 * @throws java.io.IOException   Erreur d'entr�e/sortie
	 */
	@Override
	public final void flushBuffer() throws IOException {
		if (writer != null) { //NOPMD
			writer.flush();
		} else if (stream != null) {
			stream.flush();
		}
	}

	/**
	 * D�finit la longueur du corps du contenu dans la r�ponse.
	 * Dans les servlets http, cette m�thode d�finit le Content-Length dans les headers HTTP.
	 * @param length int
	 */
	@Override
	public void setContentLength(final int length) {
		getResponse().setContentLength(length);
	}

	/**
	 * D�finit le type du contenu dans la r�ponse.
	 * Dans les servlets http, cette m�thode d�finit le Content-Type dans les headers HTTP.
	 * @param type String
	 */
	@Override
	public final void setContentType(final String type) {
		getResponse().setContentType(type);
	}
}
