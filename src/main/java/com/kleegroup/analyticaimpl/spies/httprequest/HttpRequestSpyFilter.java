package com.kleegroup.analyticaimpl.spies.httprequest;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kasper.kernel.Home;
import kasper.kernel.exception.KRuntimeException;
import kasper.kernel.exception.KUserException;

import org.apache.log4j.Logger;

import com.kleegroup.analytica.agent.AgentManager;

/**
 * Filtre de servlet (javax.servlet.Filter).
 * Il permet d'instrumenter les pages avec appels, durées, taille des flux d'entrée et de sortie.
 *
 * @author pchretien, npiedeloup 
 * @version $Id: UIFilter.java,v 1.7 2010/11/16 10:36:54 pchretien Exp $
 */
public final class HttpRequestSpyFilter implements Filter {

	private static final String PT_PAGE = "PAGE";
	private static final String ME_USER_EXCEPTION_PCT = "USER_EXCEPTION_PCT";
	private static final String ME_SYSTEM_EXCEPTION_PCT = "SYSTEM_EXCEPTION_PCT";
	private static final String ME_HTML_SIZE = "HTML_SIZE";

	/**
	 * Mécanisme de log racine
	 */
	private final Logger generalLog = Logger.getRootLogger();

	/**
	 * Mécanisme de log utilisé pour les performances
	 */
	private final Logger performanceLog = Logger.getLogger("Performance");

	/** {@inheritDoc} */
	public void init(final FilterConfig filterConfig) {
		//rien
	}

	/** {@inheritDoc} */
	public void destroy() {
		// Rien de spécial
	}

