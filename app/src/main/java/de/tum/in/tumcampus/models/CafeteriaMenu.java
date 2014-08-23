package de.tum.in.tumcampus.models;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import java.util.Date;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * CafeteriaMenu object
 */
public class CafeteriaMenu {

	/**
	 * Cafeteria ID
	 */
	public int cafeteriaId;

	/**
	 * Menu date
	 */
	public Date date;

	/**
	 * CafeteriaMenu Id (empty for addendum)
	 */
	public int id;

	/**
	 * Menu name
	 */
	public String name;

	/**
	 * Long type, e.g. Tagesgericht 1
	 */
	public String typeLong;

	/**
	 * Type ID
	 */
	public int typeNr;

	/**
	 * Short type, e.g. tg
	 */
	public String typeShort;

	// public String prize;

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
	public CafeteriaMenu(int id, int cafeteriaId, Date date, String typeShort,
			String typeLong, int typeNr, String name) {

		this.id = id;
		this.cafeteriaId = cafeteriaId;
		this.date = date;
		this.typeShort = typeShort;
		this.typeLong = typeLong;
		this.typeNr = typeNr;
		this.name = name;
		// this.prize = prize;
	}

    @Override
	public String toString() {
		return "id=" + this.id + " cafeteriaId=" + this.cafeteriaId + " date="
				+ Utils.getDateString(this.date) + " typeShort="
				+ this.typeShort + " typeLong=" + this.typeLong + " typeNr="
				+ this.typeNr + " name=" + this.name;
	}
}