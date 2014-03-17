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
package io.analytica.spies.impl.httprequest;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Implémentation de HttpServletResponseWrapper qui fonctionne avec le HttpRequestSpy.
 * @author npiedeloup
 * @version $Id: CounterServletResponseWrapper.java,v 1.1 2010/02/11 15:35:39 pchretien Exp $
 */
final class HttpRequestSpyServletResponseWrapper extends AbstractHttpServletResponseWrapper {
	/**
	 * Constructeur qui crée un adapteur de ServletResponse wrappant la response spécifiée.
	 * @param response javax.servlet.HttpServletResponse
	 */
	HttpRequestSpyServletResponseWrapper(final HttpServletResponse response) {
		super(response);
	}

	/**
	 * Retourne la valeur de la propriété dataLength.
	 * @return int
	 */
	public long getDataLength() {
		return getStream() == null ? 0 : ((HttpRequestSpyResponseStream) getStream()).getDataLength();
	}

	/**
	 * Crée et retourne un ServletOutputStream pour écrire le contenu dans la response associée.
	 * @return javax.servlet.ServletOutputStream
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public ServletOutputStream createOutputStream() throws IOException {
		return new HttpRequestSpyResponseStream((HttpServletResponse) getResponse());
	}
}
