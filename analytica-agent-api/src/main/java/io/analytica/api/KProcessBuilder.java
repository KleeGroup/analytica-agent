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
package io.analytica.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builder permettant de contruire un processus.
 * Il y a deux modes de création.
 *  - live (La date de début et celle de la création , la durée s'obtient lors de la création du process
 *  - différé (la date de débute et la durée sont renseignée ensembles )
 *
 * @author pchretien, npiedeloup
 * @version $Id: KProcessBuilder.java,v 1.18 2012/11/08 17:06:27 pchretien Exp $
 */
public final class KProcessBuilder {
	private final String appName;
	private final String myType;
	private final Date startDate;

	private String myLocation;
	private String[] myCategories;

	//Tableau des mesures identifiées par leur nom.
	private final Map<String, Double> measures;

	//Tableau des métadonnées identifiées par leur nom.
	private final Map<String, Set<String>> metaDatas;

	private final long start;
	private Double durationMs = null;
	private final List<KProcess> subProcesses;
	private final KProcessBuilder parent;

	/**
	 * Constructeur.
	 * La durée du processus sera obtenue lors de l'appel à la méthode build().
	 * @param type Type du processus
	 */
	public KProcessBuilder(final String appName, final String type) {
		this(appName, type, null, new Date(), null);
	}

	/**
	 * Constructeur .
	 * @param type Type du processus
	 * @param startDate Date de début processus
	 * @param durationMs Durée du processus (Millisecondes)
	 */
	public KProcessBuilder(final String appName, final String type, final Date startDate, final double durationMs) {
		this(appName, type, null, startDate, durationMs);
	}

	private KProcessBuilder(final String appName, final String type, final KProcessBuilder parent, final Date startDate, final Double durationMs) {
		KProcessUtil.checkNotNull(appName, "appName is required");
		KProcessUtil.checkNotNull(type, "type of process is required");
		KProcessUtil.checkNotNull(startDate, "start of process is required");
		//---
		this.appName = appName;
		this.myType = type;

		measures = new HashMap<>();
		metaDatas = new HashMap<>();
		subProcesses = new ArrayList<>();
		this.startDate = startDate;
		start = startDate.getTime();
		this.parent = parent;
		//---------------------------------------------------------------------
		this.durationMs = durationMs;
	}

	public KProcessBuilder withLocation(final String location) {
		this.myLocation = location;
		return this;
	}

	public KProcessBuilder withCategories(final String[] categories) {
		this.myCategories = categories;
		return this;
	}

	/**
	 * Incrément d'une mesure.
	 * Si la mesure est nouvelle, elle est automatiquement créée avec la valeur
	 * @param mName Nom de la mesure
	 * @param mValue  Valeur à incrémenter
	 * @return Builder
	 */
	public KProcessBuilder incMeasure(final String mName, final double mValue) {
		KProcessUtil.checkNotNull(mName, "Measure name is required");
		//---------------------------------------------------------------------
		final Double lastmValue = measures.get(mName);
		measures.put(mName, lastmValue == null ? mValue : mValue + lastmValue);
		return this;
	}

	/**
	 * Mise à jour d'une mesure.
	 * @param mName Nom de la mesure
	 * @param mValue  Valeur à incrémenter
	 * @return Builder
	 */
	public KProcessBuilder setMeasure(final String mName, final double mValue) {
		KProcessUtil.checkNotNull(mName, "Measure name is required");
		//---------------------------------------------------------------------
		measures.put(mName, mValue);
		return this;
	}

	/**
	 * Mise à jour d'une metadonnée.
	 * @param mdName Nom de la métadonnée
	 * @param mdValue  Valeur de la métadonnée
	 * @return Builder
	 */
	public KProcessBuilder withMetaData(final String mdName, final String mdValue) {
		KProcessUtil.checkNotNull(mdName, "Metadata name is required");
		KProcessUtil.checkNotNull(mdValue, "Metadata value is required");
		//---------------------------------------------------------------------
		Set<String> set = metaDatas.get(mdName);
		if (set == null) {
			set = new HashSet<>();
			metaDatas.put(mdName, set);
		}
		set.add(mdValue);
		return this;
	}

	/**
	 * Ajout d'un sous processus.
	 * @param subStartDate Date de début
	 * @param subDurationMs Durée du sous process en Ms
	 * @param type Type du sous process
	 * @return Builder
	 */
	public KProcessBuilder beginSubProcess(final String type, final Date subStartDate, final double subDurationMs) {
		return new KProcessBuilder(this.appName, type, this, subStartDate, subDurationMs);
	}

	/**
	 * Fin d'un sous processus.
	 * Le sous processus est automatiquement ajouté au processus parent.
	 * @return Builder
	 */
	public KProcessBuilder endSubProcess() {
		KProcessUtil.checkNotNull(parent, "parent is required when you close a subprocess");
		//---------------------------------------------------------------------
		parent.addSubProcess(build());
		return parent;
	}

	/**
	 * Ajout d'un sous processus.
	 * @param subPocess Sous-Processus à ajouter
	 * @return Builder
	 */
	public KProcessBuilder addSubProcess(final KProcess subPocess) {
		KProcessUtil.checkNotNull(subPocess, "sub process is required ");
		//---------------------------------------------------------------------
		subProcesses.add(subPocess);
		incMeasure(KProcess.SUB_DURATION, subPocess.getDuration());
		return this;
	}

	/**
	 * Construction du Processus.
	 * @return Process
	 */
	public KProcess build() {
		//Si on est dans le mode de construction en runtime, on ajoute la durée.
		if (durationMs == null) {
			durationMs = Long.valueOf(System.currentTimeMillis() - start).doubleValue();
		}
		//On ajoute la mesure obligatoire : durée
		setMeasure(KProcess.DURATION, durationMs);
		return new KProcess(appName, myType, myCategories, myLocation, startDate, measures, metaDatas, subProcesses);
	}
}
