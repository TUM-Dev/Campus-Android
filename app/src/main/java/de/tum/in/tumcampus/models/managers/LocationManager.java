package de.tum.in.tumcampus.models.managers;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.Cafeteria;
import de.tum.in.tumcampus.models.Geo;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequest;

/**
 * Location manager, manages intelligent location services, provides methods to easily access
 * the users current location, campus, next public transfer station and best cafeteria
 */
public class LocationManager {
    private static final double[][] campusLocations = {
            {48.2648424, 11.6709511}, // Garching Forschungszentrum
            {48.249432, 11.633905}, // Garching Hochbrück
            {48.397990, 11.722727}, // Weihenstephan
            {48.149436, 11.567635}, // Stammgelände
            {48.110847, 11.4703001}, // Klinikum Großhadern
            {48.137539, 11.601119}, // Klinikum rechts der Isar
            {48.155916, 11.583095}, // Leopoldstraße
            {48.150244, 11.580665} // Geschwister Schollplatz/Adalbertstraße
    };
    private static final String[] campusShort = {
            "G", // Garching Forschungszentrum
            "H", // Garching Hochbrück
            "W", // Weihenstephan
            "C", // Stammgelände
            "K", // Klinikum Großhadern
            "I", // Klinikum rechts der Isar
            "L", // Leopoldstraße
            "S" // Geschwister Schollplatz/Adalbertstraße
    };
    private static final String[] defaultCampusStation = {
            "Garching-Forschungszentrum",
            "Garching-Hochbrück",
            "Weihenstephan",
            "Theresienstraße",
            "Klinikum Großhadern",
            "Max-Weber-Platz",
            "Giselastraße",
            "Universität"
    };
    private static final String[] defaultCampusCafeteria = {"422", null, "423", "421", "414", null, "411", null};
    private final Context mContext;

    public LocationManager(Context c) {
        mContext = c;
    }

    /**
     * Tests if Google Play services is available and than gets last known position
     *
     * @return Returns the more or less current position or null on failure
     */
    Location getCurrentLocation() {
        if (servicesConnected()) {
            return getLastLocation();
        }

        // If location services are not available use default location if set
        final String defaultCampus = Utils.getSetting(mContext, Const.DEFAULT_CAMPUS, "G");
        if (!defaultCampus.equals("X")) {
            for (int i = 0; i < campusShort.length; i++) {
                if (campusShort[i].equals(defaultCampus)) {
                    Location location = new Location("defaultLocation");
                    location.setLatitude(campusLocations[i][0]);
                    location.setLongitude(campusLocations[i][1]);
                    return location;
                }
            }
        }
        return null;
    }

    /**
     * Returns the "id" of the current campus
     *
     * @return Campus id
     */
    int getCurrentCampus() {
        Location loc = getCurrentLocation();
        if (loc == null)
            return -1;
        return getCampusFromLocation(loc);
    }

    /**
     * Returns the "id" of the campus near the given location
     * The used radius around the middle of the campus is 1km.
     *
     * @param location The location to search for a campus
     * @return Campus id
     */
    private static int getCampusFromLocation(Location location) {
        final double lat = location.getLatitude();
        final double lng = location.getLongitude();
        float results[] = new float[1];
        float bestDistance = Float.MAX_VALUE;
        int bestCampus = -1;
        for (int i = 0; i < campusLocations.length; i++) {
            Location.distanceBetween(campusLocations[i][0], campusLocations[i][1], lat, lng, results);
            float distance = results[0];
            if (distance < bestDistance) {
                bestDistance = distance;
                bestCampus = i;
            }
        }
        if (bestDistance < 1000) {
            return bestCampus;
        } else {
            return -1;
        }
    }

    /**
     * Returns the cafeteria's identifier which is near the given location
     * The used radius around the cafeteria is 1km.
     *
     * @return Campus id
     */
    public List<Cafeteria> getCafeterias() {
        // Get current location
        Location location = getCurrentOrNextLocation();

        final double lat = location.getLatitude();
        final double lng = location.getLongitude();
        float results[] = new float[1];
        CafeteriaManager manager = new CafeteriaManager(mContext);
        Cursor cur = manager.getAllFromDb();
        List<Cafeteria> list = new ArrayList<>(cur.getCount());

        if (cur.moveToFirst()) {
            do {
                Cafeteria cafe = new Cafeteria(cur.getInt(0), cur.getString(1),
                        cur.getString(2), cur.getDouble(3), cur.getDouble(4));
                Location.distanceBetween(cur.getDouble(3), cur.getDouble(4), lat, lng, results);
                cafe.distance = results[0];
                list.add(cafe);
            } while (cur.moveToNext());
        }
        cur.close();
        Collections.sort(list);
        return list;
    }

