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
 */
package com.kleegroup.analytica.core;

import kasper.kernel.util.Assertion;

/**
 * Mesure d'un processus.
 * Une mesure poss�de un nom, une valeur et un nombre d'occurence.
 * Exemple : 
 * 	Nombre de mails envoy�s
 * 		measureType  =  mails
 * 		value = Nombre total de mail envoy�s
 *  
 * @author pchretien
 * @version $Id: Measure.java,v 1.3 2011/10/18 12:44:36 npiedeloup Exp $
 */
public final class Measure {
	private final double value;
	private final String measureType;

	/**
	 * Constructeur.
	 * @param value Valeur
	 * @param measureType Type de mesure
	 */
	public Measure(final double value, final String measureType) {
		Assertion.notNull(value);
		Assertion.notNull(measureType);
		//---------------------------------------------------------------------
		this.value = value;
		this.measureType = measureType;
	}

	/**
	 * @return Valeur totale des mesures effectu�es
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @return Type de mesure
	 */
	public String getMeasureType() {
		return measureType;
	}

	/**
	 * On incr�mente une mesure avec une autre mesure.
	 * @param addedMeasure Mesure 
	 * @return Nouvelle mesure
	 */
	Measure inc(final Measure addedMeasure) {
		Assertion.precondition(measureType.equals(addedMeasure.getMeasureType()), "unit� de mesures diff�rentes");
		//-----------------------------------------------------------------
		return new Measure(getValue() + addedMeasure.getValue(), measureType);
	}
}
