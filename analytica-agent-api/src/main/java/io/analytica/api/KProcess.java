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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Un processus est un événement
 *   - déclenché dans une application spécifique
 *   - relatif à un type d'événement (exemple : métrics des pages, des requêtes sql, des mails envoyés, des services ...)
 *
 *	Un évenement est défini selon 3 axes
 *	 - when, quand a eu lieu l'événement
 *	 - what, de quoi s'agit-il ?
 *	 - where, où s'est passé l'événement  ? sur quel serveur ?
 *
 *	[what]
 * - categories defined an array of string : search/items

 * 	[when]
 * - a start date
 *
 * 	[data]
 * - a duration (cf.measures)
 * - a list of measures with a DURATION measure
 * - a list of metadatas
 *
 * - a list of sub processes (0..*)
 *
 * @author pchretien, npiedeloup
 * @version $Id: KProcess.java,v 1.8 2012/10/16 17:18:26 pchretien Exp $
 */
public final class KProcess {
	/**
	 * Mesure de type durée.
	 */
	public static final String DURATION = "duration";
	/**
	 * Mesure de type durée.
	 */
	public static final String SUB_DURATION = "subDuration";
	/**
	 * REGEX décrivant les règles sur les noms (type de process, mesures et metadata, . (exemples : sql, mail, services)
	 */
	public static final Pattern NAME_REGEX = Pattern.compile("[a-z][a-zA-Z]*");

	private final String appName;
	private final String type; //ex : sql, page....

	private final String[] categories; //what ex : search/countries
	private final String location; //where ex : serverXXX

	private final Date startDate; //when

	private final Map<String, Double> measures;
	private final Map<String, Set<String>> metaDatas;
	private final List<KProcess> subProcesses;

	/**
	 * Le constructeur est package car il faut passer par le builder.
	 * @param appName Nom de l'application
	 * @param type Type du processus
	 * @param categories Sous processus
	 * @param startDate Date du processus
	 * @param measures Mesures du processus
	 * @param metaDatas Metadonnées du processus
	 * @param subProcesses Liste des sous processus
	 */
	KProcess(final String appName, final String type,
			final String[] categories,
			final String location,
			final Date startDate,
			final Map<String, Double> measures,
			final Map<String, Set<String>> metaDatas,
			final List<KProcess> subProcesses) {
		KProcessUtil.checkNotNull(appName, "appName is required");
		KProcessUtil.checkNotNull(type, "type of process is required");
		//		checkNotNull(categories, "categories of process are required");
		//		checkNotNull(location, "location of process is required");
		KProcessUtil.checkNotNull(startDate, "startDate is required");
		KProcessUtil.checkNotNull(measures, "measures are required");
		KProcessUtil.checkNotNull(metaDatas, "metaDatas are required");
		KProcessUtil.checkNotNull(subProcesses, "subProcesses are required");
		//---
		if (!NAME_REGEX.matcher(appName).matches()) {
			throw new IllegalArgumentException("appName " + appName + " must match regex :" + NAME_REGEX);
		}
		if (!NAME_REGEX.matcher(type).matches()) {
			throw new IllegalArgumentException("process type " + type + " must match regex :" + NAME_REGEX);
		}
		for (final String category : categories) {
			if (!NAME_REGEX.matcher(category).matches()) {
				throw new IllegalArgumentException("category " + category + " must match regex :" + NAME_REGEX);
			}
		}
		for (final String measureName : measures.keySet()) {
			if (!NAME_REGEX.matcher(measureName).matches()) {
				throw new IllegalArgumentException("measure " + measureName + " must match regex :" + NAME_REGEX);
			}
		}
		for (final String metaDataName : metaDatas.keySet()) {
			if (!NAME_REGEX.matcher(metaDataName).matches()) {
				throw new IllegalArgumentException("metadata " + metaDataName + " must match regex :" + NAME_REGEX);
			}
		}
		if (!measures.containsKey(DURATION)) {
			throw new IllegalArgumentException("measures must contain DURATION");
		}
		if (measures.containsKey(SUB_DURATION) && measures.get(SUB_DURATION) > measures.get(DURATION)) {
			throw new IllegalArgumentException("measures SUB-DURATION must be lower than DURATION (duration:" + measures.get(DURATION) + " < sub-duration:" + measures.get(SUB_DURATION) + ") in " + type + " : " + Arrays.asList(categories) + " at " + startDate);
		}
		//---------------------------------------------------------------------
		this.appName = appName;
		this.type = type;
		this.categories = categories;
		this.location = location;
		this.startDate = startDate;
		this.measures = Collections.unmodifiableMap(new HashMap<>(measures));
		this.metaDatas = Collections.unmodifiableMap(new HashMap<>(metaDatas));
		this.subProcesses = subProcesses;
	}

	/**
	 * @return AppName du processus
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * @return Type du processus
	 */
	public String getType() {
		return type;
	}

	/**
	 * [what]
	 * @return Sous-types du processus
	 */
	public String[] getCategories() {
		return categories;
	}

	/**
	 * @return Process duration */
	public double getDuration() {
		return measures.get(DURATION);
	}

	/**
	 * [when]
	 * @return Date processus
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * [where]
	 * @return location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @return Mesures du processus
	 */
	public Map<String, Double> getMeasures() {
		return measures;
	}

	/**
	 * @return Metadonnées du processus
	 */
	public Map<String, Set<String>> getMetaDatas() {
		return metaDatas;
	}

	/**
	 * @return Liste des sous-processus
	 */
	public List<KProcess> getSubProcesses() {
		return subProcesses;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "{appName:" + appName + ",  type:" + type + ", categories :" + Arrays.asList(categories) + ", location:" + location + "}";
	}
}
