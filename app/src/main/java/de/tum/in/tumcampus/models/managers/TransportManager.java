package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.cards.MVVCard;

/**
 * Transport Manager, handles querying data from mvv and card creation
 */
public class TransportManager implements Card.ProvidesCard {
    /**
     * Get all departures for a station.
     * Cursor includes target station name, departure in remaining minutes.
     *
     * @param location Station name
     * @return List of departures
     * @throws Exception
     */
    public static List<Departure> getDeparturesFromExternal(Context context, String location) throws Exception {

        String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";
        // ISO needed for mvv
        String lookupUrl = "http://www.mvg-live.de/ims/dfiStaticAnzeige.svc?haltestelle=" + URLEncoder.encode(location, "ISO-8859-1");

        String query = URLEncoder.encode("select content from html where url=\"" + lookupUrl + "\" and xpath=\"//td[contains(@class,'Column')]/p\"", "UTF-8");
        Utils.log(query);

        JSONArray jsonArray = NetUtils.downloadJson(context, baseUrl + query).getJSONObject("query").getJSONObject("results").getJSONArray("p");
        //NetUtils n = new NetUtils(context);
        //Utils.log(n.downloadStringHttp( baseUrl + query));

        //Abort if our json is 'empty'
        if (jsonArray.length() < 3) {
            throw new NoSuchElementException("No departures found");
        }

        ArrayList<Departure> list = new ArrayList<Departure>(jsonArray.length());
        for (int j = 2; j < jsonArray.length(); j = j + 3) {
            Departure dep = new Departure();
            dep.symbol = jsonArray.getString(j);
            dep.line = jsonArray.getString(j + 1).trim();
            dep.time = System.currentTimeMillis() + jsonArray.getLong(j + 2) * 60000;
            list.add(dep);
        }

        Collections.sort(list);
        return list;
    }

    public static class Departure implements Comparable<Departure> {
        public String symbol;
        public String line;
        public long time;

        @Override
        public int compareTo(@NonNull Departure departure) {
            return time < departure.time ? -1 : 1;
        }
    }

    /**
     * Find stations by station name prefix
     *
     * @param location Name prefix
     * @return Database Cursor (name, _id)
     * @throws Exception
     */
    public static Cursor getStationsFromExternal(Context context, String location) throws Exception {

        String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";
        String lookupUrl = "http://www.mvg-live.de/ims/dfiStaticAuswahl.svc?haltestelle=" + URLEncoder.encode(location, "ISO-8859-1");

        String query = URLEncoder.encode("select content from html where url=\"" + lookupUrl
                + "\" and xpath=\"//a[contains(@href,'haltestelle')]\"", "UTF-8");
        Utils.log(query);

        JSONObject jsonObj = NetUtils.downloadJson(context, baseUrl + query);
        if (jsonObj == null)
            return null;

        jsonObj = jsonObj.getJSONObject("query");

        JSONArray jsonArray = new JSONArray();
        MatrixCursor mc = new MatrixCursor(new String[]{"name", "_id"});

        try {
            Object obj = jsonObj.getJSONObject(Const.JSON_RESULTS).get("a");
            if (obj instanceof JSONArray) {
                jsonArray = (JSONArray) obj;
            } else {
                if (obj.toString().contains("aktualisieren")) {
                    return mc;
                }
                jsonArray.put(obj);
            }
        } catch (Exception e) {
            Utils.log(e);
            return mc;
        }

        for (int j = 0; j < jsonArray.length(); j++) {
            String station = jsonArray.getString(j).replaceAll("\\s+", " ");
            mc.addRow(new String[]{station, String.valueOf(j)});
        }
        return mc;
    }

    /**
     * Inserts a MVV card for the nearest public transport station
     *
     * @param context Context
     */
    @Override
    public void onRequestCard(Context context) {
        if (!NetUtils.isConnected(context))
            return;

        // Get station for current campus
        final String station = new LocationManager(context).getStation();
        if (station == null)
            return;

        List<Departure> cur;
        try {
            cur = getDeparturesFromExternal(context, station);
            MVVCard card = new MVVCard(context);
            card.setStation(station);
            card.setDepartures(cur);
            card.apply();
        } catch (Exception e) {
            Utils.log(e);
        }
    }
}