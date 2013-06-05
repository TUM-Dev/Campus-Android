package de.tum.in.tumcampusapp.models;

import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Event object
 */
public class Event {

	/**
	 * Description, multiline
	 */
	public String description;

	/**
	 * Event end DateTime
	 */
	public Date end;

	/**
	 * Event Facebook-ID
	 */
	public String id;

	/**
	 * Local image, e.g. /mnt/sdcard/tumcampus/events/cache/xy.jpg
	 */
	public String image;

	/**
	 * Event link, e.g. http://www.xyz.de
	 */
	public String link;

	/**
	 * Location, e.g. Munich
	 */
	public String location;

	/**
	 * Name, e.g. PartyX
	 */
	public String name;

	/**
	 * Event start DateTime
	 */
	public Date start;

	/**
	 * New Event
	 * 
	 * <pre>
	 * @param id Event Facebook-ID
	 * @param name Name, e.g. PartyX
	 * @param start Event start DateTime
	 * @param end Event end DateTime
	 * @param location Location, e.g. Munich
	 * @param description Description, multiline
	 * @param link Event link, e.g. http://www.xyz.de
	 * @param image Local image, e.g. /mnt/sdcard/tumcampus/events/cache/xy.jpg
	 * </pre>
	 */
	public Event(String id, String name, Date start, Date end, String location, String description, String link, String image) {

		this.id = id;
		this.name = name;
		this.start = start;
		this.end = end;
		this.location = location;
		this.description = description;
		this.link = link;
		this.image = image;
	}

	@Override
	public String toString() {
		return "id=" + id + " name=" + name + " start=" + Utils.getDateString(start) + " end=" + Utils.getDateString(end) + " location=" + location
				+ " description=" + description + " link=" + link + " image=" + image;
	}
}