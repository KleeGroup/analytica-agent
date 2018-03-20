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
package io.analytica.agent.impl.net;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

import io.analytica.agent.api.KProcessConnector;
import io.analytica.api.AProcess;
import io.analytica.api.KProcessJsonCodec;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * TODO look into http://ghads.wordpress.com/2008/09/24/calling-a-rest-webservice-from-java-without-libs/
 * @author npiedeloup
 * @version $Id: RemoteNetPlugin.java,v 1.4 2012/06/14 13:49:17 npiedeloup Exp $
 */
public final class RemoteConnector implements KProcessConnector {
	private static final String SPOOL_CONTEXT = "Analytica_Spool";
	private static final String VERSION_MAJOR = "1"; //used for compatibility warning
	private static final String VERSION_MINOR = "3";
	private static final String VERSION_PATCH = "2";
	private static final String VERSION = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_PATCH;
	private static final int EIGHT_HOURS = 8 * 60 * 60;
	private static long MAX_TIME_WAITING_TILL_FORCE_KILL = 10000L;
	private final Logger logger = LogManager.getLogger(RemoteConnector.class);

	private Thread processSenderThread = null;
	private final ConcurrentLinkedQueue<AProcess> processQueue = new ConcurrentLinkedQueue<AProcess>();
	private CacheManager manager;
	private final String serverUrl;
	private final int sendPaquetSize;
	private final int sizeCheckFrequencyMs;
	private final int sendPaquetFrequencySeconds;
	private Client locatorClient;
	private WebResource remoteWebResource;
	private final int maxResendJson = 5;//predefined value

	/**
	 * @param serverUrl Analytica's server addresse for PUT. example http://analytica/rest/process
	 * @param sendPaquetSize Number of processes to send in bulk
	 * @param sendPaquetFrequencySeconds Sending Frequency(in seconds)
	 */
	public RemoteConnector(final String serverUrl, final int sendPaquetSize, final int sendPaquetFrequencySeconds) {
		this.serverUrl = serverUrl;
		this.sendPaquetSize = sendPaquetSize;
		this.sendPaquetFrequencySeconds = sendPaquetFrequencySeconds;
		sizeCheckFrequencyMs = 250;
	}

	/** {@inheritDoc} */
	public void add(final AProcess process) {
		processQueue.add(process);
	}

	/** {@inheritDoc} */
	public void start() {
		locatorClient = Client.create();
		locatorClient.addFilter(new com.sun.jersey.api.client.filter.GZIPContentEncodingFilter());
		remoteWebResource = locatorClient.resource(serverUrl);

		manager = CacheManager.create();
		if (!manager.cacheExists(SPOOL_CONTEXT)) {
			final boolean overflowToDisk = true;
			final boolean eternal = false;
			final int timeToLiveSeconds = EIGHT_HOURS;
			final int timeToIdleSeconds = timeToLiveSeconds;
			final int maxElementsInMemory = 1;
			final Cache cache = new Cache(SPOOL_CONTEXT, maxElementsInMemory, overflowToDisk, eternal, timeToLiveSeconds, timeToIdleSeconds);
			manager.addCache(cache);
		}

		processSenderThread = new SendProcessThread(this);
		processSenderThread.start();

		checkServerVersion();
		logger.info("Start Analytica RemoteNetPlugin : connect to " + serverUrl);
	}

	/** {@inheritDoc} */
	public void stop() {
		logger.info("Stopping Analytica RemoteNetPlugin");
		processSenderThread.interrupt();
		try {
			processSenderThread.join(MAX_TIME_WAITING_TILL_FORCE_KILL);
		} catch (final InterruptedException e) {
			//NA
		}
		processSenderThread = null;
		flushAllProcessQueue();

		locatorClient = null;
		remoteWebResource = null;
		manager.shutdown();
		logger.info("Stop Analytica RemoteNetPlugin");
	}

	private static class SendProcessThread extends Thread {
		private final RemoteConnector remoteConnector;

		SendProcessThread(final RemoteConnector remoteConnector) {
			super("AnalyticaSendProcessThread");
			setDaemon(false);
			if (remoteConnector == null) {
				throw new NullPointerException("remoteConnector is required");
			}
			//-----------------------------------------------------------------
			this.remoteConnector = remoteConnector;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			while (!isInterrupted()) {
				try {
					remoteConnector.waitToSendPacket();
				} catch (final InterruptedException e) {
					interrupt();
				}
				remoteConnector.retrySendProcesses();
				remoteConnector.flushProcessQueue();
			}
		}
	}

