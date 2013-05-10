package de.tum.in.tumcampusapp.models.managers;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import de.tum.in.tumcampusapp.auxiliary.JsonConst;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Transport Manager, handles database stuff, internet connections
 */
public class TransportManager {

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
	public TransportManager(Context context) {
		db = DatabaseManager.getDb(context);

		// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS transports (name VARCHAR PRIMARY KEY)");
	}

	/**
	 * delete a station from the database
	 * 
	 * <pre>
	 * @param name Station name
	 * </pre>
	 */
	public void deleteFromDb(String name) {
		db.execSQL("DELETE FROM transports WHERE name = ?", new String[] { name });
	}

	/**
	 * Checks if the transports table is empty
	 * 
	 * @return true if no stations are available, else false
	 */
	public boolean empty() {
		boolean result = true;
		Cursor c = db.rawQuery("SELECT name FROM transports LIMIT 1", null);
		if (c.moveToNext()) {
			result = false;
		}
		c.close();
		return result;
	}

	/**
	 * Get all stations from the database
	 * 
	 * @return Database cursor (name, _id)
	 */
	public Cursor getAllFromDb() {
		return db.rawQuery("SELECT name, name as _id FROM transports ORDER BY name", null);
	}

	/**
	 * Get all departures for a station
	 * 
	 * Cursor includes target station name, departure in remaining minutes
	 * 
	 * <pre>
	 * @param location Station name
	 * @return Database cursor (name, desc, _id)
	 * @throws Exception
	 * </pre>
	 */
	public Cursor getDeparturesFromExternal(String location) throws Exception {
		String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";

		// ISO needed for mvv
		String lookupUrl = "http://www.mvg-live.de/ims/dfiStaticAnzeige.svc?haltestelle=" + URLEncoder.encode(location, "ISO-8859-1");

		String query = URLEncoder.encode("select content from html where url=\"" + lookupUrl + "\" and xpath=\"//td[contains(@class,'Column')]/p\"");
		Utils.log(query);

		JSONArray jsonArray = Utils.downloadJson(baseUrl + query).getJSONObject("query").getJSONObject("results").getJSONArray("p");

		if (jsonArray.length() < 3) {
			throw new Exception("Sorry, no departures could be found");
		}

		MatrixCursor mc = new MatrixCursor(new String[] { "name", "desc", "_id" });

		for (int j = 2; j < jsonArray.length(); j = j + 3) {
			String name = jsonArray.getString(j) + " " + jsonArray.getString(j + 1).trim();
			String desc = jsonArray.getString(j + 2) + " min";
			mc.addRow(new String[] { name, desc, String.valueOf(j) });
		}
		return mc;
	}

	/**
	 * Find stations by station name prefix
	 * 
	 * <pre>
	 * @param location Name prefix
	 * @return Database Cursor (name, _id)
	 * @throws Exception
	 * </pre>
	 */
	public Cursor getStationsFromExternal(String location) throws Exception {

		String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";

		String lookupUrl = "http://www.mvg-live.de/ims/dfiStaticAuswahl.svc?haltestelle=" + URLEncoder.encode(location, "ISO-8859-1");

		String query = URLEncoder.encode("select content from html where url=\"" + lookupUrl + "\" and xpath=\"//a[contains(@href,'haltestelle')]\"");
		Utils.log(query);

		JSONObject jsonObj = Utils.downloadJson(baseUrl + query).getJSONObject("query");

		JSONArray jsonArray = new JSONArray();
		try {
			Object obj = jsonObj.getJSONObject(JsonConst.JSON_RESULTS).get("a");
			if (obj instanceof JSONArray) {
				jsonArray = (JSONArray) obj;
			} else {
				if (obj.toString().contains("aktualisieren")) {
					throw new JSONException("Unkown error");
				}
				jsonArray.put(obj);
			}
		} catch (JSONException e) {
			throw new Exception("Sorry, no station could be found");
		}

		MatrixCursor mc = new MatrixCursor(new String[] { "name", "_id" });

		for (int j = 0; j < jsonArray.length(); j++) {
			String station = jsonArray.getString(j).replaceAll("\\s+", " ");
			mc.addRow(new String[] { station, String.valueOf(j) });
		}
		return mc;
	}

	/**
	 * Replace or Insert a station into the database
	 * 
	 * <pre>
	 * @param name Station name
	 * @throws Exception
	 * </pre>
	 */
	public void replaceIntoDb(String name) {
		Utils.log(name);

		if (name.length() == 0) {
			return;
		}
		db.execSQL("REPLACE INTO transports (name) VALUES (?)", new String[] { name });
	}
}