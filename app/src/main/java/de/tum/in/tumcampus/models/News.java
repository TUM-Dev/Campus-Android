package de.tum.in.tumcampus.models;

import java.util.Date;

import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * News Object
 */
public class News {

	/**
	 * Date
	 */
	public final Date date;

	/**
	 * News Facebook-ID
	 */
	public final String id;

	/**
	 * Local image, e.g. /mnt/sdcard/tumcampus/news/cache/xy.jpg
	 */
	public final String image;

	/**
	 * Link Url, e.g. http://www.in.tum.de
	 */
	public final String link;

	/**
	 * Message, e.g. X released
	 */
	public final String message;

	/**
	 * New News
	 *
	 * @param id News Facebook-ID
	 * @param message Message, e.g. X released
	 * @param link Url, e.g. http://www.in.tum.de
	 * @param image Local image, e.g. /mnt/sdcard/tumcampus/news/cache/xy.jpg
	 * @param date Date
	 */
	public News(String id, String message, String link, String image, Date date) {

		this.id = id;
		this.message = message;
		this.link = link;
		this.image = image;
		this.date = date;
	}

	@Override
	public String toString() {
		return "id=" + id + " message=" + message + " link=" + link + " image="
				+ image + " date=" + Utils.getDateString(date);
	}
}