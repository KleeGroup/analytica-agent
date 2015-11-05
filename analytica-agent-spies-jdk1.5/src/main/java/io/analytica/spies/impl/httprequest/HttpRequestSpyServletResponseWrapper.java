/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.analytica.spies.impl.httprequest;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementation de HttpServletResponseWrapper qui fonctionne avec le HttpRequestSpy.
 * @author npiedeloup
 * @version $Id: CounterServletResponseWrapper.java,v 1.1 2010/02/11 15:35:39 pchretien Exp $
 */
final class HttpRequestSpyServletResponseWrapper extends AbstractHttpServletResponseWrapper {
	/**
	 * Constructeur qui cree un adapteur de ServletResponse wrappant la response specifiee.
	 * @param response javax.servlet.HttpServletResponse
	 */
	HttpRequestSpyServletResponseWrapper(final HttpServletResponse response) {
		super(response);
	}

	/**
	 * Retourne la valeur de la propriete dataLength.
	 * @return int
	 */
	public long getDataLength() {
		return getStream() == null ? 0 : ((HttpRequestSpyResponseStream) getStream()).getDataLength();
	}

	/**
	 * Cree et retourne un ServletOutputStream pour ecrire le contenu dans la response associee.
	 * @return javax.servlet.ServletOutputStream
	 * @throws java.io.IOException   Erreur d'entree/sortie
	 */
	@Override
	public ServletOutputStream createOutputStream() throws IOException {
		return new HttpRequestSpyResponseStream((HttpServletResponse) getResponse());
	}
}
