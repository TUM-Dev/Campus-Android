package de.tum.in.tumcampusapp.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Pair;
import android.util.SparseArray;

import com.google.common.base.Optional;
import com.google.common.net.UrlEscapers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.MVVCard;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.widgets.MVVWidget;

/**
 * Transport Manager, handles querying data from mvv and card creation
 */
public class TransportManager extends AbstractManager implements Card.ProvidesCard {

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
    private static final String STATION_SEARCH_TYPE_COORD = "type_sf=coord";
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
    private static final String POINTS = "points";
    private static final String ERROR_INVALID_JSON = "invalid JSON from mvv ";

    private static SparseArray<WidgetDepartures> widgetDepartures;

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

    public TransportManager(Context context) {
        super(context);

        // Create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS transport_favorites (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, symbol VARCHAR)");

        db.execSQL("CREATE TABLE IF NOT EXISTS transport_widgets (" +
                "id INTEGER PRIMARY KEY, station VARCHAR, station_id VARCHAR, location BOOLEAN)");

        if(TransportManager.widgetDepartures == null) {
            TransportManager.widgetDepartures = new SparseArray<>();
        }
    }

    /**
     * Check if the transport symbol is one of the user's favorites.
     * @param symbol The transport symbol
     * @return True, if favorite
     */
    public boolean isFavorite(String symbol) {
        return db.rawQuery("SELECT * FROM transport_favorites WHERE symbol = ?", new String[]{symbol}).getCount() > 0;
    }

    /**
     * Adds a transport symbol to the list of the user's favorites.
     * @param symbol The transport symbol
     */
    public void addFavorite(String symbol) {
        db.execSQL("INSERT INTO transport_favorites (symbol) VALUES (?)", new String[]{symbol});
    }

    /**
     * Delete a user's favorite transport symbol.
     * @param symbol The transport symbol
     */
    public void deleteFavorite(String symbol) {
        db.execSQL("DELETE FROM transport_favorites WHERE symbol = ?", new String[]{symbol});
    }

    /**
     * Adds the settings of a widget to the widget list, replaces the existing settings if there are some
     *
     * @param widget_id    The id of the widget
     * @param station      The station id which will be displayed on the widget
     * @param use_location True if not a fixed station is displayed but the nearest station
     */
    public void addWidget(int widget_id, String station, String station_id, boolean use_location) {
        ContentValues values = new ContentValues();
        values.put("id", widget_id);
        values.put("station", station);
        values.put("station_id", station_id);
        values.put("location", use_location);
        db.replace("transport_widgets", null, values);
        TransportManager.widgetDepartures.remove(widget_id);
    }

    /**
     * Deletes the settings of a widget to the widget list
     *
     * @param widget_id The id of the widget
     */
    public void deleteWidget(int widget_id) {
        db.delete("transport_widgets", "id = ?", new String[]{String.valueOf(widget_id)});
        TransportManager.widgetDepartures.remove(widget_id);
    }

    /**
     * A WidgetDepartures Object containing the settings of this widget.
     * This object can provide the departures needed by this widget as well.
     * The settings are cached, only the first time its loded from the database.
     * If there is no widget with this id saved (in cache and the database) a new WidgetDepartures Object is generated
     * containing a NULL for the station and an empty string for the station id. This is not cached or saved to the database.
     *
     * @param widget_id The id of the widget
     * @return The WidgetDepartures Object
     */
    public WidgetDepartures getWidget(int widget_id) {
        if(TransportManager.widgetDepartures.indexOfKey(widget_id) >= 0){
            return TransportManager.widgetDepartures.get(widget_id);
        }
        Cursor c = db.rawQuery("SELECT * FROM transport_widgets WHERE id = ?", new String[]{String.valueOf(widget_id)});
        String station = null;
        String station_id = "";
        if (c.getCount() >= 1) {
            c.moveToFirst();
            station = c.getString(c.getColumnIndex("station"));
            station_id = c.getString(c.getColumnIndex("station_id"));
            boolean use_location = c.getInt(c.getColumnIndex("location")) != 0;
            c.close();
            if (use_location) {
                // TODO implement nearest station (replace the station_id string with the calculated station)
                station = "use location";
            }
        }
        WidgetDepartures wD = new WidgetDepartures(station, station_id);
        TransportManager.widgetDepartures.put(widget_id, wD);
        return wD;
    }

