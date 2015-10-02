package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.cards.MVVCard;

/**
 * Transport Manager, handles querying data from mvv and card creation
 */
public class TransportManager implements Card.ProvidesCard {

    /*  Documentation for using efa.mvv-muenchen.de
     *
     *  use XML_STOPFINDER_REQUEST to find available stops, e.g.: "Gar"
     *  http://efa.mvv-muenchen.de/mobile/XML_STOPFINDER_REQUEST?outputFormat=JSON&language=de&stateless=1&coordOutputFormat=WGS84&locationServerActive=1&type_sf=any&name_sf=Gar&anyObjFilter_sf=126&reducedAnyPostcodeObjFilter_sf=64&reducedAnyTooManyObjFilter_sf=2&useHouseNumberList=true&anyMaxSizeHitList=500
     *  probably hint the one that gets best="1"
     *  then SORT BY QUALITY, since finding 500+ stops is no fun, probably restrict this to around 10
     *
     *  Breakdown:
     *  http://efa.mvv-muenchen.de/mobile/XML_STOPFINDER_REQUEST? // MVV API base
     *  outputFormat=JSON
     *  &language=de&stateless=1&coordOutputFormat=WGS84&locationServerActive=1 // Common parameters
     *  &type_sf=any // Station type. Just keep "any"
     *  &name_sf=Gar // Search string
     *  &anyObjFilter_sf=126&reducedAnyPostcodeObjFilter_sf=64&reducedAnyTooManyObjFilter_sf=2&useHouseNumberList=true
     *  &anyMaxSizeHitList=500 // How many results there are being provided
     *
     *  use XSLT_DM_REQUEST for departures from stations
     *
     *  Full request: http://efa.mvv-muenchen.de/mobile/XSLT_DM_REQUEST?outputFormat=JSON&language=de&stateless=1&coordOutputFormat=WGS84&type_dm=stop&name_dm=Freising&itOptionsActive=1&ptOptionsActive=1&mergeDep=1&useAllStops=1&mode=direct
     *
     *  Breakdown:
     *  http://efa.mvv-muenchen.de/mobile/XSLT_DM_REQUEST? // MVV API base
     *  outputFormat=JSON // One could also specify XML
     *  &language=de // Tests showed this gets ignored by MVV, but set to language nevertheless
     *  &stateless=1&coordOutputFormat=WGS84 // Common parameters. They are reasonable, so don't change
     *  &type_dm=stop // Station type.
     *  &name_dm=Freising // This is the actual query string
     *  &itOptionsActive=1&ptOptionsActive=1&mergeDep=1&useAllStops=1&mode=direct // No idea what these parameters actually do. Feel free to experiment with them, just don't blame me if anything breaks
     */

    private static final String MVV_API_BASE = "http://efa.mvv-muenchen.de/mobile/"; // No HTTPS support :(
    private static final String OUTPUT_FORMAT = "outputFormat=JSON";
    private static final String STATELESS = "stateless=1";
    private static final String COORD_OUTPUT_FORMAT = "coordOutputFormat=WGS84";
    private static final String LOCATION_SERVER = "locationServerActive=1";
    private static final String STATION_SEARCH_TYPE = "type_sf=any";
    private static final String STATION_SEARCH_COMMON = "anyObjFilter_sf=126&reducedAnyPostcodeObjFilter_sf=64&reducedAnyTooManyObjFilter_sf=2&useHouseNumberList=true";
    private static final String STATION_SEARCH_HITLIST_SIZE = "anyMaxSizeHitList=10"; // 10 <=> what we can display on one page
    private static final String DEPARTURE_QUERY_TYPE = "type_dm=stop";
    private static final String DEPARTURE_QUERY_COMMON = "itOptionsActive=1&ptOptionsActive=1&mergeDep=1&useAllStops=1&mode=direct";

    private static final String STATION_SEARCH = "XML_STOPFINDER_REQUEST";
    private static final String STATION_SEARCH_CONST;
    private static final String[] STATION_SEARCH_CONST_PARAMS = {
            OUTPUT_FORMAT, STATELESS, COORD_OUTPUT_FORMAT, LOCATION_SERVER,
            STATION_SEARCH_TYPE, STATION_SEARCH_COMMON, STATION_SEARCH_HITLIST_SIZE
    };

    private static final String DEPARTURE_QUERY = "XSLT_DM_REQUEST";
    private static final String DEPARTURE_QUERY_CONST;
    private static final String[] DEPARTURE_QUERY_CONST_PARAMS = {
            OUTPUT_FORMAT, STATELESS, COORD_OUTPUT_FORMAT, DEPARTURE_QUERY_TYPE,
            DEPARTURE_QUERY_COMMON
    };

    // Set the following parameters before appending
    private static final String LANGUAGE = "language=";
    private static final String STATION_SEARCH_QUERY = "name_sf=";
    private static final String DEPARTURE_QUERY_STATION = "name_dm=";

