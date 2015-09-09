package io.analytica.agent;

import io.analytica.api.KProcess;
import io.vertigo.lang.Component;

/**
 * Agent de collecte des données.
 * Collecte automatique des Process (les infos sont portées par le thread courant).
 * 
 * @author pchretien, npiedeloup
 * @version $Id: AgentManager.java,v 1.2 2012/03/19 09:03:31 npiedeloup Exp $
 */
public interface AgentManager extends Component {
	/**
	 * Enregistre dans le thread courant le démarrage d'un process.
	 * Doit respecter les règles sur le nom d'un process.
	 * @param type Type de process
	 * @param category Nom de la catégorie
	 */
	void startProcess(final String type, final String category);

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
