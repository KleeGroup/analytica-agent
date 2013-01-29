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
package com.kleegroup.analytica.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import kasper.kernel.util.Assertion;

/**
 * Un process est un événement ayant 
 * - un type 
 * - un nom
 * - une durée
 * - une liste de sous process
 * - une liste de mesures 
 * 
 * @author pchretien
 * @version $Id: KProcess.java,v 1.5 2011/09/27 16:54:40 npiedeloup Exp $
 */
public final class KProcess {
	public static final Pattern REGEX = Pattern.compile("[A-Z][A-Z0-9_]*");

	private final String type;
	private final String name;
	private final Date startDate;

	private final Collection<Measure> measures;
	private final Collection<MetaData> metaDatas;
	private final long duration;
	private final List<KProcess> subProcesses;

	//Le constructeur est pacakage car il faut passer par le builder
	KProcess(final String type, final String name, final Date startDate, final Collection<Measure> measures, final Collection<MetaData> metaDatas, final long duration,
			final List<KProcess> subProcesses) {
		Assertion.notEmpty(type);
		Assertion.notEmpty(name);
		Assertion.precondition(REGEX.matcher(type).matches(), "le type du processus ne respecte pas la regex {0}", REGEX);
		//Assertion.precondition(REGEX.matcher(name).matches(), "le nom du processus ne respecte pas la regex {0}", REGEX);
		//---------------------------------------------------------------------
		this.type = type;
		this.name = name;
		this.startDate = startDate;
		this.measures = new ArrayList<Measure>(measures);
		this.metaDatas = new ArrayList<MetaData>(metaDatas);
		this.duration = duration;
		this.subProcesses = subProcesses;
	}

	/**
	 * @return Type duprocessus
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return Nom du processus
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Date de début du processus
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @return Liste des mesures 
	 */
	public Collection<Measure> getMeasures() {
		return measures;
	}

	/**
	 * @return Liste des meta-données 
	 */
	public Collection<MetaData> getMetaDatas() {
		return metaDatas;
	}

	/**
	 * @return Liste des Sous-Processus
	 */
	public List<KProcess> getSubProcesses() {
		return subProcesses;
	}

	/**
	 * @return Durée du processus
	 */
	public long getDuration() {
		return duration;
	}
}