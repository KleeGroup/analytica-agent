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
package io.analytica.agent.impl.net;

import io.analytica.api.KProcess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

/**
 * TODO voir http://ghads.wordpress.com/2008/09/24/calling-a-rest-webservice-from-java-without-libs/
 * @author npiedeloup
 * @version $Id: RemoteNetPlugin.java,v 1.4 2012/06/14 13:49:17 npiedeloup Exp $
 */
public final class FileLogConnector implements KProcessConnector {
	private static final String VERSION_MAJOR = "1.0"; //definit la compatibilité
	private static final String VERSION_MINOR = "0";
	private static final String VERSION = VERSION_MAJOR + "." + VERSION_MINOR;
	private static final String DATE_FORMAT = "yyyyMMdd HH:mm:ss";

	private final Logger logger = Logger.getLogger(FileLogConnector.class);
	private final long spoolFrequencyMs = 250;
	private Thread processSpoolerThread = null;
	private final ConcurrentLinkedQueue<KProcess> processQueue = new ConcurrentLinkedQueue<KProcess>();
	private final String fileName;

	public FileLogConnector(final String fileName) {
		if (fileName == null || fileName.trim().length() == 0) {
			throw new IllegalArgumentException("fileName to spool process is required");
		}
		//---------------------------------------------------------------------	
		this.fileName = fileName;
	}

	/** {@inheritDoc} */
	public void add(final KProcess process) {
		processQueue.add(process);
	}

	/** {@inheritDoc} */
	public void start() {
		processSpoolerThread = new SpoolProcessThread(this);
		processSpoolerThread.start();
		final File spoolFile = new File(fileName);
		final File dir = spoolFile.getParentFile();
		dir.mkdirs();
		try {
			spoolFile.createNewFile();
		} catch (final IOException e) {
			throw new RuntimeException("Can't create " + fileName, e);
		}
		if (!(spoolFile.exists() && spoolFile.canWrite())) {
			throw new IllegalArgumentException("Can't write " + fileName);
		}
		logger.info("Start Analytica FileLogNetPlugin : log to " + fileName);
	}

	/** {@inheritDoc} */
	public void stop() {
		processSpoolerThread.interrupt();
		try {
			processSpoolerThread.join(10000);//on attend 10s max
		} catch (final InterruptedException e) {
			//rien, si interrupt on continu l'arret
		}
		processSpoolerThread = null;
		flushProcessQueue();
		logger.info("Stop Analytica RemoteNetPlugin");
	}

	private static class SpoolProcessThread extends Thread {
		private final FileLogConnector fileLogConnector;

		SpoolProcessThread(final FileLogConnector fileLogConnector) {
			super("AnalyticaSpoolProcessThread");
			setDaemon(false); //ce n'est pas un démon car on veux envoyer les derniers process
			//-----------------------------------------------------------------
			this.fileLogConnector = fileLogConnector;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			while (!isInterrupted()) {
				try {
					fileLogConnector.waitToSendPacket();
				} catch (final InterruptedException e) {
					interrupt();//On remet le flag qui a été reset lors du throw InterruptedException (pour le test isInterrupted())
					//on envoi avant l'arret du serveur
				}
				//On flush la queue sur :
				// - le timeout
				// - un interrupt (arret du serveur)
				fileLogConnector.flushProcessQueue();
			}
		}
	}

	/**
	 * On attend la constitution d'un paquet.
	 * Rend la main après : 
	 * - le timeout 
	 * - un processQueue.notify (taille max de la queue atteinte)
	 * - un interrupt (arret du serveur) 
	 * @throws InterruptedException Si interrupt
	 */
	void waitToSendPacket() throws InterruptedException {
		synchronized (processQueue) {
			processQueue.wait(spoolFrequencyMs);
		}
	}

	/**
	 * Effectue le flush de la queue des processes à envoyer.
	 */
	void flushProcessQueue() {
		final Collection<KProcess> processes = new ArrayList<KProcess>();
		KProcess head;
		do {
			head = processQueue.poll();
			if (head != null) {
				processes.add(head);
			}
		} while (head != null); //On depile tout : car lors de l'arret du serveur on aura pas d'autre flush
		if (!processes.isEmpty()) {
			final String json = new Gson().toJson(processes);
			writeToLogFile(json);
			//logger.info("Spool " + processes.size() + " processes to " + spoolLogger.getName());
		}
	}

	private void writeToLogFile(final String json) {
		final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		Writer writer;
		try {
			writer = new FileWriter(fileName, true);
			try {
				final BufferedWriter out = new BufferedWriter(writer);
				try {
					doWriteToLogFile(json, sdf, out);
				} finally {
					out.close();
				}
			} finally {
				writer.close();
			}
		} catch (final IOException e) {
			logger.error("Can't write in logFile :" + fileName + " : " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Can't write in logFile :" + fileName, e); //stopera le thread
		}
	}

	private void doWriteToLogFile(final String json, final SimpleDateFormat sdf, final BufferedWriter out) throws IOException {
		out.write(sdf.format(new Date()));
		out.write(" [AnalyticaProcess] - ");
		out.write(json);
		out.write("\n");
	}
}
