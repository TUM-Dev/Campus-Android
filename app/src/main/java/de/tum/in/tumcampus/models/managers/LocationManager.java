package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.tum.in.tumcampus.models.Cafeteria;
import de.tum.in.tumcampus.models.CalendarRow;


public class LocationManager {
    private final Context mContext;

    public LocationManager(Context c) {
        mContext = c;
    }

    /**
     * Tests if Google Play services is available and than gets last known position
     *
     * @return Returns the more or less current position or null on failure
     */
    public Location getCurrentLocation() {
        if (servicesConnected()) {
            return getLastLocation();
        }
        return null;
    }

    /**
     * Returns the "id" of the current campus
     *
     * @return Campus id
     */
    public int getCurrentCampus() {
        Location loc = getCurrentLocation();
        if (loc == null)
            return -1;
        return 0; //TODO delete
        //return getCampusFromLocation(loc);
    }

    /**
     * Returns the "id" of the campus near the given location
     * The used radius around the middle of the campus is 1km.
     *
     * @param location The location to search for a campus
     * @return Campus id
     */
    private int getCampusFromLocation(Location location) {
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
        Location location = getCurrentLocation();
        if(location==null)
            return null;

        final double lat = location.getLatitude();
        final double lng = location.getLongitude();
        float results[] = new float[1];
        CafeteriaManager manager = new CafeteriaManager(mContext);
        Cursor cur = manager.getAllFromDb();
        List<Cafeteria> list = new ArrayList<Cafeteria>(cur.getCount());

        if (cur.moveToFirst()) {
            do {
                Cafeteria cafe = new Cafeteria(cur.getInt(0),cur.getString(1),
                        cur.getString(2),cur.getDouble(3), cur.getDouble(4));
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
     * Returns the last known location of the device
     *
     * @return The last location
     */
    public Location getLastLocation() {
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;
        long minTime = 0;

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

    public String getStation() {
        int campus = getCurrentCampus();
        if(campus==-1)
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
            Log.d("Location Updates", "Google Play services is NOT available.");
            return false;
        }
    }

    /**
     * Gets the campus you are currently on or if you are at home or wherever
     * query for your next lecture and find out at which campus it takes place
     * */
     public int getCurrentOrNextCampus() {
        int campus = getCurrentCampus();
        if(campus!=-1)
            return campus;
        return getNextCampus();
    }

    /**
     * Provides some intelligence to pick one cafeteria to show
     * */
    public int getCafeteria() {
        int campus = getCurrentOrNextCampus();
        if(campus!=-1) { // If the user is in university or a lecture has been recognized
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String defaultVal = defaultCampusCafeteria[campus];
            String cafeteria = prefs.getString("card_cafeteria_default_"+campusShort[campus], defaultVal);
            if(cafeteria!=null)
                return Integer.parseInt(cafeteria);
        }

        // Get nearest cafeteria
        List<Cafeteria> list = getCafeterias();
        if(list!=null && list.size()>0)
            return list.get(0).id;
        else
            return -1;
    }

    /**
     * Queries your calender and gets the campus at which your next lecture takes place
     * */
    public int getNextCampus() {
        CalendarManager manager = new CalendarManager(mContext);
        CalendarRow nextLecture = manager.getNextCalendarItem();
        if(nextLecture!=null) {
            // TODO:
            // - nextLecture.getLocation(); of form 00.09.036@5609
            // - query room location from http://vmbaumgarten3.informatik.tu-muenchen.de/roommaps/room/coordinates?id=00.09.036@5609
            // - convert UML based position to latitude/longitude
            // - return getCampusFromLocation(convertedLocation);
        }
        return 0; // TODO Replace this (it is just for testing purposes)
        //return -1;
    }

    /**
     * Converts UTM based coordinates to latitude and longitude based format
     * */
    private Location UTMtoLL(double north, double east, double zone) {
        double d = 0.99960000000000004;
        double d1 = 6378137;
        double d2 = 0.0066943799999999998;
        double d4 = (1 - Math.sqrt(1 - d2)) / (1 + Math.sqrt(1 - d2));
        double d15 = east - 500000;
        double d11 = ((zone - 1) * 6 - 180) + 3;
        double d3 = d2 / (1 - d2);
        double d10 = north / d;
        double d12 = d10 / (d1 * (1 - d2 / 4 - (3 * d2 * d2) / 64 - (5 * Math.pow(d2, 3)) / 256));
        double d14 = d12 + ((3 * d4) / 2 - (27 * Math.pow(d4, 3)) / 32) * Math.sin(2 * d12) + ((21 * d4 * d4) / 16 - (55 * Math.pow(d4, 4)) / 32) * Math.sin(4 * d12) + ((151 * Math.pow(d4, 3)) / 96) * Math.sin(6 * d12);
        double d5 = d1 / Math.sqrt(1 - d2 * Math.sin(d14) * Math.sin(d14));
        double d6 = Math.tan(d14) * Math.tan(d14);
        double d7 = d3 * Math.cos(d14) * Math.cos(d14);
        double d8 = (d1 * (1 - d2)) / Math.pow(1 - d2 * Math.sin(d14) * Math.sin(d14), 1.5);
        double d9 = d15 / (d5 * d);
        double d17 = d14 - ((d5 * Math.tan(d14)) / d8) * (((d9 * d9) / 2 - (((5 + 3 * d6 + 10 * d7) - 4 * d7 * d7 - 9 * d3) * Math.pow(d9, 4)) / 24) + (((61 + 90 * d6 + 298 * d7 + 45 * d6 * d6) - 252 * d3 - 3 * d7 * d7) * Math.pow(d9, 6)) / 720);
        d17 = d17 * 180 / Math.PI;
        double d18 = ((d9 - ((1 + 2 * d6 + d7) * Math.pow(d9, 3)) / 6) + (((((5 - 2 * d7) + 28 * d6) - 3 * d7 * d7) + 8 * d3 + 24 * d6 * d6) * Math.pow(d9, 5)) / 120) / Math.cos(d14);
        d18 = d11 + d18 * 180 / Math.PI;
        Location location = new Location("roomfinder");
        location.setLatitude(d18);
        location.setLongitude(d17);
        return location;
    }

    public static final double[][] campusLocations = {
            {48.2648424, 11.6709511}, // Garching Forschungszentrum
            {48.249432, 11.633905}, // Garching Hochbrück
            {48.397990, 11.722727}, // Weihenstephan
            {48.149436, 11.567635}, // Stammgelände
            {48.110847, 11.4703001}, // Klinikum Großhadern
            {48.137539, 11.601119}, // Klinikum rechts der Isar
            {48.155916, 11.583095}, // Leopoldstraße
            {48.150244, 11.580665} // Geschwister Schollplatz/Adalbertstraße
    };

    public static final String[] campusShort = {
            "G", // Garching Forschungszentrum
            "H", // Garching Hochbrück
            "W", // Weihenstephan
            "C", // Stammgelände
            "K", // Klinikum Großhadern
            "I", // Klinikum rechts der Isar
            "L", // Leopoldstraße
            "S" // Geschwister Schollplatz/Adalbertstraße
    };

    public static final String[] defaultCampusStation = {
            "Garching-Forschungszentrum",
            "Garching-Hochbrück",
            "Weihenstephan",
            "Theresienstraße",
            "Klinikum Großhadern",
            "Max-Weber-Platz",
            "Giselastraße",
            "Universität"
    };

    public static final String[] defaultCampusCafeteria = { "422", null, "423", "421", "414", null, "411", null };
}
