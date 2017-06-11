package de.tum.in.tumcampusapp.models.efa;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.tum.in.tumcampusapp.managers.TransportManager;
import de.tum.in.tumcampusapp.widgets.MVVWidget;

public class WidgetDepartures {

    private String station;
    private String station_id;
    private boolean use_location;
    private boolean auto_reload;
    private List<Departure> departures;
    private long last_load;
    private boolean is_offline = false;

    /**
     * Create a new WidgetDepartures. It contains the widget settings and can load the according departure list
     */
    public WidgetDepartures() {
        this.station = "";
        this.station_id = "";
        this.auto_reload = false;
        this.use_location = false;
        this.departures = new ArrayList<>();
    }

    /**
     * The station_id which is set for this widget
     *
     * @return The station name
     */
    public String getStationId() {
        return this.station_id;
    }

    /**
     * Sets a station_id for this widget
     *
     * @param station_id The station name
     */
    public void setStationId(String station_id) {
        if (!this.station_id.equals(station_id)) {
            this.departures.clear();
        }
        this.station_id = station_id;
    }

    /**
     * The station which is set for this widget
     *
     * @return The station name
     */
    public String getStation() {
        if (this.use_location) {
            // TODO implement nearest station (replace the station_id string with the calculated station)
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
        return use_location;
    }

    /**
     * Whether this widgets station should determined by the current location
     *
     * @param use_location True is location has to be used
     */
    public void setUseLocation(boolean use_location) {
        this.use_location = use_location;
    }

    /**
     * True if widget should update automatically, otherwise a button-press is required
     *
     * @return Whether auto_reload is enabled
     */
    public boolean autoReload() {
        return this.auto_reload;
    }

    /**
     * True if widget should update automatically, otherwise a button-press is required
     *
     * @param auto_reload Whether auto_reload should enabled
     */
    public void setAutoReload(boolean auto_reload) {
        this.auto_reload = auto_reload;
    }

    /**
     * Are the departure information older than two minutes (because of any connection problems)
     *
     * @return True if only offline data available
     */
    public boolean isOffline() {
        return this.is_offline;
    }

    /**
     * Get the list of departures for this widget, download them if they are not cached
     *
     * @return The list of departures
     */
    public List<Departure> getDepartures(Context context, boolean force_server_load) {
        if (this.departures == null) {
            this.departures = new ArrayList<>();
        }
        // download only id there is no data or the last loading is more than X min ago
        if (this.departures.size() == 0 || force_server_load || (this.autoReload() && System.currentTimeMillis() - this.last_load > MVVWidget.DOWNLOAD_DELAY)) {
            List<Departure> departures = TransportManager.getDeparturesFromExternal(context, this.getStationId());
            if (departures.size() == 0) {
                this.is_offline = true;
            } else {
                this.departures = departures;
                this.last_load = System.currentTimeMillis();
                this.is_offline = false;
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
