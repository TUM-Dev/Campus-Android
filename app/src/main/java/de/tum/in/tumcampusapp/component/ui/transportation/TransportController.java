package de.tum.in.tumcampusapp.component.ui.transportation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.other.general.model.Recent;
import de.tum.in.tumcampusapp.component.other.locations.LocationManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.component.ui.transportation.api.MvvClient;
import de.tum.in.tumcampusapp.component.ui.transportation.api.MvvDeparture;
import de.tum.in.tumcampusapp.component.ui.transportation.api.MvvDepartureList;
import de.tum.in.tumcampusapp.component.ui.transportation.api.MvvStationList;
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
public class TransportController implements ProvidesCard {

    // TODO Set the following parameters before appending
    private static final String LANGUAGE = "language=";
    private static final String STATION_SEARCH_QUERY = "name_sf=";
    private static final String POINTS = "points";

    private static SparseArray<WidgetDepartures> widgetDeparturesList;

    private Context mContext;
    private final TransportDao transportDao;


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
        try {
            MvvDepartureList mvvDepartures = MvvClient.getInstance(context)
                    .getDepartures(stationID).execute().body();
            if (mvvDepartures == null) {
                return Collections.emptyList();
            }

            List<Departure> result = new ArrayList<>(mvvDepartures.getDepartureList().size());
            for (MvvDeparture departure : mvvDepartures.getDepartureList()) {
                result.add(new Departure(
                        departure.getServingLine().getName(),
                        departure.getServingLine().getDirection(),
                        departure.getServingLine().getSymbol(),
                        departure.getCountdown(),
                        departure.getDateTime()
                ));
            }

            Collections.sort(result, (lhs, rhs) -> Integer.compare(lhs.getCountDown(), rhs.getCountDown()));
            return result;
        } catch (Exception e) {
            Utils.log(e);
        }
        return Collections.emptyList();
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
            MvvStationList stationList = MvvClient.getInstance(context)
                    .getStations(prefix).execute().body();

            if (stationList == null) {
                return Collections.emptyList();
            }

            List<StationResult> results = stationList.getDepartureList();

            // Sort by quality
            Collections.sort(results, (lhs, rhs) -> Integer.compare(rhs.getQuality(), lhs.getQuality()));

            return results;
        } catch (Exception e) {
            Utils.log(e);
        }
        return Collections.emptyList();
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

    public static List<StationResult> getRecentStations(Collection<Recent> recents) {
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
