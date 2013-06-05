package de.tum.in.tumcampusapp.models.managers;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.tum.in.tumcampusapp.auxiliary.JsonConst;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.Event;

/**
 * Event Manager, handles database stuff, external imports
 */
public class EventManager {

	/**
	 * Last insert counter
	 */
	public static int lastInserted = 0;

	/**
	 * Convert JSON object to Event, download event picture
	 * 
	 * Example JSON: e.g. { "id": "166478443419659", "owner": { "name":
	 * "TUM Campus App for Android", "category": "Software", "id":
	 * "162327853831856" }, "name":
	 * "R\u00fcckmeldung f\u00fcr Wintersemester 2011/12", "description": "..."
	 * , "start_time": "2011-08-15T00:00:00", "end_time": "2011-08-15T03:00:00",
	 * "location": "TU M\u00fcnchen", "privacy": "OPEN", "updated_time":
	 * "2011-06-25T06:26:14+0000" }
	 * 
	 * <pre>
	 * @param json see above
	 * @return Event
	 * @throws Exception
	 * </pre>
	 */
	public static Event getFromJson(JSONObject json) throws Exception {

		String eventId = json.getString(JsonConst.JSON_ID);

		String picture = "http://graph.facebook.com/" + eventId + "/Picture?type=large";

		String target = Utils.getCacheDir("events/cache") + eventId + ".jpg";
		Utils.downloadFileThread(picture, target);

		String description = "";
		if (json.has(JsonConst.JSON_DESCRIPTION)) {
			description = json.getString(JsonConst.JSON_DESCRIPTION);
		}
		String location = "";
		if (json.has(JsonConst.JSON_LOCATION)) {
			location = json.getString(JsonConst.JSON_LOCATION);
		}
		// Link only available in event/feed
		String link = "";

		return new Event(eventId, json.getString(JsonConst.JSON_NAME), Utils.getDateTime(json.getString(JsonConst.JSON_START_TIME)), Utils.getDateTime(json
				.getString(JsonConst.JSON_END_TIME)), location, description, link, target);
	}

	/**
	 * Database connection
	 */
	private SQLiteDatabase db;

	/**
	 * Constructor, open/create database, create table if necessary
	 * 
	 * <pre>
	 * @param context Context
	 * </pre>
	 */
	public EventManager(Context context) {
		db = DatabaseManager.getDb(context);

		// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS events (id VARCHAR PRIMARY KEY, name VARCHAR, start VARCHAR, "
				+ "end VARCHAR, location VARCHAR, description VARCHAR, link VARCHAR, image VARCHAR)");
	}

	/**
	 * Removes all old items (older than 3 months)
	 */
	public void cleanupDb() {
		db.execSQL("DELETE FROM events WHERE start < date('now','-3 month')");
	}

	/**
	 * Download events from external interface (JSON)
	 * 
	 * <pre>
	 * @param force True to force download over normal sync period, else false
	 * @throws Exception
	 * </pre>
	 */
	public void downloadFromExternal(boolean force) throws Exception {

		if (!force && !SyncManager.needSync(db, this, 21600)) { // 6h
			return;
		}

		// https://graph.facebook.com/162327853831856/events?fields=id,name,start_time,end_time,location,description&limit=50&access_token=141869875879732|FbjTXY-wtr06A18W9wfhU8GCkwU

		String url = "https://graph.facebook.com/162327853831856/events?" + "fields=id,name,start_time,end_time,location,description&limit=50&access_token=";
		String token = "141869875879732|FbjTXY-wtr06A18W9wfhU8GCkwU";

		JSONArray jsonArray = Utils.downloadJson(url + URLEncoder.encode(token)).getJSONArray("data");

		cleanupDb();
		int count = Utils.dbGetTableCount(db, "events");

		db.beginTransaction();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				replaceIntoDb(getFromJson(jsonArray.getJSONObject(i)));
			}
			SyncManager.replaceIntoDb(db, this);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		// update last insert counter
		lastInserted += Utils.dbGetTableCount(db, "events") - count;
	}

	/**
	 * Get event details form the database
	 * 
	 * <pre>
	 * @param id Event-ID
	 * @return Database cursor (image, name, weekday, start_de, end_de, location, description, link, _id)
	 * </pre>
	 */
	public Cursor getDetailsFromDb(String id) {
		return db.rawQuery("SELECT image, name, strftime('%w', start) as weekday, "
				+ "strftime('%d.%m.%Y %H:%M', start) as start_de, strftime('%H:%M', end) as end_de, "
				+ "location, description, link, id as _id FROM events WHERE id = ?", new String[] { id });
	}

	/**
	 * Get all upcoming or unfinished events from the database
	 * 
	 * @return Database cursor (image, name, weekday, start_de, end_de,
	 *         location, _id)
	 */
	public Cursor getNextFromDb() {
		return db.rawQuery("SELECT image, name, strftime('%w', start) as weekday, "
				+ "strftime('%d.%m.%Y %H:%M', start) as start_de, strftime('%H:%M', end) as end_de, "
				+ "location, id as _id FROM events WHERE end > datetime('now', 'localtime') " + "ORDER BY start ASC LIMIT 25", null);
	}

	/**
	 * Get all finished events from the database
	 * 
	 * @return Database cursor (image, name, weekday, start_de, end_de,
	 *         location, _id)
	 */
	public Cursor getPastFromDb() {
		return db.rawQuery("SELECT image, name, strftime('%w', start) as weekday, "
				+ "strftime('%d.%m.%Y %H:%M', start) as start_de, strftime('%H:%M', end) as end_de, "
				+ "location, id as _id FROM events WHERE end <= datetime('now', 'localtime') " + "ORDER BY start DESC LIMIT 50", null);
	}

	/**
	 * Removes all cache items
	 */
	public void removeCache() {
		db.execSQL("DELETE FROM events");
		Utils.emptyCacheDir("events/cache");
	}

	/**
	 * Replace or Insert a event in the database
	 * 
	 * <pre>
	 * @param e Event object
	 * @throws Exception
	 * </pre>
	 */
	public void replaceIntoDb(Event e) throws Exception {
		if (e.id.length() == 0) {
			throw new Exception("Invalid id.");
		}
		if (e.name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		db.execSQL("REPLACE INTO events (id, name, start, end, location, description, link, image) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", new String[] { e.id,
				e.name, Utils.getDateTimeString(e.start), Utils.getDateTimeString(e.end), e.location, e.description, e.link, e.image });
	}
}