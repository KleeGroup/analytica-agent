package com.kleegroup.analyticaimpl.spies.httprequest;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Impl�mentation de HttpServletResponseWrapper qui fonctionne avec le HttpRequestSpy.
 * @author npiedeloup
 * @version $Id: CounterServletResponseWrapper.java,v 1.1 2010/02/11 15:35:39 pchretien Exp $
 */
final class HttpRequestSpyServletResponseWrapper extends AbstractHttpServletResponseWrapper {
	/**
	 * Constructeur qui cr�e un adapteur de ServletResponse wrappant la response sp�cifi�e.
	 * @param response javax.servlet.HttpServletResponse
	 */
	HttpRequestSpyServletResponseWrapper(final HttpServletResponse response) {
		super(response);
	}

	/**
	 * Retourne la valeur de la propri�t� dataLength.
	 * @return int
	 */
	public long getDataLength() {
		return getStream() == null ? 0 : ((HttpRequestSpyResponseStream) getStream()).getDataLength();
	}

	/**
	 * Cr�e et retourne un ServletOutputStream pour �crire le contenu dans la response associ�e.
	 * @return javax.servlet.ServletOutputStream
	 * @throws java.io.IOException   Erreur d'entr�e/sortie
	 */
	@Override
	public ServletOutputStream createOutputStream() throws IOException {
		return new HttpRequestSpyResponseStream((HttpServletResponse) getResponse());
	}
}
