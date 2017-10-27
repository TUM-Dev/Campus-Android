package de.tum.in.tumcampusapp.managers;

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
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.common.base.Optional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.cafeteria.Cafeteria;
import de.tum.in.tumcampusapp.models.efa.StationResult;
import de.tum.in.tumcampusapp.models.tumcabe.BuildingsToGps;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderCoordinate;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderRoom;
import de.tum.in.tumcampusapp.models.tumo.Geo;

/**
 * Location manager, manages intelligent location services, provides methods to easily access
 * the users current location, campus, next public transfer station and best cafeteria
 */
public class LocationManager extends AbstractManager {
    private static final double[][] CAMPUS_LOCATIONS = {
            {48.2648424, 11.6709511}, // Garching Forschungszentrum
            {48.249432, 11.633905}, // Garching Hochbrück
            {48.397990, 11.722727}, // Weihenstephan
            {48.149436, 11.567635}, // Stammgelände
            {48.110847, 11.4703001}, // Klinikum Großhadern
            {48.137539, 11.601119}, // Klinikum rechts der Isar
            {48.155916, 11.583095}, // Leopoldstraße
            {48.150244, 11.580665} // Geschwister Schollplatz/Adalbertstraße
    };
    private static final String[] CAMPUS_SHORT = {
            "G", // Garching Forschungszentrum
            "H", // Garching Hochbrück
            "W", // Weihenstephan
            "C", // Stammgelände
            "K", // Klinikum Großhadern
            "I", // Klinikum rechts der Isar
            "L", // Leopoldstraße
            "S" // Geschwister Schollplatz/Adalbertstraße
    };
    private static final StationResult[] DEFAULT_CAMPUS_STATION = {new StationResult("Garching-Forschungszentrum", "1000460", Integer.MAX_VALUE),
                                                                   new StationResult("Garching-Hochbrück", "1000480", Integer.MAX_VALUE),
                                                                   new StationResult("Weihenstephan", "1002911", Integer.MAX_VALUE),
                                                                   new StationResult("Theresienstraße", "1000120", Integer.MAX_VALUE),
                                                                   new StationResult("Klinikum Großhadern", "1001540", Integer.MAX_VALUE),
                                                                   new StationResult("Max-Weber-Platz", "1000580", Integer.MAX_VALUE),
                                                                   new StationResult("Giselastraße", "1000080", Integer.MAX_VALUE),
                                                                   new StationResult("Universität", "1000070", Integer.MAX_VALUE)
    };

    private static final String[] DEFAULT_CAMPUS_CAFETERIA = {"422", null, "423", "421", "414", null, "411", null};
    private final Context mContext;

    public LocationManager(Context c) {
        super(c);
        mContext = c;
        createBuildingsToGpsTable();
    }

