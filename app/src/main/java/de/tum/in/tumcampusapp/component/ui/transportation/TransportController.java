package de.tum.in.tumcampusapp.component.ui.transportation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.google.common.base.Optional;
import com.google.common.net.UrlEscapers;
import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.other.general.model.Recent;
import de.tum.in.tumcampusapp.component.other.locations.LocationManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.component.ui.transportation.model.TransportFavorites;
import de.tum.in.tumcampusapp.component.ui.transportation.model.WidgetsTransport;
import de.tum.in.tumcampusapp.component.ui.transportation.model.efa.Departure;
import de.tum.in.tumcampusapp.component.ui.transportation.model.efa.StationResult;
import de.tum.in.tumcampusapp.component.ui.transportation.model.efa.WidgetDepartures;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Transport Manager, handles querying data from mvv and card creation
 */
@SuppressWarnings("StringConcatenationMissingWhitespace")
public class TransportController implements ProvidesCard {

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
    private static final String STATION_SEARCH_TYPE = "type_sf=stop";
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

    private static SparseArray<WidgetDepartures> widgetDeparturesList;

    private Context mContext;
    private final TransportDao transportDao;

    static {
        StringBuilder stationSearch = new StringBuilder(MVV_API_BASE);
        stationSearch.append(STATION_SEARCH)
                .append('?');
        for (String param : STATION_SEARCH_CONST_PARAMS) {
            stationSearch.append(param)
                    .append('&');
        }
        STATION_SEARCH_CONST = stationSearch.toString();

        StringBuilder departureQuery = new StringBuilder(MVV_API_BASE);
        departureQuery.append(DEPARTURE_QUERY)
                .append('?');
        for (String param : DEPARTURE_QUERY_CONST_PARAMS) {
            departureQuery.append(param)
                    .append('&');
        }
        DEPARTURE_QUERY_CONST = departureQuery.toString();
    }

    public TransportController(Context context) {
        mContext = context;
        TcaDb tcaDb = TcaDb.getInstance(context);
        transportDao = tcaDb.transportDao();

        if (TransportController.widgetDeparturesList == null) {
            TransportController.widgetDeparturesList = new SparseArray<>();
        }

    }

    /**
     * Check if the transport symbol is one of the user's favorites.
     *
     * @param symbol The transport symbol
     * @return True, if favorite
     */
    public boolean isFavorite(String symbol) {
        return transportDao.isFavorite(symbol);
    }

    /**
     * Adds a transport symbol to the list of the user's favorites.
     *
     * @param symbol The transport symbol
     */
    public void addFavorite(String symbol) {
        TransportFavorites transportFavorites = new TransportFavorites();
        transportFavorites.setSymbol(symbol);
        transportDao.addFavorite(transportFavorites);
    }

    /**
     * Delete a user's favorite transport symbol.
     *
     * @param symbol The transport symbol
     */
    public void deleteFavorite(String symbol) {
        transportDao.deleteFavorite(symbol);
    }

    /**
     * Adds the settingsPrefix of a widget to the widget list, replaces the existing settingsPrefix if there are some
     */
    public void addWidget(int appWidgetId, WidgetDepartures widgetDepartures) {
        WidgetsTransport widgetsTransport = new WidgetsTransport();
        widgetsTransport.setId(appWidgetId);
        widgetsTransport.setStation(widgetDepartures.getStation());
        widgetsTransport.setStationId(widgetDepartures.getStationId());
        widgetsTransport.setLocation(widgetDepartures.getUseLocation());
        widgetsTransport.setReload(widgetDepartures.getAutoReload());
        transportDao.replaceWidget(widgetsTransport);
        TransportController.widgetDeparturesList.put(appWidgetId, widgetDepartures);
    }

    /**
     * Deletes the settingsPrefix of a widget to the widget list
     *
     * @param widgetId The id of the widget
     */
    public void deleteWidget(int widgetId) {
        transportDao.deleteWidget(widgetId);
        TransportController.widgetDeparturesList.remove(widgetId);
    }

    /**
     * A WidgetDepartures Object containing the settingsPrefix of this widget.
     * This object can provide the departures needed by this widget as well.
     * The settingsPrefix are cached, only the first time its loded from the database.
     * If there is no widget with this id saved (in cache and the database) a new WidgetDepartures Object is generated
     * containing a NULL for the station and an empty string for the station id. This is not cached or saved to the database.
     *
     * @param widgetId The id of the widget
     * @return The WidgetDepartures Object
     */
    public WidgetDepartures getWidget(int widgetId) {
        if (TransportController.widgetDeparturesList.indexOfKey(widgetId) >= 0) {
            return TransportController.widgetDeparturesList.get(widgetId);
        }
        WidgetDepartures widgetDepartures;
        WidgetsTransport widgetsTransports = transportDao.getAllWithId(widgetId);
        widgetDepartures = new WidgetDepartures();
        if (widgetsTransports != null) {
            widgetDepartures.setStation(widgetsTransports.getStation());
            widgetDepartures.setStationId(widgetsTransports.getStationId());
            widgetDepartures.setUseLocation(widgetsTransports.getLocation());
            widgetDepartures.setAutoReload(widgetsTransports.getReload());
        }
        TransportController.widgetDeparturesList.put(widgetId, widgetDepartures);
        return widgetDepartures;
    }

