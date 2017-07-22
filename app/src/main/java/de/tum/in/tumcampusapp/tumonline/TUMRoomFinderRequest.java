package de.tum.in.tumcampusapp.tumonline;

import android.content.Context;
import android.os.AsyncTask;

import com.google.common.base.Optional;
import com.google.common.net.UrlEscapers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.auxiliary.calendar.IntegratedCalendarEvent;
import de.tum.in.tumcampusapp.managers.CacheManager;
import de.tum.in.tumcampusapp.models.tumo.Geo;

/**
 * Base class for communication with TUMRoomFinder
 */
public class TUMRoomFinderRequest {

    // Json keys
    public static final String KEY_ARCH_ID = "arch_id";
    public static final String KEY_MAP_ID = "map_id";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_ROOM_ID = "room_id";
    public static final String KEY_CAMPUS_ID = "campus";
    public static final String KEY_CAMPUS_TITLE = "name";
    public static final String KEY_BUILDING_TITLE = "address";
    public static final String KEY_ROOM_TITLE = "info";
    public static final String KEY_UTM_ZONE = "utm_zone";
    public static final String KEY_UTM_EASTING = "utm_easting";
    public static final String KEY_UTM_NORTHING = "utm_northing";

    // Api urls
    private static final String API_BASE_URL = "https://tumcabe.in.tum.de/Api/roomfinder/";

    private static final String API_URL_SEARCH = API_BASE_URL + "room/search/";
    private static final String API_URL_DEFAULT_MAP = API_BASE_URL + "room/defaultMap/";
    private static final String API_URL_MAP = API_BASE_URL + "room/map/";
    private static final String API_URL_COORDINATES = API_BASE_URL + "room/coordinates/";
    private static final String API_URL_AVAILABLE_MAPS = API_BASE_URL + "room/availableMaps/";
    private static final String API_URL_SCHEDULE = API_BASE_URL + "room/scheduleById/";
    private final NetUtils net;
    /**
     * asynchronous task for interactive fetch
     */
    private AsyncTask<String, Void, List<Map<String, String>>> backgroundTask;

    public TUMRoomFinderRequest(Context context) {
        net = new NetUtils(context);
    }

    /**
     * returns the url to get the default map
     *
     * @param archId architecture id
     * @return url of default map
     */
    public static String fetchDefaultMap(String archId) {
        return API_URL_DEFAULT_MAP + encodeUrl(archId);
    }

    /**
     * returns the url for any map
     *
     * @param archId architecture id
     * @param mapId  map id
     * @return url of map
     */
    public static String fetchMap(String archId, String mapId) {
        return API_URL_MAP + encodeUrl(archId) + '/' + encodeUrl(mapId);
    }

    /**
     * encodes an url
     *
     * @param pUrl input url
     * @return encoded url
     */
    private static String encodeUrl(String pUrl) {
        String url = pUrl.replace("/", ""); //remove slashes in queries as this breaks the url
        return UrlEscapers.urlPathSegmentEscaper().escape(url);
    }

    /**
     * fetches all rooms that match the search string
     *
     * @param searchString string that was entered by the user
     * @return list of HashMaps representing rooms, Map: attributes -> values
     */
    public List<Map<String, String>> fetchRooms(String searchString) {

        String url = API_URL_SEARCH + encodeUrl(searchString);
        Optional<JSONArray> jsonArray = net.downloadJsonArray(url, CacheManager.VALIDITY_DO_NOT_CACHE, true);

        List<Map<String, String>> roomsList = new ArrayList<>();
        try {
            if (jsonArray.isPresent()) {
                JSONArray arr = jsonArray.get();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Map<String, String> roomMap = new HashMap<>();
                    roomMap.put(KEY_CAMPUS_ID, obj.getString(KEY_CAMPUS_ID));
                    roomMap.put(KEY_CAMPUS_TITLE, obj.getString(KEY_CAMPUS_TITLE));
                    roomMap.put(KEY_BUILDING_TITLE, obj.getString(KEY_BUILDING_TITLE));
                    roomMap.put(KEY_ROOM_TITLE, obj.getString(KEY_ROOM_TITLE));
                    roomMap.put(KEY_ARCH_ID, obj.getString(KEY_ARCH_ID));
                    roomMap.put(KEY_ROOM_ID, obj.getString(KEY_ROOM_ID));

                    // adding HashList to ArrayList
                    roomsList.add(roomMap);
                }
            }
        } catch (JSONException e) {
            Utils.log(e);
        }

        return roomsList;
    }

    /**
     * fetches the room schedule for a given room
     *
     * @param roomId roomId
     * @return List of Events
     */
    public List<IntegratedCalendarEvent> fetchRoomSchedule(String roomId, String startDate, String endDate, List<IntegratedCalendarEvent> scheduleList) {

        String url = API_URL_SCHEDULE + encodeUrl(roomId) + '/' + encodeUrl(startDate) + '/' + encodeUrl(endDate);

        Optional<JSONArray> jsonArray = net.downloadJsonArray(url, CacheManager.VALIDITY_DO_NOT_CACHE, true);

        if (!jsonArray.isPresent()) {
            return scheduleList;
        }

        JSONArray arr = jsonArray.get();

        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                String start = obj.getString("start");
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(Utils.getISODateTime(start));

                String end = obj.getString("end");
                Calendar endCal = Calendar.getInstance();
                endCal.setTime(Utils.getISODateTime(end));
                IntegratedCalendarEvent event = new IntegratedCalendarEvent(
                        obj.getLong("event_id"),
                        obj.getString(Const.JSON_TITLE),
                        startCal,
                        endCal,
                        "",
                        IntegratedCalendarEvent.getDisplayColorFromColor(0xff28921f)
                );
                scheduleList.add(event);
            }
        } catch (JSONException e) {
            Utils.log(e);
        }

        return scheduleList;
    }
}
