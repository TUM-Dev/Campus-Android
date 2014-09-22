package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.cards.MVVCard;

/**
 * Transport Manager, handles database stuff, internet connections
 */
public class TransportManager implements Card.ProvidesCard {

    /**
     * Database connection
     */
    private final SQLiteDatabase db;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public TransportManager(Context context) {
        db = DatabaseManager.getDb(context);

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS transports (name VARCHAR PRIMARY KEY)");
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
        return db.rawQuery(
                "SELECT name, name as _id FROM transports ORDER BY name", null);
    }

    /**
     * Get all departures for a station.
     * Cursor includes target station name, departure in remaining minutes.
     *
     * @param location Station name
     * @return List of departures
     * @throws Exception
     */
    public List<Departure> getDeparturesFromExternal(String location) throws Exception {

        String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";
        // ISO needed for mvv
        String lookupUrl = "http://www.mvg-live.de/ims/dfiStaticAnzeige.svc?haltestelle=" + URLEncoder.encode(location, "ISO-8859-1");

        String query = URLEncoder.encode("select content from html where url=\"" + lookupUrl + "\" and xpath=\"//td[contains(@class,'Column')]/p\"", "UTF-8");
        Utils.log(query);

        JSONArray jsonArray = Utils.downloadJson(baseUrl + query)
                .getJSONObject("query").getJSONObject("results")
                .getJSONArray("p");

        if (jsonArray.length() < 3) {
            throw new NoSuchElementException("No departures found");
        }

        //TODO read server time

        ArrayList<Departure> list = new ArrayList<Departure>(jsonArray.length());
        for (int j = 2; j < jsonArray.length(); j = j + 3) {
            Departure dep = new Departure();
            dep.symbol = jsonArray.getString(j);
            dep.line = jsonArray.getString(j + 1).trim();
            dep.time = jsonArray.getLong(j + 2);
            list.add(dep);
        }

        Collections.sort(list);

        return list;
    }

    public class Departure implements Comparable<Departure> {
        public String symbol;
        public String line;
        public long time;

        @Override
        public int compareTo(@NonNull Departure departure) {
            return time<departure.time?-1:1;
        }
    }

    /**
     * Find stations by station name prefix
     *
     * @param location Name prefix
     * @return Database Cursor (name, _id)
     * @throws Exception
     */
    public Cursor getStationsFromExternal(String location) throws Exception {

        String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";
        String lookupUrl = "http://www.mvg-live.de/ims/dfiStaticAuswahl.svc?haltestelle=" + URLEncoder.encode(location, "ISO-8859-1");

        String query = URLEncoder.encode("select content from html where url=\"" + lookupUrl
                        + "\" and xpath=\"//a[contains(@href,'haltestelle')]\"", "UTF-8");
        Utils.log(query);

        JSONObject jsonObj = Utils.downloadJson(baseUrl + query).getJSONObject(
                "query");
        JSONArray jsonArray = new JSONArray();

        try {
            Object obj = jsonObj.getJSONObject(Const.JSON_RESULTS).get("a");
            if (obj instanceof JSONArray) {
                jsonArray = (JSONArray) obj;
            } else {
                if (obj.toString().contains("aktualisieren")) {
                    throw new NoSuchElementException("No station found");
                }
                jsonArray.put(obj);
            }
        } catch (Exception e) {
            throw new NoSuchElementException("No station found");
        }

        MatrixCursor mc = new MatrixCursor(new String[]{"name", "_id"});

        for (int j = 0; j < jsonArray.length(); j++) {
            String station = jsonArray.getString(j).replaceAll("\\s+", " ");
            mc.addRow(new String[]{station, String.valueOf(j)});
        }
        return mc;
    }

    /**
     * Replace or Insert a station into the database
     *
     * @param name Station name
     */
    public void replaceIntoDb(String name) {
        Utils.log(name);
        if (name.length() == 0) {
            return;
        }
        db.execSQL("REPLACE INTO transports (name) VALUES (?)", new String[]{name});
    }

    /**
     * Inserts a MVV card for the nearest public transport station
     * @param context Context
     */
    @Override
    public void onRequestCard(Context context) {
        if(!Utils.isConnected(context))
            return;

        // Get station for current campus
        final String station = new LocationManager(context).getStation();
        if(station==null)
            return;

        List<Departure> cur;
        try {
            cur = getDeparturesFromExternal(station);
            MVVCard card = new MVVCard(context);
            card.setStation(station);
            card.setDepartures(cur);
            card.setTime(System.currentTimeMillis());
            card.apply();
        } catch (Exception e) {
            Utils.log(e);
        }
    }
}