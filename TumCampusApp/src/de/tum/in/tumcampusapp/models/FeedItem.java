package de.tum.in.tumcampusapp.models;

import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * FeedItem object
 */
public class FeedItem {

	/**
	 * Feed ID
	 */
	public int feedId;

	/**
	 * Item title
	 */
	public String title;

	/**
	 * Item link, e.g. http://www.heise.de/...
	 */
	public String link;

	/**
	 * description Item description
	 */
	public String description;

	/**
	 * DateTime
	 */
	public Date date;

	/**
	 * image Local image, e.g. /mnt/sdcard/tumcampus/feeds/cache/xy.jpg
	 */
	public String image;

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
	public FeedItem(int feedId, String title, String link, String description, Date date, String image) {

		this.feedId = feedId;
		this.title = title;
		this.link = link;
		this.description = description;
		this.date = date;
		this.image = image;
	}

	@Override
	public String toString() {
		return "feedId=" + feedId + " title=" + title + " link=" + link + " description=" + description + " date="
				+ Utils.getDateString(date) + " image=" + image;
	}
}