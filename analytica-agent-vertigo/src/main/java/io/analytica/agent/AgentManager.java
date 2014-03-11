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
package io.analytica.agent;

import io.analytica.api.KProcess;
import io.vertigo.kernel.component.Manager;

/**
 * Agent de collecte des données.
 * Collecte automatique des Process (les infos sont portées par le thread courant).
 * 
 * @author pchretien, npiedeloup
 * @version $Id: AgentManager.java,v 1.2 2012/03/19 09:03:31 npiedeloup Exp $
 */
public interface AgentManager extends Manager {
	/**
	 * Enregistre dans le thread courant le démarrage d'un process.
	 * Doit respecter les règles sur le nom d'un process.
	 * @param type Type de process
	 * @param names Nom du process
	 */
	void startProcess(final String type, final String... names);

	/**
	 * Incrémentation d'une mesure du process courant (set si pas présente).
	 * @param measureType Nom de la mesure
	 * @param value Valeur
	 */
	void incMeasure(final String measureType, final double value);

	/**
	 * Annule et remplace une mesure du process courant.
	 * @param measureType Nom de la mesure
	 * @param value Valeur
	 */
	void setMeasure(final String measureType, final double value);

	/**
	 * Ajoute une méta-donnée du process courant (set si pas présente).
	 * TODO V0+ : voir si mutlivaluée intéressante.
	 * @param metaDataName Nom de la méta donnée
	 * @param value Valeur
	 */
	void addMetaData(final String metaDataName, final String value);

	/**
	 * Termine le process courant.
	 * Le processus courant devient alors le processus parent le cas échéant.
	 */
	void stopProcess();

	/**
	 * Ajout d'un process déjà assemblé par une sonde.
	 * Cet ajout peut-être multi-threadé.
	 * @param process Process à ajouter
	 */
	void add(KProcess process);
}
