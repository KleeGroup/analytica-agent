package io.analytica.agent;

import io.analytica.api.KProcess;
import io.vertigo.lang.Component;

/**
 * Agent de collecte des donn�es.
 * Collecte automatique des Process (les infos sont port�es par le thread courant).
 * 
 * @author pchretien, npiedeloup
 * @version $Id: AgentManager.java,v 1.2 2012/03/19 09:03:31 npiedeloup Exp $
 */
public interface AgentManager extends Component {
	/**
	 * Enregistre dans le thread courant le d�marrage d'un process.
	 * Doit respecter les r�gles sur le nom d'un process.
	 * @param type Type de process
	 * @param category Nom de la cat�gorie
	 */
	void startProcess(final String type, final String category);

	/**
	 * Incr�mentation d'une mesure du process courant (set si pas pr�sente).
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
	 * Ajoute une m�ta-donn�e du process courant (set si pas pr�sente).
	 * TODO V0+ : voir si mutlivalu�e int�ressante.
	 * @param metaDataName Nom de la m�ta donn�e
	 * @param value Valeur
	 */
	void addMetaData(final String metaDataName, final String value);

	/**
	 * Termine le process courant.
	 * Le processus courant devient alors le processus parent le cas �ch�ant.
	 */
	void stopProcess();

	/**
	 * Ajout d'un process d�j� assembl� par une sonde.
	 * Cet ajout peut-�tre multi-thread�.
	 * @param process Process � ajouter
	 */
	void add(KProcess process);
}
