package de.tum.in.tumcampusapp.models;

/**
 * Gallery object
 */
public class Gallery {

	/**
	 * Image is archived
	 */
	public boolean archive;

	/**
	 * Gallery Facebook-ID
	 */
	public String id;

	/**
	 * Local image file, e.g. /mnt/sdcard/tumcampus/events/cache/xy.jpg
	 */
	public String image;

	/**
	 * Name, e.g. PartyX
	 */
	public String name;

	/**
	 * Position in gallery
	 */
	public String position;

	/**
	 * New Event
	 * 
	 * <pre>
	 * @param id Event Facebook-ID
	 * @param name Name, e.g. PartyX
	 * @param image Local image, e.g. /mnt/sdcard/tumcampus/events/cache/xy.jpg
	 * @param position Position in gallery
	 * @param archive Image is archived
	 * </pre>
	 */
	public Gallery(String id, String name, String image, String position, boolean archive) {

		this.id = id;
		this.name = name;
		this.image = image;
		this.position = position;
		this.archive = archive;
	}

	@Override
	public String toString() {
		return "id=" + id + " name=" + name + " image=" + image + " position=" + position;
	}
}