    static {
        StringBuilder stationSearch = new StringBuilder(MVV_API_BASE);
        stationSearch.append(STATION_SEARCH).append('?');
        for (String param : STATION_SEARCH_CONST_PARAMS) {
            stationSearch.append(param).append('&');
        }
        STATION_SEARCH_CONST = stationSearch.toString();

        StringBuilder departureQuery = new StringBuilder(MVV_API_BASE);
        departureQuery.append(DEPARTURE_QUERY).append('?');
        for (String param : DEPARTURE_QUERY_CONST_PARAMS) {
            departureQuery.append(param).append('&');
        }
        DEPARTURE_QUERY_CONST = departureQuery.toString();
    }

    /**
     * Get all departures for a station.
     * Cursor includes target station name, departure in remaining minutes.
     *
     * @param station Station name
     * @return List of departures
     */
    public static List<Departure> getDeparturesFromExternal(Context context, String station) {
        try {
            String language = LANGUAGE + Locale.getDefault().getLanguage();
            // ISO-8859-1 is needed for mvv
            String departureQuery = DEPARTURE_QUERY_STATION + URLEncoder.encode(station, "ISO-8859-1");

            String query = DEPARTURE_QUERY_CONST + language + '&' + departureQuery;
            Utils.logv(query);

            // Download departures
            JSONArray departures = NetUtils.downloadJson(context, query).optJSONArray("departureList");
            if (departures == null) {
                return null;
            }

            ArrayList<Departure> result = new ArrayList<>(departures.length());
            for (int i = 0; i < departures.length(); i++) {
                JSONObject departure = departures.getJSONObject(i);
                JSONObject servingLine = departure.getJSONObject("servingLine");
                result.add(new Departure(
                        servingLine.getString("name"),
                        servingLine.getString("direction"),
                        // Limit symbol length to 3, longer symbols are pointless
                        String.format("%3.3s", servingLine.getString("symbol")).trim(),
                        departure.getInt("countdown")
                ));
            }

            Collections.sort(result, new Comparator<Departure>() {
                @Override
                public int compare(Departure lhs, Departure rhs) {
                    return lhs.countDown - rhs.countDown;
                }
            });
            return result;

        } catch (UnsupportedEncodingException e) {
            throw new Error(e); // Programming error. Fail hard.
        } catch (IOException | JSONException e) {
            //We got no valid JSON, mvg-live is probably bugged
            Utils.log(e, "invalid JSON from mvv " + DEPARTURE_QUERY);
            return null;
        }
    }

    public static class Departure {
        public String servingLine;
        public String direction;
        public String symbol;
        public int countDown;

        public Departure(String servingLine, String direction, String symbol, int countDown) {
            this.servingLine = servingLine;
            this.direction = direction;
            this.symbol = symbol;
            this.countDown = countDown;
        }
    }

    /**
     * Find stations by station name prefix
     *
     * @param prefix Name prefix
     * @return Database Cursor (name, _id)
     */
    public static Cursor getStationsFromExternal(Context context, String prefix) {
        try {
            String language = LANGUAGE + Locale.getDefault().getLanguage();
            // ISO-8859-1 is needed for mvv
            String stationQuery = STATION_SEARCH_QUERY + URLEncoder.encode(prefix, "ISO-8859-1");

            String query = STATION_SEARCH_CONST + language + '&' + stationQuery;
            Utils.log(query);

            // Download possinble stations
            JSONObject jsonObj = NetUtils.downloadJson(context, query);
            if (jsonObj == null) {
                return null;
            }

            MatrixCursor mc = new MatrixCursor(new String[]{"name", "_id"});
            JSONObject stopfinder = jsonObj.getJSONObject("stopFinder");

            // Possible values for points: Object, Array or null
            JSONArray pointsArray = stopfinder.optJSONArray("points");
            if (pointsArray != null) {
                //TODO: sorting
                for (int i = 0; i < pointsArray.length(); i++) {
                    JSONObject point = pointsArray.getJSONObject(i);
                    String station = point.getString("name");
                    String id = point.getString("stateless");
                    mc.addRow(new String[]{station, id});
                }
            } else {
                JSONObject points = stopfinder.optJSONObject("points");
                if (points == null) {
                    return null;
                }
                JSONObject point = points.getJSONObject("point");
                String station = point.getString("name");
                String id = point.getString("stateless");
                mc.addRow(new String[]{station, id});
            }
            return mc;
        } catch (UnsupportedEncodingException e) {
            throw new Error(e); // Programming error. Fail hard.
        } catch (JSONException | IOException e) {
            Utils.log(e, "invalid JSON from mvv " + STATION_SEARCH);
        }
        return null;
    }

    /**
     * Inserts a MVV card for the nearest public transport station
     *
     * @param context Context
     */
    @Override
    public void onRequestCard(Context context) {
        if (!NetUtils.isConnected(context)) {
            return;
        }

        // Get station for current campus
        final String station = new LocationManager(context).getStation();
        if (station == null) {
            return;
        }

        List<Departure> cur = getDeparturesFromExternal(context, station);
        if (cur == null) {
            return;
        }
        MVVCard card = new MVVCard(context);
        card.setStation(station);
        card.setDepartures(cur);
        card.apply();

    }
}