	/**
	 * Waiting for:
	 * - timeout
	 * - processQueue.notify (number of paquets has achived the maximum)
	 * - interruption
	 * @throws InterruptedException
	 */
	void waitToSendPacket() throws InterruptedException {
		final long start = System.currentTimeMillis();
		while (processQueue.size() < sendPaquetSize //
				&& System.currentTimeMillis() - start < sendPaquetFrequencySeconds * 1000) {
			Thread.sleep(sizeCheckFrequencyMs);
		}
	}

	/**
	 * Standard usage of the flushing the process queue function
	 */
	void flushProcessQueue() {
		flushProcessQueue(sendPaquetSize * 2); //if necessary accepting twice the allowed limit
	}

	/**
	 * Flushing the entire process queue(generally when stopping the agent)
	 */
	void flushAllProcessQueue() {
		flushProcessQueue(Long.MAX_VALUE);
	}

	/**
	 * Flushing maximum maxPaquetSize elements from the queue
	 * - sending the data using the configured connector
	 * - removing the data
	 */
	private void flushProcessQueue(final long maxPaquetSize) {
		long sendPaquet = 0;
		final List<AProcess> processes = new ArrayList<AProcess>();
		AProcess head;
		do {
			head = processQueue.poll();
			if (head != null) {
				processes.add(head);
			}
			if (processes.size() >= sendPaquetSize * 2) {
				doSendProcesses(processes);
				sendPaquet += processes.size();
				processes.clear();
			}
		} while (head != null && sendPaquet < maxPaquetSize);
		doSendProcesses(processes);
	}

	private void doSendProcesses(final List<AProcess> processes) {
		if (!processes.isEmpty()) {
			final String json = KProcessJsonCodec.toJson(processes);
			try {
				doSendJson(remoteWebResource, json);
				logger.info("Send " + processes.size() + " processes to " + serverUrl + "(" + processQueue.size() + " remaining)");
			} catch (final Exception e) {
				logSendError(false, e);
				doStoreJson(json);
			}
		}
	}

	void retrySendProcesses() {
		try {
			final List<UUID> keys = manager.getCache(SPOOL_CONTEXT).getKeys();
			for (int i = 0; i < maxResendJson && i < keys.size(); i++) {
				final UUID key = keys.get(i);
				doSendJson(remoteWebResource, (String) manager.getCache(SPOOL_CONTEXT).get(key).getValue());
				manager.getCache(SPOOL_CONTEXT).remove(key);
			}
		} catch (final Exception e) {
			logSendError(true, e);
		}
	}

	private void logSendError(final boolean isResend, final Exception e) {
		final String action = isResend ? "Resend" : "Send";
		if (logger.isDebugEnabled()) {
			logger.debug(action + " : The Analytica server is not responding : " + e.getMessage(), e);
		} else {
			final String message = action + " : The Analytica server is not responding : " + e.getMessage();
			if (isResend) {
				logger.info(message);
			} else {
				logger.warn(message);
			}
		}
	}

	private void doStoreJson(final String json) {
		final Element element = new Element(UUID.randomUUID(), json);
		manager.getCache(SPOOL_CONTEXT).put(element);
	}

	private void doSendJson(final WebResource webResource, final String json) {
		final ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, json);
		checkResponseStatus(response);
	}

	private String doGet(final WebResource webResource) {
		final ClientResponse response = webResource.get(ClientResponse.class);
		checkResponseStatus(response);
		return response.getEntity(String.class);
	}

	private static void checkResponseStatus(final ClientResponse response) {
		final Status status = response.getClientResponseStatus();
		if (status.getFamily() == Family.SUCCESSFUL) {
			return;
		}
		throw new RuntimeException("The error " + status.getStatusCode() + " was received. " + status.getReasonPhrase());
	}

	private void checkServerVersion() {
		final WebResource remoteVersionWebResource = locatorClient.resource(serverUrl + "/version");
		try {
			final String serverVersion = doGet(remoteVersionWebResource);
			if (!serverVersion.startsWith(VERSION_MAJOR)) {
				logger.warn("Analytica's client version (" + VERSION + ") is not compatible with the server (" + serverVersion + ")");
			} else {
				logger.info("Connexion OK avec le serveur Analytica (" + serverUrl + ")");
			}
		} catch (final Exception e) {
			logger.warn("Unable to connect to the Analytica server (" + serverUrl + ") : " + e.getMessage());
		}
	}
}
