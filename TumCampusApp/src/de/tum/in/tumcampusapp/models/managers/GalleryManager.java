package de.tum.in.tumcampusapp.models.managers;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.Gallery;

/**
 * Gallery Manager, handles database stuff, external imports
 */
public class GalleryManager {

	/**
	 * Last insert counter
	 */
	public static int lastInserted = 0;

	/**
	 * Position
	 */
	// TODO Review Vasyl: Changed 25.10.2012 by Florian Schulz (thx to Mr. Bley)
	public static int position = 0;

	/**
	 * Convert JSON object to Gallery, download gallery picture
	 * 
	 * <pre>
	 * Example JSON: e.g.  {
	 *          "id": "280076022057038",
	 *          "name": "Poker Turnier 30.1.2012, 16:30",
	 *          "source": "http://a1.sphotos.ak.fbcdn.net/...jpg",
	 *          "position": 1,
	 *          "created_time": "2012-01-21T19:38:25+0000"
	 *       },
	 * 
	 * @param json see above
	 * @return Gallery
	 * @throws Exception
	 * </pre>
	 */
	public static Gallery getFromJson(JSONObject json) throws Exception {

		String id = json.getString("id");

		String target = Utils.getCacheDir("gallery/cache") + id + ".jpg";

		Utils.downloadFileThread(json.getString("source"), target);

		return new Gallery(id, json.getString("name"), target, String.valueOf(position++), json.has("archive"));
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
	public GalleryManager(Context context) {
		db = DatabaseManager.getDb(context);

		// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS gallery (id VARCHAR PRIMARY KEY, name VARCHAR, image VARCHAR, " + "position INTEGER, archive VARCHAR(1))");
	}

	/**
	 * Download Gallery from external interface (JSON)
	 * 
	 * <pre>
	 * @param force True to force download over normal sync period, else false
	 * @throws Exception
	 * </pre>
	 */
	@SuppressWarnings("deprecation")
	public void downloadFromExternal() throws Exception {

		if (!SyncManager.needSync(db, this, 21600)) { // 6h
			return;
		}

		String url = "https://graph.facebook.com/280074732057167/photos?" + "fields=id,name,source,position&limit=25&access_token=";
		String urlArchive = "https://graph.facebook.com/291553714242602/photos?" + "fields=id,name,source,position&limit=25&access_token=";
		String token = "141869875879732|FbjTXY-wtr06A18W9wfhU8GCkwU";

		JSONArray jsonArray = Utils.downloadJson(url + URLEncoder.encode(token)).getJSONArray("data");
		JSONArray jsonArrayArchive = Utils.downloadJson(urlArchive + URLEncoder.encode(token)).getJSONArray("data");

		int count = Utils.dbGetTableCount(db, "gallery");

		db.beginTransaction();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				replaceIntoDb(getFromJson(jsonArray.getJSONObject(i)));
			}
			for (int i = 0; i < jsonArrayArchive.length(); i++) {
				jsonArrayArchive.getJSONObject(i).put("archive", true);
				replaceIntoDb(getFromJson(jsonArrayArchive.getJSONObject(i)));
			}
			SyncManager.replaceIntoDb(db, this);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		// update last insert counter
		lastInserted += Utils.dbGetTableCount(db, "gallery") - count;
	}

	/**
	 * Get a gallery item from the database
	 * 
	 * <pre>
	 * @param id Item-ID
	 * @return Database cursor (id, name, image, position)
	 * </pre>
	 */
	public Cursor getDetailsFromDb(String id) {
		return db.rawQuery("SELECT * FROM gallery WHERE id=?", new String[] { id });
	}

	/**
	 * Get all active gallery items from the database
	 * 
	 * @return Database cursor (_id, id)
	 */
	public Cursor getFromDb() {
		return db.rawQuery("SELECT image as _id, id FROM gallery WHERE archive='0' ORDER BY position LIMIT 50", null);
	}

	/**
	 * Get all archived gallery items from the database
	 * 
	 * @return Database cursor (_id, id)
	 */
	public Cursor getFromDbArchive() {
		return db.rawQuery("SELECT image as _id, id FROM gallery WHERE archive='1' ORDER BY position LIMIT 50", null);
	}

	/**
	 * Removes all cache items
	 */
	public void removeCache() {
		db.execSQL("DELETE FROM gallery");
		Utils.emptyCacheDir("gallery/cache");
	}

	/**
	 * Replace or Insert a gallery item in the database
	 * 
	 * <pre>
	 * @param g Gallery object
	 * @throws Exception
	 * </pre>
	 */
	public void replaceIntoDb(Gallery g) throws Exception {
		if (g.id.length() == 0) {
			throw new Exception("Invalid id.");
		}
		if (g.name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		db.execSQL("REPLACE INTO gallery (id, name, image, position, archive) VALUES (?, ?, ?, ?, ?)", new String[] { g.id, g.name, g.image, g.position,
				g.archive ? "1" : "0" });
	}

	/**
	 * Update database
	 */
	public void update() {
		db.execSQL("DROP TABLE gallery");
	}
}