    /**
     * Get all departures for a station.
     *
     * @param stationID Station ID, station name might or might not work
     * @return List of departures
     */
    public static List<Departure> getDeparturesFromExternal(Context context, String stationID) {
        List<Departure> result = new ArrayList<>();
        try {
            String language = LANGUAGE + Locale.getDefault()
                    .getLanguage();
            // ISO-8859-1 is needed for mvv
            String departureQuery = DEPARTURE_QUERY_STATION + UrlEscapers.urlPathSegmentEscaper()
                    .escape(stationID);

            String query = DEPARTURE_QUERY_CONST + language + '&' + departureQuery;
            Utils.logv(query);
            NetUtils net = new NetUtils(context);

            // Download departures
            Optional<JSONObject> departures = net.downloadJson(query);
            if (!departures.isPresent()) {
                return result;
            }

            if (departures.get()
                    .isNull("departureList")) {
                return result;
            }

            JSONArray arr = departures.get()
                    .getJSONArray("departureList");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject departure = arr.getJSONObject(i);
                JSONObject servingLine = departure.getJSONObject("servingLine");
                JSONObject time = departure.getJSONObject("dateTime");
                DateTime date = new DateTime()
                        .withDate(time.getInt("year"), time.getInt("month"), time.getInt("day"))
                        .withTime(time.getInt("hour"), time.getInt("minute"), 0, 0);
                result.add(new Departure(
                        servingLine.getString("name"),
                        servingLine.getString("direction"),
                        // Limit symbol length to 3, longer symbols are pointless
                        String.format("%3.3s", servingLine.getString("symbol"))
                                .trim(),
                        departure.getInt("countdown"),
                        date
                ));
            }

            Collections.sort(result, (lhs, rhs) -> Integer.compare(lhs.getCountDown(), rhs.getCountDown()));
        } catch (JSONException e) {
            //We got no valid JSON, mvg-live is probably bugged
            Utils.log(e, ERROR_INVALID_JSON + DEPARTURE_QUERY);
        } catch (Exception e) {
            Utils.log(e);
        }

        return result;
    }

    /**
     * Find stations by station name prefix
     *
     * @param prefix Name prefix
     * @return List of StationResult
     */
    public static List<StationResult> getStationsFromExternal(Context context, String prefix) {
        prefix = Utils.escapeUmlauts(prefix);
        try {
            String language = LANGUAGE + Locale.getDefault()
                    .getLanguage();
            // ISO-8859-1 is needed for mvv
            String stationQuery = STATION_SEARCH_QUERY + UrlEscapers.urlPathSegmentEscaper()
                    .escape(prefix);

            String query = STATION_SEARCH_CONST + language + '&' + stationQuery;
            Utils.log(query);
            NetUtils net = new NetUtils(context);

            // Download possible stations
            Optional<JSONObject> jsonObj = net.downloadJsonObject(query, 0, true);
            if (!jsonObj.isPresent()) {
                return Collections.emptyList();
            }

            List<StationResult> results = new ArrayList<>();
            JSONObject stopfinder = jsonObj.get()
                    .getJSONObject("stopFinder");

            // Possible values for points: Object, Array or null
            JSONArray pointsArray = stopfinder.optJSONArray(POINTS);
            if (pointsArray == null) {
                JSONObject points = stopfinder.optJSONObject(POINTS);
                if (points == null) {
                    return Collections.emptyList();
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
            Collections.sort(results, (lhs, rhs) -> Integer.compare(rhs.getQuality(), lhs.getQuality()));

            return results;
        } catch (JSONException e) {
            Utils.log(e, ERROR_INVALID_JSON + STATION_SEARCH);
        }
        return Collections.emptyList();
    }

    private static void addStationResult(Collection<StationResult> results, JSONObject point) throws JSONException {
        String name = point.getString("name");
        String id = point.getJSONObject("ref")
                .getString("id");
        int quality = (point.has("quality")) ? point.getInt("quality") : 0;
        results.add(new StationResult(name, id, quality));
    }

    @NotNull
    @Override
    public List<Card> getCards(@NonNull CacheControl cacheControl) {
        List<Card> results = new ArrayList<>();
        if (!NetUtils.isConnected(mContext)) {
            return results;
        }

        // Get station for current campus
        LocationManager locMan = new LocationManager(mContext);
        StationResult station = locMan.getStation();
        if (station == null) {
            return results;
        }

        List<Departure> departures = getDeparturesFromExternal(mContext, station.getId());
        MVVCard card = new MVVCard(mContext);
        card.setStation(station);
        card.setDepartures(departures);
        results.add(card.getIfShowOnStart());

        return results;
    }

    public static List<StationResult> getRecentStations(List<Recent> recents) {
        List<StationResult> stationResults = new ArrayList<>(recents.size());
        for (Recent r : recents) {
            try {
                stationResults.add(StationResult.Companion.fromRecent(r));
            } catch (JsonSyntaxException ignore) {
                // We don't care about deserialization errors
            }
        }
        return stationResults;
    }
}
