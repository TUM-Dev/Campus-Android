package de.tum.in.tumcampusapp.models.managers;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.JsonConst;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.News;

/**
 * News Manager, handles database stuff, external imports
 */
public class NewsManager {

	/**
	 * Last insert counter
	 */
	public static int lastInserted = 0;

	/**
	 * Convert JSON object to News and download news image
	 * 
	 * Example JSON: e.g. { "id": "162327853831856_174943842570257", "from": {
	 * ... }, "message": "Testing ...", "picture":
	 * "http://photos-d.ak.fbcdn.net/hphotos-ak-ash4/268937_174943835903591_162327853831856_476156_7175901_s.jpg"
	 * , "link":
	 * "https://www.facebook.com/photo.php?fbid=174943835903591&set=a.174943832570258.47966.162327853831856&type=1"
	 * , "name": "Wall Photos", "icon":
	 * "http://static.ak.fbcdn.net/rsrc.php/v1/yz/r/StEh3RhPvjk.gif", "type":
	 * "photo", "object_id": "174943835903591", "created_time":
	 * "2011-07-04T01:58:25+0000", "updated_time": "2011-07-04T01:58:25+0000" },
	 * 
	 * <pre>
	 * @param json see above
	 * @return News
	 * @throws Exception
	 * </pre>
	 */
	public static News getFromJson(JSONObject json) throws Exception {

		String target = "";
		if (json.has("picture")) {
			String picture = json.getString(JsonConst.JSON_PICTURE);
			target = Utils.getCacheDir("news/cache") + Utils.md5(picture) + ".jpg";
			Utils.downloadFileThread(picture, target);
		}
		String link = "";
		if (json.has(JsonConst.JSON_LINK) && !json.getString(JsonConst.JSON_LINK).contains(Const.FACEBOOK)) {
			link = json.getString(JsonConst.JSON_LINK);
		}
		if (link.length() == 0 && json.has(JsonConst.JSON_OBJECT_ID)) {
			link = "http://graph.facebook.com/" + json.getString(JsonConst.JSON_OBJECT_ID) + "/Picture?type=normal";
		}

		// message empty => description empty => caption
		String message = "";
		if (json.has(JsonConst.JSON_MESSAGE)) {
			message = json.getString(JsonConst.JSON_MESSAGE);
		} else if (json.has(JsonConst.JSON_DESCRIPTION)) {
			message = json.getString(JsonConst.JSON_DESCRIPTION);
		} else if (json.has(JsonConst.JSON_CAPTION)) {
			message = json.getString(JsonConst.JSON_CAPTION);
		} else if (json.has("name")) {
			message = json.getString("name");
		} else if (json.has("story")) { // NTK
			message = json.getString("story"); // NTK
		} else
			Log.e("TumCampus", "No message / json object error - FB changed something?"); // NTK

		Date date = Utils.getDate(json.getString(JsonConst.JSON_CREATED_TIME));

		return new News(json.getString(JsonConst.JSON_ID), message, link, target, date);
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
	public NewsManager(Context context) {
		db = DatabaseManager.getDb(context);

		// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS news (id VARCHAR PRIMARY KEY, message VARCHAR, link VARCHAR, " + "image VARCHAR, date VARCHAR)");
	}

	/**
	 * Removes all old items (older than 3 months)
	 */
	public void cleanupDb() {
		db.execSQL("DELETE FROM news WHERE date < date('now','-3 month')");
	}

	/**
	 * Download news from external interface (JSON)
	 * 
	 * <pre>
	 * @param force True to force download over normal sync period, else false
	 * @throws Exception
	 * </pre>
	 */
	public void downloadFromExternal() throws Exception {

		if (!SyncManager.needSync(db, this, 86400)) {
			return;
		}

		String url = "https://graph.facebook.com/162327853831856/feed/?limit=100&access_token=";
		String token = "141869875879732|FbjTXY-wtr06A18W9wfhU8GCkwU";

		@SuppressWarnings("deprecation")
		JSONArray jsonArray = Utils.downloadJson(url + URLEncoder.encode(token)).getJSONArray(JsonConst.JSON_DATA);

		cleanupDb();
		int count = Utils.dbGetTableCount(db, "news");

		db.beginTransaction();
		try {
			int countItems = 0;
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);

				String[] types = new String[] { "photo", "link", "video" };

				String[] ids = new String[] { "162327853831856_228060957258545", "162327853831856_228060957258545", "162327853831856_224344127630228" };

				// filter out events, empty items
				if ((!Arrays.asList(types).contains(obj.getString("type")) && !Arrays.asList(ids).contains(obj.getString("id")))
						|| !obj.getJSONObject(JsonConst.JSON_FROM).getString(JsonConst.JSON_ID).equals("162327853831856")) {
					continue;
				}
				// NTK added Kurz notiert Archiv ---> ignore in news
				if (obj.has("name") && (obj.getString("name").equals("Kurz notiert") || obj.getString("name").equals("Kurz notiert Archiv"))) {
					continue;
				}

				// NTK added ignore events
				if (obj.has("story") && (obj.getString("story").equals("TUM Campus App created an event."))) {
					continue;
				}

				if (countItems > 24) {
					break;
				}
				replaceIntoDb(getFromJson(obj));
				countItems++;
			}
			SyncManager.replaceIntoDb(db, this);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		// update last insert counter
		lastInserted += Utils.dbGetTableCount(db, "news") - count;
	}

	/**
	 * Get all news from the database
	 * 
	 * @return Database cursor (image, message, date_de, link, _id)
	 */
	public Cursor getAllFromDb() {
		return db.rawQuery("SELECT image, message, strftime('%d.%m.%Y', date) as date_de, link, id as _id " + "FROM news ORDER BY date DESC", null);
	}

	/**
	 * Removes all cache items
	 */
	public void removeCache() {
		db.execSQL("DELETE FROM news");
		Utils.emptyCacheDir("news/cache");
	}

	/**
	 * Replace or Insert a event in the database
	 * 
	 * <pre>
	 * @param n News object
	 * @throws Exception
	 * </pre>
	 */
	public void replaceIntoDb(News n) throws Exception {
		Log.d(getClass().getSimpleName(), n.toString());

		if (n.id.length() == 0) {
			throw new Exception("Invalid id.");
		}
		if (n.message.length() == 0) {
			Log.e("TumCampus", n.toString());
			throw new Exception("Invalid message.");
		}
		db.execSQL("REPLACE INTO news (id, message, link, image, date) VALUES (?, ?, ?, ?, ?)",
				new String[] { n.id, n.message, n.link, n.image, Utils.getDateString(n.date) });
	}
}