    /**
     * Gets the current location and if it is not available guess
     * by querying for the next lecture.
     *
     * @return Any of the above described locations.
     */
    private
    @NonNull
    Location getCurrentOrNextLocation() {
        Location l = getCurrentLocation();
        if (l != null) {
            return l;
        }
        return getNextLocation();
    }

    /**
     * Returns the last known location of the device
     *
     * @return The last location
     */
    Location getLastLocation() {
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;
        long minTime = 0;

        //Check Location permission for Android 6.0
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        android.location.LocationManager locationManager = (android.location.LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        List<String> matchingProviders = locationManager.getAllProviders();
        for (String provider : matchingProviders) {

            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if ((time > minTime && accuracy < bestAccuracy)) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                } else if (time < minTime &&
                        bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }
        return bestResult;
    }

    /**
     * Returns the name of the station that is nearby and/or set by the user
     *
     * @return Name of the station or null if the user is not near any campus
     */
    public String getStation() {
        int campus = getCurrentCampus();
        if (campus == -1)
            return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final String defaultVal = defaultCampusStation[campus];
        return prefs.getString("card_stations_default_" + campusShort[campus], defaultVal);
    }

    /**
     * Checks that Google Play services are available
     */
    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            Utils.log("Google Play services is NOT available.");
            return false;
        }
    }

    /**
     * Gets the campus you are currently on or if you are at home or wherever
     * query for your next lecture and find out at which campus it takes place
     */
    int getCurrentOrNextCampus() {
        int campus = getCurrentCampus();
        if (campus != -1)
            return campus;
        return getNextCampus();
    }

    /**
     * Provides some intelligence to pick one cafeteria to show
     */
    public int getCafeteria() {
        int campus = getCurrentOrNextCampus();
        if (campus != -1) { // If the user is in university or a lecture has been recognized
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String defaultVal = defaultCampusCafeteria[campus];
            String cafeteria = prefs.getString("card_cafeteria_default_" + campusShort[campus], defaultVal);
            if (cafeteria != null) {
                return Integer.parseInt(cafeteria);
            }
        }

        // Get nearest cafeteria
        List<Cafeteria> list = getCafeterias();
        if (list != null && list.size() > 0) {
            return list.get(0).id;
        } else {
            return -1;
        }
    }

    /**
     * Queries your calender and gets the campus at which your next lecture takes place
     */
    int getNextCampus() {
        return getCampusFromLocation(getNextLocation());
    }

    /**
     * Gets the location of the next room where the user has a lecture.
     * If no lectures are available Garching will be returned
     *
     * @return Location of the next lecture room
     */
    private Location getNextLocation() {
        CalendarManager manager = new CalendarManager(mContext);
        Geo geo = manager.getNextCalendarItemGeo();
        Location location = new Location("roomfinder");
        if (geo != null) {
            location.setLatitude(Double.parseDouble(geo.getLatitude()));
            location.setLongitude(Double.parseDouble(geo.getLongitude()));
        } else {
            location.setLatitude(48.2648424);
            location.setLongitude(11.6709511);
        }
        return location;
    }

    /**
     * Translates room title to Geo
     * HINT: Don't call from UI thread
     *
     * @param loc Room title
     * @return Location or null on failure
     */
    public Geo roomLocationStringToGeo(String loc) {
        TUMRoomFinderRequest requestHandler = new TUMRoomFinderRequest(mContext);
        if (loc.contains("(")) {
            loc = loc.substring(0, loc.indexOf('(')).trim();
        }

        ArrayList<HashMap<String, String>> request = requestHandler.fetchRooms(loc);
        if (request.size() > 0) {
            String room = request.get(0).get(TUMRoomFinderRequest.KEY_ARCHITECT_NUMBER);
            return requestHandler.fetchCoordinates(room);
        }
        return null;
    }
}
