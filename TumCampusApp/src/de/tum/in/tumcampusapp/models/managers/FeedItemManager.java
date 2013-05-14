package de.tum.in.tumcampusapp.models.managers;

import java.net.URLEncoder;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.JsonConst;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.FeedItem;

/**
 * Feed item Manager, handles database stuff, external imports
 */
public class FeedItemManager {

	/**
	 * Last insert counter
	 */
	public static int lastInserted = 0;

	/**
	 * Convert JSON to FeedItem and download feed item iamge
	 * 
	 * Example JSON: e.g. { "title":
	 * "US-Truppenabzug aus Afghanistan: \"Verlogen und verkorkst\"",
	 * "description": "..." , "link":
	 * "http://www.n-tv.de/politik/pressestimmen/Verlogen-und-verkorkst-article3650731.html"
	 * , "pubDate": "Thu, 23 Jun 2011 20:06:53 GMT", "enclosure": { "url":
	 * "http://www.n-tv.de/img/30/304801/Img_4_3_220_Pressestimmen.jpg" }
	 * 
	 * <pre>
	 * @param feedId Feed ID
	 * @param json see above
	 * @return Feeds
	 * @throws Exception
	 * </pre>
	 */
	public static FeedItem getFromJson(int feedId, JSONObject json) throws Exception {

		String target = "";
		if (json.has(JsonConst.JSON_ENCLOSURE)) {
			final String enclosure = json.getJSONObject(JsonConst.JSON_ENCLOSURE).getString(Const.URL);

			target = Utils.getCacheDir("rss/cache") + Utils.md5(enclosure) + ".jpg";
			Utils.downloadFileThread(enclosure, target);
		}
		Date pubDate = new Date();
		if (json.has(JsonConst.JSON_PUB_DATE)) {
			pubDate = Utils.getDateTimeRfc822(json.getString(JsonConst.JSON_PUB_DATE));
		}
		String description = "";
		if (json.has(JsonConst.JSON_DESCRIPTION) && !json.isNull(JsonConst.JSON_DESCRIPTION)) {
			// decode HTML entites, remove links, images, etc.
			description = Html.fromHtml(json.getString(JsonConst.JSON_DESCRIPTION).replaceAll("\\<.*?\\>", "")).toString();
		}
		return new FeedItem(feedId, json.getString(JsonConst.JSON_TITLE).replaceAll("\n", ""), json.getString(JsonConst.JSON_LINK), description, pubDate,
				target);
	}

	/**
	 * Database connection
	 */
	private SQLiteDatabase db;

	/**
	 * Additional information for exception messages
	 */
	public String lastInfo = "";

	/**
	 * Constructor, open/create database, create table if necessary
	 * 
	 * <pre>
	 * @param context Context
	 * </pre>
	 */
	public FeedItemManager(Context context) {
		db = DatabaseManager.getDb(context);

		// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS feeds_items (" + "id INTEGER PRIMARY KEY AUTOINCREMENT, feedId INTEGER, "
				+ "title VARCHAR, link VARCHAR, description VARCHAR, date VARCHAR, image VARCHAR)");
	}

	/** Removes all old items (older than 7 days) */
	public void cleanupDb() {
		db.execSQL("DELETE FROM feeds_items WHERE date < date('now','-7 day')");
	}

	/**
	 * Deletes feed items from the database
	 * 
	 * <pre>
	 * @param feedId Feed ID
	 * </pre>
	 */
	public void deleteFromDb(int feedId) {
		db.execSQL("DELETE FROM feeds_items WHERE feedId = ?", new String[] { String.valueOf(feedId) });
	}

	/**
	 * Download feed items from external interface (YQL+JSON)
	 * 
	 * <pre>
	 * @param id Feed-ID
	 * @param retry Retry download after resolving RSS-Url
	 * @param force True to force download over normal sync period, else false
	 * @throws Exception
	 * </pre>
	 */
	public void downloadFromExternal(int id, boolean retry) throws Exception {
		String syncId = "feeditem" + id;
		if (!SyncManager.needSync(db, syncId, 900)) {
			return;
		}

		cleanupDb();
		int count = Utils.dbGetTableCount(db, "feeds_items");

		Cursor feed = db.rawQuery("SELECT feedUrl FROM feeds WHERE id = ?", new String[] { String.valueOf(id) });
		feed.moveToNext();
		String feedUrl = feed.getString(0);
		feed.close();

		lastInfo = feedUrl;
		String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";

		@SuppressWarnings("deprecation")
		String query = URLEncoder.encode("SELECT title, link, description, pubDate, enclosure.url " + "FROM rss WHERE url=\"" + feedUrl + "\" LIMIT 25");

		JSONObject jsonObj = Utils.downloadJson(baseUrl + query).getJSONObject("query");

		if (jsonObj.isNull("results") && !retry) {
			String url = Utils.getRssLinkFromUrl(feedUrl);

			if (!url.equals(feedUrl)) {
				db.execSQL("UPDATE feeds SET feedUrl=? WHERE id = ?", new String[] { url, String.valueOf(id) });
				downloadFromExternal(id, true);
				return;
			}
		}
		Object obj = jsonObj.getJSONObject("results").get("item");

		JSONArray jsonArray = new JSONArray();
		if (obj instanceof JSONArray) {
			jsonArray = (JSONArray) obj;
		} else {
			jsonArray.put(obj);
		}

		deleteFromDb(id);
		db.beginTransaction();
		try {
			for (int j = 0; j < jsonArray.length(); j++) {
				insertIntoDb(getFromJson(id, jsonArray.getJSONObject(j)));
			}
			SyncManager.replaceIntoDb(db, syncId);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		// update last insert counter
		lastInserted += Utils.dbGetTableCount(db, "feeds_items") - count;
	}

	/**
	 * Checks if the feeds_items table is empty
	 * 
	 * @return true if no feed items are available, else false
	 */
	public boolean empty() {
		boolean result = true;
		Cursor c = db.rawQuery("SELECT id FROM feeds_items LIMIT 1", null);
		if (c.moveToNext()) {
			result = false;
		}
		c.close();
		return result;
	}

	/**
	 * Get all feed items for a feed from the database
	 * 
	 * <pre>
	 * @param feedId Feed ID
	 * @return Database cursor (image, title, description, link, _id)
	 * </pre>
	 */
	public Cursor getAllFromDb(String feedId) {
		return db.rawQuery("SELECT image, title, description, link, id as _id " + "FROM feeds_items WHERE feedId = ? ORDER BY date DESC",
				new String[] { feedId });
	}

	/**
	 * Insert a feed item in the database
	 * 
	 * <pre>
	 * @param n FeedItem object
	 * @throws Exception
	 * </pre>
	 */
	public void insertIntoDb(FeedItem n) throws Exception {
		if (n.feedId <= 0) {
			throw new Exception("Invalid feedId.");
		}
		if (n.link.length() == 0) {
			throw new Exception("Invalid link.");
		}
		if (n.title.length() == 0) {
			throw new Exception("Invalid title.");
		}
		db.execSQL("INSERT INTO feeds_items (feedId, title, link, description, " + "date, image) VALUES (?, ?, ?, ?, ?, ?)",
				new String[] { String.valueOf(n.feedId), n.title, n.link, n.description, Utils.getDateTimeString(n.date), n.image });
	}

	/** Removes all cache items */
	public void removeCache() {
		db.execSQL("DELETE FROM feeds_items");
		Utils.emptyCacheDir("rss/cache");
	}
}