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
package io.analytica;

import io.analytica.mock.MockServerManager;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 * Abstract Test class for Analytica.
 * Start a MockAnalyticaServer to receive and check process send.
 * @author pchretien, npiedeloup
 */
public abstract class AbstractAnalyticaTestCaseJU4 {

	//	private MockServerManager mockServerManager;
	private HttpServer httpServer;

	private final URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/").port(9998).build();
	}

	public final URI baseUri = getBaseURI();

	protected final void startServer() throws IOException {
		System.out.println("Starting grizzly...");
		//	mockServerManager = new MockServerManager();
		final ResourceConfig rc = new PackagesResourceConfig("io.analytica.mock");
		rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, com.sun.jersey.api.container.filter.GZIPContentEncodingFilter.class.getName());
		rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, com.sun.jersey.api.container.filter.GZIPContentEncodingFilter.class.getName());
		httpServer = GrizzlyServerFactory.createHttpServer(baseUri, rc);
		System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl", baseUri));

	}

	protected void flushAgentToServer() {
		try {
			Thread.sleep(2000);//on attend 2s que le process soit envoyé au serveur.
		} catch (final InterruptedException e) {
			//rien on stop juste l'attente
		}
	}

	protected final void nop(final Object o) {
		//rien
	}

	@Before
	public final void setUp() throws Exception {
		doSetUp();
	}

	@After
	public final void tearDown() throws Exception {
		try {
			doTearDown();
		} finally {
			if (httpServer != null) {
				httpServer.stop();
			}
			//	mockServerManager = null;
			doAfterTearDown();
		}
	}

	/**
	 * Initialisation du test pour implé spécifique.
	 * @throws Exception Erreur
	 */
	protected void doSetUp() throws Exception {
		// pour implé spécifique 
	}

	/**
	 * Finalisation du test pour implé spécifique.
	 * @throws Exception Erreur
	 */
	protected void doTearDown() throws Exception {
		// pour implé spécifique 
	}

	/**
	 * Finalisation du test pour implé spécifique.
	 * @throws Exception Erreur
	 */
	protected void doAfterTearDown() throws Exception {
		// pour implé spécifique 
	}

	protected void checkMetricCount(final String metricName, final long countExpected, final String type, final String... subTypes) {
		final List<Double> measures = MockServerManager.getInstance().getMeasures(metricName, type, subTypes);
		Assert.assertEquals("Le cube " + toString(type, subTypes) + " n'est pas peuplé correctement", countExpected, measures.size(), 0);
		System.out.println("Cube OK :" + type + " metric " + metricName);
	}

	protected void checkMetricMean(final String metricName, final double meanExpected, final String type, final String... subTypes) {
		final List<Double> measures = MockServerManager.getInstance().getMeasures(metricName, type, subTypes);
		double sum = 0;
		for (final Double value : measures) {
			sum += value;
		}
		Assert.assertNotEquals("Le cube " + toString(type, subTypes) + " n'est pas peuplé correctement (0 mesures)", 0, measures.size());
		Assert.assertEquals("Le cube " + toString(type, subTypes) + " n'est pas peuplé correctement", meanExpected, sum / measures.size(), 0.001);
	}

	private String toString(final String type, final String[] subTypes) {
		final StringBuilder sb = new StringBuilder("[");
		sb.append(type);
		for (final String subType : subTypes) {
			sb.append(",");
			sb.append(subType);
		}
		sb.append("]");
		return sb.toString();
	}
}
