package de.tum.in.tumcampusapp.models.managers;

import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * CafeteriaMenu object
 */
public class CafeteriaMenu {

	/**
	 * Cafeteria ID
	 */
	int cafeteriaId;

	/**
	 * Menu date
	 */
	Date date;

	/**
	 * CafeteriaMenu Id (empty for addendum)
	 */
	int id;

	/**
	 * Menu name
	 */
	String name;

	/**
	 * Long type, e.g. Tagesgericht 1
	 */
	String typeLong;

	/**
	 * Type ID
	 */
	int typeNr;

	/**
	 * Short type, e.g. tg
	 */
	String typeShort;

	/**
	 * New CafeteriaMenu
	 * 
	 * <pre>
	 * @param id CafeteriaMenu Id (empty for addendum)
	 * @param cafeteriaId Cafeteria ID
	 * @param date Menu date
	 * @param typeShort Short type, e.g. tg 
	 * @param typeLong Long type, e.g. Tagesgericht 1
	 * @param typeNr Type ID
	 * @param name Menu name
	 * </pre>
	 */
	public CafeteriaMenu(int id, int cafeteriaId, Date date, String typeShort, String typeLong, int typeNr, String name) {

		this.id = id;
		this.cafeteriaId = cafeteriaId;
		this.date = date;
		this.typeShort = typeShort;
		this.typeLong = typeLong;
		this.typeNr = typeNr;
		this.name = name;
	}

	@Override
	public String toString() {
		return "id=" + this.id + " cafeteriaId=" + this.cafeteriaId + " date=" + Utils.getDateString(this.date) + " typeShort=" + this.typeShort + " typeLong="
				+ this.typeLong + " typeNr=" + this.typeNr + " name=" + this.name;
	}
}