package de.tum.in.tumcampus.models;

import java.util.Date;

import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * FeedItem object
 */
public class FeedItem {

	/**
	 * DateTime
	 */
	public Date date;

	/**
	 * description Item description
	 */
	public String description;

	/**
	 * Feed ID
	 */
	public int feedId;

	/**
	 * image Local image, e.g. /mnt/sdcard/tumcampus/feeds/cache/xy.jpg
	 */
	public String image;

	/**
	 * Item link, e.g. http://www.heise.de/...
	 */
	public String link;

	/**
	 * Item title
	 */
	public String title;

	/**
	 * New Feed item (news)
	 * 
	 * <pre>
	 * @param feedId Feed ID
	 * @param title Item title
	 * @param link Item link, e.g. http://www.heise.de/...
	 * @param description Item description
	 * @param date DateTime
	 * @param image Local image, e.g. /mnt/sdcard/tumcampus/feeds/cache/xy.jpg
	 * </pre>
	 */
	public FeedItem(int feedId, String title, String link, String description,
			Date date, String image) {

		this.feedId = feedId;
		this.title = title;
		this.link = link;
		this.description = description;
		this.date = date;
		this.image = image;
	}

	@Override
	public String toString() {
		return "feedId=" + feedId + " title=" + title + " link=" + link
				+ " description=" + description + " date="
				+ Utils.getDateString(date) + " image=" + image;
	}
}