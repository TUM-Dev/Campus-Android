package de.tum.in.tumcampusapp.models.efa;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.tum.in.tumcampusapp.managers.TransportManager;
import de.tum.in.tumcampusapp.widgets.MVVWidget;

public class WidgetDepartures {

    private String station;
    private String stationId;
    private boolean useLocation;
    private boolean autoReload;
    private List<Departure> departures;
    private long lastLoad;
    private boolean isOffline = false;

    /**
     * Create a new WidgetDepartures. It contains the widget settings and can load the according departure list
     */
    public WidgetDepartures() {
        this.station = "";
        this.stationId = "";
        this.autoReload = false;
        this.useLocation = false;
        this.departures = new ArrayList<>();
    }

    /**
     * The stationId which is set for this widget
     *
     * @return The station name
     */
    public String getStationId() {
        return this.stationId;
    }

    /**
     * Sets a stationId for this widget
     *
     * @param stationId The station name
     */
    public void setStationId(String stationId) {
        if (!this.stationId.equals(stationId)) {
            this.departures.clear();
        }
        this.stationId = stationId;
    }

    /**
     * The station which is set for this widget
     *
     * @return The station name
     */
    public String getStation() {
        if (this.useLocation) {
            // TODO implement nearest station (replace the stationId string with the calculated station)
            this.station = "use location";
        }
        return this.station;
    }

    /**
     * Sets a station title for this widget
     *
     * @param station The station name
     */
    public void setStation(String station) {
        this.station = station;
    }

    /**
     * Whether this widgets station is determined by the current location
     *
     * @return True if location is used
     */
    public boolean useLocation() {
        return useLocation;
    }

    /**
     * Whether this widgets station should determined by the current location
     *
     * @param useLocation True is location has to be used
     */
    public void setUseLocation(boolean useLocation) {
        this.useLocation = useLocation;
    }

    /**
     * True if widget should update automatically, otherwise a button-press is required
     *
     * @return Whether autoReload is enabled
     */
    public boolean autoReload() {
        return this.autoReload;
    }

    /**
     * True if widget should update automatically, otherwise a button-press is required
     *
     * @param autoReload Whether autoReload should enabled
     */
    public void setAutoReload(boolean autoReload) {
        this.autoReload = autoReload;
    }

    /**
     * Are the departure information older than two minutes (because of any connection problems)
     *
     * @return True if only offline data available
     */
    public boolean isOffline() {
        return this.isOffline;
    }

    /**
     * Get the list of departures for this widget, download them if they are not cached
     *
     * @return The list of departures
     */
    public List<Departure> getDepartures(Context context, boolean forceServerLoad) {
        if (this.departures == null) {
            this.departures = new ArrayList<>();
        }
        // download only id there is no data or the last loading is more than X min ago
        if (this.departures.size() == 0 || forceServerLoad || (this.autoReload() && System.currentTimeMillis() - this.lastLoad > MVVWidget.DOWNLOAD_DELAY)) {
            List<Departure> departures = TransportManager.getDeparturesFromExternal(context, this.getStationId());
            if (departures.size() == 0) {
                this.isOffline = true;
            } else {
                this.departures = departures;
                this.lastLoad = System.currentTimeMillis();
                this.isOffline = false;
            }
        }

        // remove Departures which have a negative countdown
        for (Iterator<Departure> iterator = this.departures.iterator(); iterator.hasNext(); ) {
            Departure departure = iterator.next();
            if (departure.getCalculatedCountDown() < 0) {
                iterator.remove();
            }
        }
        return this.departures;
    }
}
