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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kasper.kernel.lang.Builder;
import kasper.kernel.util.Assertion;

/**
 * Builder permettant de contruire un processus.
 * Il y a deux modes de création.
 *  - live (La date de début et celle de la création , la durée s'obtient lors de la création du process
 *  - différé (la date de débute et la durée sont renseignée ensembles )
 * 
 * @author pchretien
 * @version $Id: KProcessBuilder.java,v 1.10 2012/03/22 18:09:42 pchretien Exp $
 */
public final class KProcessBuilder implements Builder<KProcess> {
	private final String type;
	private final String name;
	private final Date startDate;

	private final Map<String, Measure> measures;
	private final List<MetaData> metaDatas;
	private final long start;
	private Long duration = null;
	private final List<KProcess> subProcesses;

	/**
	 * Constructeur.
	 * @param type Type du processus
	 * @param name Nom du processus
	 */
	public KProcessBuilder(final String type, final String name) {
		this(type, name, new Date());
	}

	/**
	 * Constructeur pour deserialization.
	 * @param type Type du processus
	 * @param name Nom du processus
	 * @param startDate Date de début processus
	 * @param duration Durée du processus
	 */
	public KProcessBuilder(final String type, final String name, final Date startDate, final long duration) {
		this(type, name, startDate);
		//---------------------------------------------------------------------
		this.duration = duration;
	}

	private KProcessBuilder(final String type, final String name, final Date startDate) {
		Assertion.notEmpty(type);
		Assertion.notEmpty(name);
		Assertion.notNull(startDate);
		Assertion.precondition(KProcess.REGEX.matcher(type).matches(), "le type du processus ne respecte pas la regex {0}", KProcess.REGEX);
		//---------------------------------------------------------------------
		measures = new HashMap<String, Measure>();
		metaDatas = new ArrayList<MetaData>();
		subProcesses = new ArrayList<KProcess>();
		this.startDate = startDate;
		start = startDate.getTime();
		this.type = type;
		this.name = name;
	}

	/**
	 * Incrément d'une mesure. 
	 * @param measure Mesure
	 */
	public KProcessBuilder incMeasure(final Measure measure) {
		Assertion.notNull(measure);
		//---------------------------------------------------------------------
		final Measure lastMeasure = measures.get(measure.getMeasureType());
		if (lastMeasure == null) {
			setMeasure(measure);
		} else {
			setMeasure(measure.inc(lastMeasure));
		}
		return this;
	}

	/** 
	 * Mise à jour d'une mesure.
	 * @param measure Mesure
	 */
	public KProcessBuilder setMeasure(final Measure measure) {
		Assertion.notNull(measure);
		//---------------------------------------------------------------------
		measures.put(measure.getMeasureType(), measure);
		return this;
	}

	/** 
	 * Ajout d'une meta-donnée.
	 * @param metaData meta-donnée
	 */
	public KProcessBuilder addMetaData(final MetaData metaData) {
		Assertion.notNull(metaData);
		//---------------------------------------------------------------------
		metaDatas.add(metaData);
		return this;
	}

	/**
	 * Ajout d'un sous processus.
	 * @param process Sous-Processus à ajouter
	 */
	public KProcessBuilder addSubProcess(final KProcess process) {
		Assertion.notNull(process);
		//---------------------------------------------------------------------
		subProcesses.add(process);
		return this;
	}

	/** 
	 * Construction du Processus.
	 * @return Process
	 */
	public KProcess build() {
		//Si on est dans le mode de construction en runtime, on ajoute la durée.
		if (duration == null) {
			duration = System.currentTimeMillis() - start;
		}
		return new KProcess(type, name, startDate, measures.values(), metaDatas, duration, subProcesses);
	}

}