package com.kleegroup.analyticaimpl.spies.httprequest;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import vertigo.kernel.Home;
import vertigo.kernel.exception.VUserException;

import com.kleegroup.analytica.agent.AgentManager;

/**
 * Filtre de servlet (javax.servlet.Filter).
 * Il permet d'instrumenter les pages avec appels, dur�es, taille des flux d'entr�e et de sortie.
 * 
 * @author pchretien, npiedeloup 
 * @version $Id: UIFilter.java,v 1.7 2010/11/16 10:36:54 pchretien Exp $
 */
public final class HttpRequestSpyFilter implements Filter {

	private static final String PT_REQUEST = "REQUEST";
	private static final String ME_RESPONSE_LENGTH = "RESPONSE_LENGTH";
	private static final String ME_USER_ERROR_PCT = "USER_ERROR_PCT";
	private static final String ME_OTHER_ERROR_PCT = "OTHER_ERROR_PCT";
	private static final String ME_CPU_TIME = "CPU_TIME";

	/**
	 * M�canisme de log racine
	 */
	private final Logger generalLog = Logger.getRootLogger();

	/** {@inheritDoc} */
	public void init(final FilterConfig filterConfig) {
		//rien
	}

	/** {@inheritDoc} */
	public void destroy() {
		// Rien de sp�cial
	}

	/**
	 * La m�thode doFilter est appel�e par le container chaque fois qu'une paire
	 * requ�te/r�ponse passe � travers la cha�ne suite � une requ�te d'un client
	 * pour une ressource au bout de la cha�ne. L'instance de FilterChain pass�e
	 * dans cette m�thode permet au filtre de passer la requ�te et la r�ponse �
	 * l'entit� suivante dans la cha�ne.
	 *
	 * Cette impl�mentation encapsule les flux d'entr�e et de sortie (compress�s
	 * ou non) pour compter les octets lus ou �crits. Puis elle logue le
	 * r�sultat avec le nom du filtre (pour distinguer si c'est avant ou apr�s
	 * compression). Le r�sultat est �galement enregistr� dans les statistiques.
	 *
	 * @param request javax.servlet.ServletRequest
	 * @param response javax.servlet.ServletResponse
	 * @param chain javax.servlet.FilterChain
	 * @throws java.io.IOException Si une erreur d'entr�e/sortie survient
	 * @throws javax.servlet.ServletException Si une erreur de servlet survient
	 */
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			filter((HttpServletRequest) request, (HttpServletResponse) response, chain);
		} else {
			chain.doFilter(request, response);
		}
	}

	private void filter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final FilterChain chain) throws IOException, ServletException {
		final AgentManager agentManager = getAgentManager();
		final long begin = System.currentTimeMillis();
		boolean ok = false;
		//D�marrage de la requ�te.
		final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		final long startCpuTime = threadBean.getThreadCpuTime(Thread.currentThread().getId());

		final String processName = getProcessName(httpRequest);
		agentManager.startProcess(PT_REQUEST, processName);
		logRequestStart(httpRequest);
		final HttpRequestSpyServletResponseWrapper wrappedResponse = new HttpRequestSpyServletResponseWrapper(httpResponse);
		try {
			chain.doFilter(httpRequest, httpResponse);
			agentManager.setMeasure(ME_RESPONSE_LENGTH, wrappedResponse.getDataLength());
			ok = true;
		} catch (final ServletException servletException) {
			//Permet de compter les erreurs par type.
			if (isUserException(servletException)) {
				agentManager.setMeasure(ME_USER_ERROR_PCT, 100);
			} else {
				agentManager.setMeasure(ME_OTHER_ERROR_PCT, 100);
			}
			logRequestException(servletException);
			throw servletException;
		} finally {
			final long endCpuTime = threadBean.getThreadCpuTime(Thread.currentThread().getId());
			if (startCpuTime != -1 && endCpuTime != -1) {
				//On compte le temps CPU de ce thread
				agentManager.setMeasure(ME_CPU_TIME, endCpuTime / 1000000 - startCpuTime / 1000000);
			}

			agentManager.stopProcess();
			logRequestFinish(httpRequest, System.currentTimeMillis() - begin, ok);
		}
	}

	//-------------------------------------------------------------------------

	private static boolean isUserException(final Throwable throwable) {
		Throwable e = throwable;

		while (e != null) {
			if (e instanceof VUserException) {
				return true;
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
		return false;
	}

	/**
	 * Retourne le nom du type de la requ�te.
	 *
	 * @param request Requ�te HTTP
	 * @return Nom du dossier dans lequel sont class�s les r�sultats
	 */
	private String getProcessName(final HttpServletRequest request) {
		final StringBuilder requestName = new StringBuilder();
		final String pathInfo = request.getPathInfo();
		if (pathInfo != null) {
			requestName.append(pathInfo);
		} else {
			final String servletPath = request.getServletPath();
			requestName.append(servletPath);
		}
		requestName.append(" (").append(request.getMethod()).append(")");
		return requestName.toString();
	}

	private static AgentManager getAgentManager() {
		//Le Filter �tant un filtre de servlet, il ne supporte pas l'injection de d�pendance via le contructeur
		return Home.getComponentSpace().resolve(AgentManager.class);
	}

	//-------------------------------------------------------------------------

	private void logRequestStart(final HttpServletRequest request) {
		if (generalLog.isTraceEnabled()) {
			generalLog.trace("[Start] " + getRequestNameFull(request));
		}
	}

	private void logRequestFinish(final HttpServletRequest httpRequest, final long requestTime, final boolean success) {
		final String requestName = getProcessName(httpRequest);
		if (success) {
			if (generalLog.isTraceEnabled()) {
				generalLog.trace("[Finish] " + requestName + " (" + getRequestNameFull(httpRequest) + ") reussie en  ( " + requestTime + ") ms");
			} else if (generalLog.isInfoEnabled()) {
				generalLog.info("[Finish] " + requestName + " reussie en  ( " + requestTime + ") ms");
			}
		} else {
			//Echec
			if (requestName == null) {
				generalLog.warn("[Finish] -NoRequestName- interrompue apr�s ( " + requestTime + ") ms");
			} else {
				generalLog.warn("[Finish] " + requestName + " interrompue apr�s ( " + requestTime + ") ms");
			}
		}
	}

	private void logRequestException(final Exception exception) {
		// e.toString()) affiche la classe du message et le message lui m�me.
		// Une KUser non proprement g�r� est volontairement logg�e en erreur.
		generalLog.error(exception.toString(), exception);
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
}
