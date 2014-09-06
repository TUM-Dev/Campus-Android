package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.MVVSymbolView;
import de.tum.in.tumcampus.cards.MVVCard;
import de.tum.in.tumcampus.data.LocationManager;
import java.util.NoSuchElementException;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.cards.ProvidesCard;

/**
 * Transport Manager, handles database stuff, internet connections
 */
public class TransportManager implements ProvidesCard {

    /**
     * Database connection
     */
    private SQLiteDatabase db;

    /**
     * Constructor, open/create database, create table if necessary
     * <p/>
     * <pre>
     *
     * @param context Context
     *                </pre>
     */
    public TransportManager(Context context) {
        db = DatabaseManager.getDb(context);

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS transports (name VARCHAR PRIMARY KEY)");
    }

    /**
     * delete a station from the database
     * <p/>
     * <pre>
     *
     * @param name Station name
     *             </pre>
     */
    public void deleteFromDb(String name) {
        db.execSQL("DELETE FROM transports WHERE name = ?",
                new String[]{name});
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
     * Get all departures for a station
     * <p/>
     * Cursor includes target station name, departure in remaining minutes
     * <p/>
     * <pre>
     *
     * @param location Station name
     * @return Database cursor (name, desc, _id)
     * @throws Exception </pre>
     */
    public Cursor getDeparturesFromExternal(String location) throws Exception {

        String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";
        // ISO needed for mvv
        String lookupUrl = "http://www.mvg-live.de/ims/dfiStaticAnzeige.svc?haltestelle=" + URLEncoder.encode(location, "ISO-8859-1");

        @SuppressWarnings("deprecation")
        String query = URLEncoder.encode("select content from html where url=\"" + lookupUrl + "\" and xpath=\"//td[contains(@class,'Column')]/p\"");
        Utils.log(query);

        JSONArray jsonArray = Utils.downloadJson(baseUrl + query)
                .getJSONObject("query").getJSONObject("results")
                .getJSONArray("p");

        if (jsonArray.length() < 3) {
            throw new NoSuchElementException("No departures found");
        }

        MatrixCursor mc = new MatrixCursor(new String[]{"symbol","name", "time", "_id"});

        for (int j = 2; j < jsonArray.length(); j = j + 3) {
            String symbol = jsonArray.getString(j);
            String name = jsonArray.getString(j + 1).trim();
            String desc = jsonArray.getString(j + 2) + " min";
            mc.addRow(new String[]{symbol, name, desc, String.valueOf(j)});
        }
        return mc;
    }

    /**
     * Find stations by station name prefix
     * <p/>
     * <pre>
     *
     * @param location Name prefix
     * @return Database Cursor (name, _id)
     * @throws Exception </pre>
     */
    public Cursor getStationsFromExternal(String location) throws Exception {

        String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";
        String lookupUrl = "http://www.mvg-live.de/ims/dfiStaticAuswahl.svc?haltestelle=" + URLEncoder.encode(location, "ISO-8859-1");

        @SuppressWarnings("deprecation")
        String query = URLEncoder
                .encode("select content from html where url=\"" + lookupUrl
                        + "\" and xpath=\"//a[contains(@href,'haltestelle')]\"");
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
     * <p/>
     * <pre>
     *
     * @param name Station name
     * @throws Exception </pre>
     */
    public void replaceIntoDb(String name) {
        Utils.log(name);
        if (name.length() == 0) {
            return;
        }
        db.execSQL("REPLACE INTO transports (name) VALUES (?)", new String[]{name});
    }

    @Override
    public void onRequestCard(Context context) throws Exception {
        // Get current campus
        int campus = 0;//new LocationManager(context).getCurrentCampus();
        if(campus==-1)
            return;

        // Get station for this campus
        final String location = LocationManager.campusStation[campus];
        Cursor cur = getDeparturesFromExternal(location);
        MVVCard card = new MVVCard(context);
        card.setStation(location);
        card.setDepartures(cur);
        card.setTime(System.currentTimeMillis());
        card.apply();
    }

    public static void setSymbol(Context context, TextView view, String symbol) {
        MVVSymbolView d = new MVVSymbolView(context, symbol);
        view.setTextColor(d.getTextColor());
        view.setText(symbol);
        view.setBackgroundDrawable(d);
    }
}