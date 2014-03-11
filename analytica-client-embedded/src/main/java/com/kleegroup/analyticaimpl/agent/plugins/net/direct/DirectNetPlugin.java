package com.kleegroup.analyticaimpl.agent.plugins.net.direct;

import io.vertigo.kernel.lang.Assertion;

import javax.inject.Inject;

import com.kleegroup.analytica.core.KProcess;
import com.kleegroup.analytica.server.ServerManager;
import com.kleegroup.analyticaimpl.agent.plugins.net.NetPlugin;

/**
 * Direct call to Analytica Server.
 * No buffering. Design for tests purpose.
 * @author npiedeloup
 * @version $Id: DirectNetPlugin.java,v 1.4 2012/10/16 08:30:56 pchretien Exp $
 */
public final class DirectNetPlugin implements NetPlugin {
	private final ServerManager serverManager;

	/**
	 * Constructor.
	 * @param serverManager Analytica ServerManager
	 */
	@Inject
	public DirectNetPlugin(final ServerManager serverManager) {
		Assertion.checkNotNull(serverManager);
		//---------------------------------------------------------------------
		this.serverManager = serverManager;

	}

	/** {@inheritDoc} */
	public void add(final KProcess process) {
		serverManager.push(process);
	}

	/** {@inheritDoc} */
	public void start() {
		//rien
	}

	/** {@inheritDoc} */
	public void stop() {
		//rien
	}
}
