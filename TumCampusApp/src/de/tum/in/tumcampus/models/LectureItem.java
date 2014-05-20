package de.tum.in.tumcampus.models;

import java.util.Date;

import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * LectureItem object
 */
public class LectureItem {

	// TODO Check whether to externalise Strings ex. Feiertag - problems (no
	// activity)
	/**
	 * Holiday object (extends LectureItem)
	 */
	public static class Holiday extends LectureItem {

		/**
		 * New Holiday
		 * 
		 * <pre>
		 * @param id Holiday ID
		 * @param date Date
		 * @param name Name, e.g. Allerheiligen
		 * </pre>
		 */

		public Holiday(String id, Date date, String name) {
			/*
			 * String id, String lectureId, Date start, Date end, String name,
			 * String module, String location, String note, String url, String
			 * seriesId
			 */
			super(id, "holiday", date, date, "Feiertage", "", "", name,
					"about:blank", id);
		}
	}

	/**
	 * Vacation object (extends LectureItem)
	 */
	public static class Vacation extends LectureItem {

		/**
		 * New Vacation
		 * 
		 * <pre>
		 * @param id Vacation ID
		 * @param start Begin Date
		 * @param end End Date
		 * @param name Name, e.g. Sommerferien
		 * </pre>
		 */
		public Vacation(String id, Date start, Date end, String name) {
			super(id, "vacation", start, end, "Ferien", "", "", name,
					"about:blank", id);
		}
	}

	/**
	 * End DateTime
	 */
	public Date end;

	/**
	 * Lecture item ID (LectureId_Start-Unix-Timestamp)
	 */
	public String id;

	/**
	 * Lecture ID
	 */
	public String lectureId;

	/**
	 * Lecture item location
	 */
	public String location;

	/**
	 * Lecture module
	 */
	public String module;

	/**
	 * Lecture name
	 */
	public String name;

	/**
	 * Lecture item note, e.g. Übung
	 */
	public String note;

	/**
	 * Lecture item series ID (LectureID_Week-Day_Start-Time)
	 */
	public String seriesId;

	/**
	 * Start DateTime
	 */
	public Date start;

	/**
	 * Lecture item URL
	 */
	public String url;

	/**
	 * New Lecture item
	 * 
	 * <pre>
	 * @param id Lecture item ID (LectureId_Start-Unix-Timestamp)
	 * @param lectureId Lecture ID
	 * @param start Start DateTime
	 * @param end End DateTime
	 * @param name Lecture name
	 * @param module Lecture module
	 * @param location Lecture item location
	 * @param note Lecture item note, e.g. Übung
	 * @param url Lecture item URL
	 * @param seriesId Lecture item series ID (LectureID_Week-Day_Start-Time)
	 * </pre>
	 */
	public LectureItem(String id, String lectureId, Date start, Date end,
			String name, String module, String location, String note,
			String url, String seriesId) {
		this.id = id;
		this.lectureId = lectureId;
		this.start = start;
		this.end = end;
		this.name = name;
		this.module = module;
		this.location = location;
		this.note = note;
		this.url = url;
		this.seriesId = seriesId;
	}

	@Override
	public String toString() {
		return "id=" + id + ", lectureId=" + lectureId + ", start="
				+ Utils.getDateTimeString(start) + ", end="
				+ Utils.getDateTimeString(end) + ", name=" + name + ", module="
				+ module + ", location=" + location + ", note=" + note
				+ ", seriesId=" + seriesId + ", url=" + url;
	}
}