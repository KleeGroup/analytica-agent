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
package com.kleegroup.analyticaimpl.spies.javassist;

/**
 * Cas de Test JUNIT de l'API Analytics.
 * 
 * @author pchretien, npiedeloup
 * @version $Id: AgentManagerTest.java,v 1.3 2012/03/29 08:48:19 npiedeloup Exp $
 */
public final class TestAnalyse {

	public void work1s() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			//rien
		}
	}

	public void workRandom() {
		try {
			Thread.sleep((long) (Math.random() * 5000L));
		} catch (final InterruptedException e) {
			//rien
		}

	}
	
	public void workError() {
		throw new RuntimeException("MyError");

	}

	public void workNoAnalyse() {
		try {
			Thread.sleep(500);
		} catch (final InterruptedException e) {
			//rien
		}

	}

}