    /**
     * Get all departures for a station.
     * Cursor includes target station name, departure in remaining minutes.
     *
     * @param stationID Station ID, station name might or might not work
     * @return List of departures
     */
    public static List<Departure> getDeparturesFromExternal(Context context, String stationID) {
        List<Departure> result = new ArrayList<>();
        try {
            String language = LANGUAGE + Locale.getDefault().getLanguage();
            // ISO-8859-1 is needed for mvv
            String departureQuery = DEPARTURE_QUERY_STATION + UrlEscapers.urlPathSegmentEscaper().escape(stationID);

            String query = DEPARTURE_QUERY_CONST + language + '&' + departureQuery;
            Utils.logv(query);
            NetUtils net = new NetUtils(context);

            // Download departures
            Optional<JSONObject> departures = net.downloadJson(query);
            if (!departures.isPresent()) {
                return result;
            }

            if (departures.get().isNull("departureList")) {
                return result;
            }

            JSONArray arr = departures.get().getJSONArray("departureList");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject departure = arr.getJSONObject(i);
                JSONObject servingLine = departure.getJSONObject("servingLine");
                JSONObject time = departure.getJSONObject("dateTime");
                Date date = new GregorianCalendar(time.getInt("year"), time.getInt("month") - 1, time.getInt("day"), time.getInt("hour"), time.getInt("minute")).getTime();
                result.add(new Departure(
                        servingLine.getString("name"),
                        servingLine.getString("direction"),
                        // Limit symbol length to 3, longer symbols are pointless
                        String.format("%3.3s", servingLine.getString("symbol")).trim(),
                        departure.getInt("countdown"),
                        date.getTime()
                ));
            }

            Collections.sort(result, new Comparator<Departure>() {
                @Override
                public int compare(Departure lhs, Departure rhs) {
                    return lhs.countDown - rhs.countDown;
                }
            });
        } catch (JSONException e) {
            //We got no valid JSON, mvg-live is probably bugged
            Utils.log(e, ERROR_INVALID_JSON + DEPARTURE_QUERY);
        }
        return result;
    }

    /**
     * Find stations by station name prefix
     *
     * @param prefix Name prefix
     * @return Database Cursor (name, _id)
     */
    public static Optional<Cursor> getStationsFromExternal(Context context, String prefix) {
        try {
            String language = LANGUAGE + Locale.getDefault().getLanguage();
            // ISO-8859-1 is needed for mvv
            String stationQuery = STATION_SEARCH_QUERY + UrlEscapers.urlPathSegmentEscaper().escape(prefix);

            String query = STATION_SEARCH_CONST + language + '&' + stationQuery;
            Utils.log(query);
            NetUtils net = new NetUtils(context);

            // Download possible stations
            Optional<JSONObject> jsonObj = net.downloadJsonObject(query, CacheManager.VALIDITY_DO_NOT_CACHE, true);
            if (!jsonObj.isPresent()) {
                return Optional.absent();
            }

            List<StationResult> results = new ArrayList<>();
            JSONObject stopfinder = jsonObj.get().getJSONObject("stopFinder");

            // Possible values for points: Object, Array or null
            JSONArray pointsArray = stopfinder.optJSONArray(POINTS);
            if (pointsArray == null) {
                JSONObject points = stopfinder.optJSONObject(POINTS);
                if (points == null) {
                    return Optional.absent();
                }
                JSONObject point = points.getJSONObject("point");
                addStationResult(results, point);
            } else {
                for (int i = 0; i < pointsArray.length(); i++) {
                    JSONObject point = pointsArray.getJSONObject(i);
                    addStationResult(results, point);
                }
            }

            //Sort by quality
            Collections.sort(results, new Comparator<StationResult>() {
                @Override
                public int compare(StationResult lhs, StationResult rhs) {
                    return rhs.quality - lhs.quality;
                }
            });

            MatrixCursor mc = new MatrixCursor(new String[]{Const.NAME_COLUMN, Const.ID_COLUMN});
            for (StationResult result : results) {
                mc.addRow(new String[]{result.station, result.id});
            }
            return Optional.of((Cursor) mc);
        } catch (JSONException e) {
            Utils.log(e, ERROR_INVALID_JSON + STATION_SEARCH);
        }
        return Optional.absent();
    }

    private static void addStationResult(Collection<StationResult> results, JSONObject point) throws JSONException {
        results.add(new StationResult(
                point.getString("name"),
                point.getJSONObject("ref").getString("id"),
                point.getInt("quality")
        ));
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
        LocationManager locMan = new LocationManager(context);
        String station = locMan.getStation();
        if (station == null) {
            return;
        }

        List<Departure> cur = getDeparturesFromExternal(context, station);
        MVVCard card = new MVVCard(context);
        card.setStation(new Pair<>(station, station));
        card.setDepartures(cur);
        card.apply();

    }

    public static class Departure {
        final public String servingLine;
        final public String direction;
        final public String symbol;
        final public int countDown;
        final public long departureTime;

        public Departure(String servingLine, String direction, String symbol, int countDown, long departureTime) {
            this.servingLine = servingLine;
            this.direction = direction;
            this.symbol = symbol;
            this.countDown = countDown;
            this.departureTime = departureTime;
        }

        /**
         * Calculates the countDown with the real departure time and the current time
         *
         * @return The calculated countDown in minutes
         */
        public long getCalculatedCountDown(){
            return TimeUnit.MINUTES.convert(departureTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
    }

    public static class StationResult {
        final String station;
        final String id;
        final int quality;

        public StationResult(String station, String id, int quality) {
            this.station = station;
            this.id = id;
            this.quality = quality;
        }
    }

    public static class WidgetDepartures {
        String station;
        String station_id;
        List<Departure> departures;
        long last_load;
        boolean is_offline = false;

        /**
         * Create a new WidgetDepartures. It contains the widget settings and can load the according departure list
         *
         * @param station The station name
         * @param station_id The station id
         */
        WidgetDepartures(String station, String station_id) {
            this.station = station;
            this.station_id = station_id;
        }

        /**
         * The station which is set for this widget
         * @return The station name
         */
        public String getStation() {
            return this.station;
        }

        /**
         * Are the departure information older than two minutes (because of any connection problems)
         *
         * @return True if only offline data available
         */
        public boolean isOffline(){
            return this.is_offline;
        }

        /**
         * Get the list of departures for this widget, download them if they are not cached
         *
         * @return The list of departures
         */
        public List<Departure> getDepartures(Context context) {
            if(this.departures == null){
                this.departures = new ArrayList<>();
            }
            // download only id there is no data or the last loading is more than 2min ago
            if (this.departures.size() == 0 || System.currentTimeMillis() - this.last_load > MVVWidget.DOWNLOAD_DELAY) {
                List<Departure> departures = TransportManager.getDeparturesFromExternal(context, station_id);
                if(departures.size() == 0){
                    this.is_offline = true;
                } else {
                    this.departures = departures;
                    this.last_load = System.currentTimeMillis();
                    this.is_offline = false;
                }
            }

            // remove Departures which have a negative countdown
            for (Iterator<Departure> iterator = this.departures.iterator(); iterator.hasNext();) {
                Departure departure = iterator.next();
                if (departure.getCalculatedCountDown() < 0) {
                    iterator.remove();
                }
            }
            return this.departures;
        }
    }
}
