/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidi�re - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.analytica.spies.impl.javassist;

/**
 * Cas de Test JUNIT de l'API Analytics.
 * 
 * @author pchretien, npiedeloup
 * @version $Id: AgentManagerTest.java,v 1.3 2012/03/29 08:48:19 npiedeloup Exp $
 */
public final class TestAnalyse {
	static {
		System.out.println("Loading TestAnalyse.class");
	}

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

	public int workResult() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			//rien
		}
		return 1;
	}

	public void workReentrant() {
		//		final int i = 1;
		work1s();
		//		final int j = 2;
	}

	public static void workStatic() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			//rien
		}
	}

	public int workFastest() {
		return new Integer((int) (Math.random() * 100000));
	}

	public int workFastestNotInstrumented() {
		return new Integer((int) (Math.random() * 100000));
	}
}