	/**
	 * La méthode doFilter est appelée par le container chaque fois qu'une paire
	 * requête/réponse passe à travers la chaîne suite à une requête d'un client
	 * pour une ressource au bout de la chaîne. L'instance de FilterChain passée
	 * dans cette méthode permet au filtre de passer la requête et la réponse à
	 * l'entité suivante dans la chaîne.
	 *
	 * Cette implémentation encapsule les flux d'entrée et de sortie (compressés
	 * ou non) pour compter les octets lus ou écrits. Puis elle logue le
	 * résultat avec le nom du filtre (pour distinguer si c'est avant ou après
	 * compression). Le résultat est également enregistré dans les statistiques.
	 *
	 * @param request javax.servlet.ServletRequest
	 * @param response javax.servlet.ServletResponse
	 * @param chain javax.servlet.FilterChain
	 * @throws java.io.IOException Si une erreur d'entrée/sortie survient
	 * @throws javax.servlet.ServletException Si une erreur de servlet survient
	 */
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			filter((HttpServletRequest) request, new HttpRequestSpyServletResponseWrapper((HttpServletResponse) response), chain, getAgentManager());
		} else {
			chain.doFilter(request, response);
		}
	}

	private void filter(final HttpServletRequest httpRequest, final HttpRequestSpyServletResponseWrapper wrappedResponse, final FilterChain chain, final AgentManager agentManager) throws IOException, ServletException {
		final long begin = System.currentTimeMillis();
		boolean ok = false;
		//Démarrage de la requête.
		agentManager.startProcess(PT_PAGE, getRequestName(httpRequest));
		logRequestStart(httpRequest);

		try {
			chain.doFilter(httpRequest, wrappedResponse);
			ok = true;
		} catch (final ServletException servletException) {
			if (isUserException(servletException)) {
				agentManager.setMeasure(ME_USER_EXCEPTION_PCT, 100);
			} else {
				agentManager.setMeasure(ME_SYSTEM_EXCEPTION_PCT, 100);
			}
			logRequestException(servletException);
			throw servletException;
		} finally {
			final long requestTime = System.currentTimeMillis() - begin;
			// Fin de l'instrumentation générique de la servlet
			// Ajout de compteurs spécifiques au projet
			String requestSimpleName = httpRequest.getPathInfo();
			if (requestSimpleName == null) {
				requestSimpleName = httpRequest.getServletPath();
			}
			if (wrappedResponse.getDataLength() != 0) {
				//en cas d'erreur on n'a pas la taille, il ne faut pas la compter
				agentManager.setMeasure(ME_HTML_SIZE, wrappedResponse.getDataLength());
			}
			agentManager.stopProcess();

			logRequestFinish(httpRequest, requestTime, ok);
		}
	}

	//-------------------------------------------------------------------------

	private static boolean isUserException(final Throwable throwable) {
		Throwable e = throwable;
		Boolean userException = null;

		while (e != null) {
			if (userException == null && e instanceof KUserException) {
				userException = Boolean.TRUE;
			}
			if (userException == null && e instanceof KRuntimeException) {
				userException = Boolean.FALSE;
			}

			Throwable t = e.getCause();
			if (t == null && e instanceof ServletException) {
				t = ((ServletException) e).getRootCause();
			}
			if (t == null && e instanceof java.sql.SQLException) {
				t = ((java.sql.SQLException) e).getNextException();
			}
			e = t;
		}
		return Boolean.TRUE.equals(userException);
	}

	private static String getRequestNameFull(final HttpServletRequest request) {
		final StringBuilder requestName = new StringBuilder(request.getMethod());
		requestName.append(" from ");
		requestName.append(request.getRemoteAddr());
		requestName.append(" (");
		requestName.append(request.getRequestURL().toString());
		final String queryString = request.getQueryString();
		if (queryString != null) {
			requestName.append('?');
			requestName.append(request.getQueryString());
		}
		requestName.append(')');
		return requestName.toString();
	}

	/**
	 * Retourne le nom du type de la requête.
	 *
	 * @param request Requête HTTP
	 * @return Nom du dossier dans lequel sont classés les résultats
	 */
	private String getRequestName(final HttpServletRequest request) {
		final StringBuilder requestName = new StringBuilder(request.getMethod());
		requestName.append(" ");
		final String pathInfo = request.getPathInfo();
		if (pathInfo != null) {
			requestName.append(pathInfo);
		} else {
			final String servletPath = request.getServletPath();
			requestName.append(servletPath.substring(0, servletPath.indexOf('/', 1))).append("/*");
		}
		return requestName.toString();
	}

	//-------------------------------------------------------------------------

	private void logRequestStart(final HttpServletRequest request) {
		if (generalLog.isTraceEnabled()) {
			generalLog.trace("[Start] " + getRequestNameFull(request));
		}
	}

	// IsoFonctionnel existant
	// @Todo revoir implémentation
	private void logPerformance(final String controllerName, final long elapsedTime) {
		if (performanceLog.isInfoEnabled()) {
			performanceLog.info(">> Request : " + controllerName + " : time = " + elapsedTime);
		}
	}

	private void logRequestFinish(final HttpServletRequest httpRequest, final long requestTime, final boolean success) {
		final String requestName = getRequestName(httpRequest);
		if (success) {
			if (generalLog.isTraceEnabled()) {
				generalLog.trace("[Finish] " + requestName + " (" + getRequestNameFull(httpRequest) + ") reussie en  ( " + requestTime + ") ms");
			} else if (generalLog.isInfoEnabled() && !performanceLog.isInfoEnabled()) {
				generalLog.info("[Finish] " + requestName + " reussie en  ( " + requestTime + ") ms");
			}
			logPerformance(requestName, requestTime);
		} else {
			//Echec
			if (requestName == null) {
				generalLog.warn("[Finish] -NotFound- interrompue après ( " + requestTime + ") ms");
			} else {
				generalLog.warn("[Finish] " + requestName + " interrompue après ( " + requestTime + ") ms");
			}
		}
	}

	private void logRequestException(final Exception exception) {
		// e.toString()) affiche la classe du message et le message lui même.
		// Une KUser non proprement géré est volontairement loggée en erreur.
		generalLog.error(exception.toString(), exception);
	}

	private static AgentManager getAgentManager() {
		return Home.getContainer().getManager(AgentManager.class);
	}
}