    /**
     * Tests if Google Play services is available and than gets last known position
     *
     * @return Returns the more or less current position or null on failure
     */
    private Location getCurrentLocation() {
        if (servicesConnected()) {
            Location loc = getLastLocation();
            if (loc != null) {
                return loc;
            }
        }

        // If location services are not available use default location if set
        final String defaultCampus = Utils.getSetting(mContext, Const.DEFAULT_CAMPUS, "G");
        if (!"X".equals(defaultCampus)) {
            for (int i = 0; i < CAMPUS_SHORT.length; i++) {
                if (CAMPUS_SHORT[i].equals(defaultCampus)) {
                    Location location = new Location("defaultLocation");
                    location.setLatitude(CAMPUS_LOCATIONS[i][0]);
                    location.setLongitude(CAMPUS_LOCATIONS[i][1]);
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
    private int getCurrentCampus() {
        Location loc = getCurrentLocation();
        if (loc == null) {
            return -1;
        }
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
        float[] results = new float[1];
        float bestDistance = Float.MAX_VALUE;
        int bestCampus = -1;
        for (int i = 0; i < CAMPUS_LOCATIONS.length; i++) {
            Location.distanceBetween(CAMPUS_LOCATIONS[i][0], CAMPUS_LOCATIONS[i][1], lat, lng, results);
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
        float[] results = new float[1];
        CafeteriaManager manager = new CafeteriaManager(mContext);
        List<Cafeteria> list;
        try (Cursor cur = manager.getAllFromDb()) {
            list = new ArrayList<>(cur.getCount());

            if (cur.moveToFirst()) {
                do {
                    Cafeteria cafe = new Cafeteria(cur.getInt(0), cur.getString(1),
                                                   cur.getString(2), cur.getDouble(3), cur.getDouble(4));
                    Location.distanceBetween(cur.getDouble(3), cur.getDouble(4), lat, lng, results);
                    cafe.distance = results[0];
                    list.add(cafe);
                } while (cur.moveToNext());
            }
        }
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
    private Location getLastLocation() {
        //Check Location permission for Android 6.0
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

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

                if (time > minTime && accuracy < bestAccuracy) {
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
    public String getStation() { // TODO: return a StationResult, so we can query the MVV for IDs instead of station names
        int campus = getCurrentCampus();
        if (campus == -1) {
            return null;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final String defaultVal = DEFAULT_CAMPUS_STATION[campus].station;
        return prefs.getString("card_stations_default_" + CAMPUS_SHORT[campus], defaultVal);
    }

    /**
     * Checks that Google Play services are available
     */
    private boolean servicesConnected() {
        int resultCode = GoogleApiAvailability.getInstance()
                                              .isGooglePlayServicesAvailable(mContext);
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
    private int getCurrentOrNextCampus() {
        int campus = getCurrentCampus();
        if (campus != -1) {
            return campus;
        }
        return getNextCampus();
    }

    /**
     * Provides some intelligence to pick one cafeteria to show
     */
    public int getCafeteria() {
        int campus = getCurrentOrNextCampus();
        if (campus != -1) { // If the user is in university or a lecture has been recognized
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String defaultVal = DEFAULT_CAMPUS_CAFETERIA[campus];
            String cafeteria = prefs.getString("card_cafeteria_default_" + CAMPUS_SHORT[campus], defaultVal);
            if (cafeteria != null) {
                return Integer.parseInt(cafeteria);
            }
        }

        // Get nearest cafeteria
        List<Cafeteria> list = getCafeterias();
        if (list == null || list.isEmpty()) {
            return -1;
        }
        return list.get(0).id;
    }

    /**
     * Queries your calender and gets the campus at which your next lecture takes place
     */
    private int getNextCampus() {
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
        if (geo == null) {
            location.setLatitude(48.2648424);
            location.setLongitude(11.6709511);
        } else {
            location.setLatitude(Double.parseDouble(geo.getLatitude()));
            location.setLongitude(Double.parseDouble(geo.getLongitude()));
        }
        return location;
    }

    /**
     * Converts UTM based coordinates to latitude and longitude based format
     */
    private static Geo convertUTMtoLL(double north, double east, double zone) {
        double d = 0.99960000000000004;
        double d1 = 6378137;
        double d2 = 0.0066943799999999998;
        double d4 = (1 - Math.sqrt(1 - d2)) / (1 + Math.sqrt(1 - d2));
        double d15 = east - 500000;
        double d11 = (zone - 1) * 6 - 180 + 3;
        double d3 = d2 / (1 - d2);
        double d10 = north / d;
        double d12 = d10 / (d1 * (1 - d2 / 4 - (3 * d2 * d2) / 64 - (5 * Math.pow(d2, 3)) / 256));
        double d14 = d12 + ((3 * d4) / 2 - (27 * Math.pow(d4, 3)) / 32) * Math.sin(2 * d12) + ((21 * d4 * d4) / 16 - (55 * Math.pow(d4, 4)) / 32) * Math.sin(4 * d12) + ((151 * Math.pow(d4, 3)) / 96) * Math.sin(6 * d12);
        double d5 = d1 / Math.sqrt(1 - d2 * Math.sin(d14) * Math.sin(d14));
        double d6 = Math.tan(d14) * Math.tan(d14);
        double d7 = d3 * Math.cos(d14) * Math.cos(d14);
        double d8 = (d1 * (1 - d2)) / Math.pow(1 - d2 * Math.sin(d14) * Math.sin(d14), 1.5);
        double d9 = d15 / (d5 * d);
        double d17 = d14 - ((d5 * Math.tan(d14)) / d8) * ((d9 * d9) / 2 - ((5 + 3 * d6 + 10 * d7 - 4 * d7 * d7 - 9 * d3) * Math.pow(d9, 4)) / 24 + ((61 + 90 * d6 + 298 * d7 + 45 * d6 * d6 - 252 * d3 - 3 * d7 * d7) * Math.pow(d9, 6)) / 720);
        d17 *= 180 / Math.PI;
        double d18 = (d9 - ((1 + 2 * d6 + d7) * Math.pow(d9, 3)) / 6 + ((5 - 2 * d7 + 28 * d6 - 3 * d7 * d7 + 8 * d3 + 24 * d6 * d6) * Math.pow(d9, 5)) / 120) / Math.cos(d14);
        d18 = d11 + d18 * 180 / Math.PI;
        return new Geo(d17, d18);
    }

    public static Optional<Geo> convertRoomFinderCoordinateToGeo(RoomFinderCoordinate roomFinderCoordinate) {
        Geo result;
        try {
            double zone = Double.parseDouble(roomFinderCoordinate.getUtm_zone());
            double easting = Double.parseDouble(roomFinderCoordinate.getUtm_easting());
            double northing = Double.parseDouble(roomFinderCoordinate.getUtm_northing());
            result = convertUTMtoLL(northing, easting, zone);

            return Optional.of(result);
        } catch (NullPointerException | NumberFormatException e) {
            Utils.log(e);
        }

        return Optional.absent();
    }

    /**
     * Get the geo information for a room
     *
     * @param archId arch_id of the room
     * @return Location or null on failure
     */
    private Optional<Geo> fetchRoomGeo(String archId) {
        try {
            RoomFinderCoordinate coordinate = TUMCabeClient.getInstance(mContext)
                                                           .fetchCoordinates(archId);
            return convertRoomFinderCoordinateToGeo(coordinate);
        } catch (IOException e) {
            Utils.log(e);
        }

        return Optional.absent();
    }

    /**
     * Translates room title to Geo
     * HINT: Don't call from UI thread
     *
     * @param roomTitle Room title
     * @return Location or null on failure
     */
    Optional<Geo> roomLocationStringToGeo(String roomTitle) {
        String loc = roomTitle;
        if (loc.contains("(")) {
            loc = loc.substring(0, loc.indexOf('('))
                     .trim();
        }

        try {
            Optional<List<RoomFinderRoom>> rooms = Optional.of(TUMCabeClient.getInstance(mContext)
                                                                            .fetchRooms(loc));

            if (rooms.isPresent() && !rooms.get()
                                           .isEmpty()) {
                String room = rooms.get()
                                   .get(0)
                                   .getArch_id();
                return fetchRoomGeo(room);
            }

        } catch (IOException | NullPointerException e) {
            Utils.log(e);
        }

        return Optional.absent();
    }

    private void createBuildingsToGpsTable() {
        db.execSQL("CREATE TABLE IF NOT EXISTS buildings2gps ("
                   + "id VARCHAR PRIMARY KEY, latitude VARCHAR, longitude VARCHAR)");
    }

    private void insertInBuildingsToGps(BuildingsToGps map) {
        String[] params = {
                map.getId(),
                map.getLatitude(),
                map.getLongitude()
        };
        db.execSQL("INSERT INTO buildings2gps (id, latitude, longitude) VALUES (?, ?, ?)", params);
    }

    /**
     * This method tries to get the list of BuildingsToGps by querying database or requesting the server.
     * If both two ways fail, it returns Optional.absent().
     *
     * @return The list of BuildingsToGps
     */
    private List<BuildingsToGps> getOrFetchBuildingsToGps() {
        List<BuildingsToGps> result;
        try (Cursor cursor = db.rawQuery("SELECT * FROM buildings2gps", null)) {
            result = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                if (cursor.isAfterLast()) {
                    continue;
                }

                String id = cursor.getString(0);
                String latitude = cursor.getString(1);
                String longitude = cursor.getString(2);

                BuildingsToGps mapping = new BuildingsToGps(id, latitude, longitude);

                result.add(mapping);
            }
        }

        if (result.isEmpty()) {
            // we have to fetch buildings to gps mapping first.

            try {
                result = TUMCabeClient.getInstance(mContext)
                                      .getBuilding2Gps();
                if (result == null) {
                    return new ArrayList<>();
                }

                for (BuildingsToGps map : result) {
                    insertInBuildingsToGps(map);
                }

            } catch (IOException e) {
                Utils.log(e);
                return new ArrayList<>();
            }
        }

        return result;
    }

    /**
     * Get Building ID accroding to the current location
     * Do not call on UI thread.
     *
     * @return the id of current building
     */
    public Optional<String> getBuildingIDFromCurrentLocation() {
        return getBuildingIDFromLocation(getCurrentLocation());
    }

    /**
     * Get Building ID accroding to the given location.
     * Do not call on UI thread.
     *
     * @param location the give location
     * @return the id of current building
     */
    private Optional<String> getBuildingIDFromLocation(Location location) {
        List<BuildingsToGps> buildingsToGpsList = getOrFetchBuildingsToGps();

        if (buildingsToGpsList.isEmpty()) {
            return Optional.absent();
        }

        final double lat = location.getLatitude();
        final double lng = location.getLongitude();
        float[] results = new float[1];
        float bestDistance = Float.MAX_VALUE;
        String bestBuilding = "";

        for (BuildingsToGps building : buildingsToGpsList) {
            double buildingLat = Double.parseDouble(building.getLatitude());
            double buildingLng = Double.parseDouble(building.getLongitude());

            Location.distanceBetween(buildingLat, buildingLng, lat, lng, results);
            float distance = results[0];
            if (distance < bestDistance) {
                bestDistance = distance;
                bestBuilding = building.getId();
            }
        }

        if (bestDistance < 1000) {
            return Optional.of(bestBuilding);
        } else {
            return Optional.absent();
        }
    }
}
