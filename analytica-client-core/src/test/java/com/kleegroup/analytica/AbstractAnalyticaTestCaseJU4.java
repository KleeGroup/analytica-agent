package com.kleegroup.analytica;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.kleegroup.analyticamock.MockServerManager;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 * Charge l'environnement de test par defaut.
 * @author pchretien
 */
public abstract class AbstractAnalyticaTestCaseJU4 {

	private MockServerManager mockServerManager;
	private HttpServer httpServer;

	private final URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/").port(9998).build();
	}

	public final URI baseUri = getBaseURI();

	protected final void startServer() throws IOException {
		System.out.println("Starting grizzly...");
		mockServerManager = new MockServerManager();
		final ResourceConfig rc = new PackagesResourceConfig("com.kleegroup.analyticamock");
		rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, com.sun.jersey.api.container.filter.GZIPContentEncodingFilter.class.getName());
		rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, com.sun.jersey.api.container.filter.GZIPContentEncodingFilter.class.getName());
		httpServer = GrizzlyServerFactory.createHttpServer(baseUri, rc);
		System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl", baseUri));

	}

	protected void flushAgentToServer() {
		try {
			Thread.sleep(5000);//on attend 2s que le process soit envoyé au serveur.
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
		doTearDown();
		if (httpServer != null) {
			httpServer.stop();
		}
		mockServerManager = null;
		doAfterTearDown();
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
