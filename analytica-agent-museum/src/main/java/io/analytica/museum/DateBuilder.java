package io.analytica.museum;

import io.analytica.api.Assertion;

import java.util.Calendar;
import java.util.Date;

/**
 * Utilitaire concernant les dates.
 * On distingue deux type de date
 * - date (précise au jour sans notion d'heure)
 * - dateTime
 * @author npiedeloup, pchretien
 */
public final class DateBuilder {
	private final Calendar calendar;

	/**
	 * Constructeur du builder de date.
	 * @param date Date de départ des traitements (elle n'est pas modifiée)
	 */
	public DateBuilder(final Date date) {
		Assertion.checkNotNull(date);
		//-----
		calendar = Calendar.getInstance();
		calendar.setTime(date);
	}

	/**
	 * @return Date tronquée à 0h 0min 0ss
	 */
	public Date build() {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * @return Date complète avec minutes, secondes et millisecondes
	 */
	public Date toDateTime() {
		return calendar.getTime();
	}

	/**
	 * Ajoute un nombre de secondes.
	 *
	 * @param seconds Nombre de secondes à ajouter
	 * @return Ce DateBuilder pour enchainer les traitements
	 */
	public DateBuilder addSeconds(final int seconds) {
		calendar.add(Calendar.SECOND, seconds);
		return this;
	}

	/**
	 * Ajoute un nombre de minutes.
	 *
	 * @param minutes Nombre de minutes à ajouter
	 * @return Ce DateBuilder pour enchainer les traitements
	 */
	public DateBuilder addMinutes(final int minutes) {
		calendar.add(Calendar.MINUTE, minutes);
		return this;
	}

	/**
	 * Ajoute un nombre d'heures.
	 *
	 * @param hours Nombre d'heures à ajouter
	 * @return Ce DateBuilder pour enchainer les traitements
	 */
	public DateBuilder addHours(final int hours) {
		calendar.add(Calendar.HOUR, hours);
		return this;
	}

	/**
	 * Ajoute un nombre de jours.
	 *
	 * @param days Nombre de jours à ajouter
	 * @return Ce DateBuilder pour enchainer les traitements
	 */
	public DateBuilder addDays(final int days) {
		calendar.add(Calendar.DATE, days);
		return this;
	}

	/**
	 * Ajoute un nombre de semaines.
	 *
	 * @param weeks Nombre de semaines à ajouter
	 * @return Ce DateBuilder pour enchainer les traitements
	 */
	public DateBuilder addWeeks(final int weeks) {
		calendar.add(Calendar.WEEK_OF_YEAR, weeks);
		return this;

	}

	/**
	 * Ajoute un nombre de mois.
	 * Si le nouveau mois est plus court, la date est tronquée
	 * Cf. Calendar.add(int, int).
	 * For example, if the date is the 31 january 2004,
	 * addMonths(date, 1) will return 29 february 2004.
	 *
	 * @param months Nombre de mois à ajouter
	 * @return Ce DateBuilder pour enchainer les traitements
	 */
	public DateBuilder addMonths(final int months) {
		calendar.add(Calendar.MONTH, months);
		return this;
	}

	/**
	 * Ajoute un nombre d'années.
	 *
	 * @param years Nombre d'années à ajouter
	 * @return Ce DateBuilder pour enchainer les traitements
	 */
	public DateBuilder addYears(final int years) {
		calendar.add(Calendar.YEAR, years);
		return this;
	}
}
