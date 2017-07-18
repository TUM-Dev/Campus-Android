package de.tum.in.tumcampusapp.tumonline;

import android.content.Context;
import android.os.AsyncTask;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.CacheManager;
import de.tum.in.tumcampusapp.managers.LocationManager;
import de.tum.in.tumcampusapp.models.barrierfree.BarrierfreeContact;
import de.tum.in.tumcampusapp.models.barrierfree.BarrierfreeMoreInfo;

import static de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequest.*;

/**
 * The class used to handle Barrierfree data exchange with backend
 */
public class TUMBarrierFreeRequest {
    // Json keys
    // contact
    public static final String KEY_PERSON_NAME = "name";
    public static final String KEY_PERSON_TELEPHONE = "telephone";
    public static final String KEY_PERSON_EMAIL= "email";
    public static final String KEY_PERSON_FACULTY = "faculty";
    public static final String KEY_PERSON_TUM_ID = "tumID";

    // More info
    public static final String KEY_MORE_INFO_TITLE = "title";
    public static final String KEY_MORE_INFO_CATEGORY = "category";
    public static final String KEY_MORE_INFO_URL = "url";

    //Building to gps
    public static final String KEY_BUILDING_TO_GPS_Building_ID = "id";
    public static final String KEY_BUILDING_TO_GPS_LATITUDE= "latitude";
    public static final String KEY_BUILDING_TO_GPS_LONGITUDE = "longitude";

    // facilities
    public static final String KEY_FACILITY_PURPOSE = "purpose";

    // Api urls
    //private static final String API_BASE_URL = "https://tumcabe.in.tum.de/Api/roomfinder/";
    // local testing
    // TODO: 7/5/2017 Change to real server when used
    private static final String API_BASE_URL = "http://10.0.2.2/api/barrierfree/";

    public static final String API_URL_RESPONSIBLEPERSON = API_BASE_URL + "contacts";
    public static final String API_URL_MORE_INFO = API_BASE_URL + "moreInformation";
    public static final String API_URL_LIST_OF_TOILETS = API_BASE_URL + "listOfToilets";
    public static final String API_URL_LIST_OF_ELEVATORS = API_BASE_URL + "listOfElevators";
    public static final String API_URL_BUILDINGS_TO_GPS = API_BASE_URL + "getBuilding2Gps";
    public static final String API_URL_Nerby_FACILITIES = API_BASE_URL + "nerby";

    private final NetUtils net;

    private AsyncTask<Void, Void, List<Map<String, String>>> facilitiesBackgroundTask;
    private AsyncTask<Void, Void, List<Map<String, String>>> buildingsToGpsBackgroundTask;

    public TUMBarrierFreeRequest(Context context) {
        net = new NetUtils(context);
    }

    public void fetchListOfElevators(final Context context, final TUMRoomFinderRequestFetchListener listener){
        String url = API_URL_LIST_OF_ELEVATORS;
        fetchListOfFacilities(context, listener, url);
    }

    public void fetchListOfToilets(final Context context, final TUMRoomFinderRequestFetchListener listener){
        String url = API_URL_LIST_OF_TOILETS;
        fetchListOfFacilities(context, listener, url);
    }

    public void fetchListOfNerbyFacilities(final Context context, final TUMRoomFinderRequestFetchListener listener){
        fetchBuildings2GpsAndFacilities(context, listener);
    }

    private void fetchListOfFacilities(final Context context,
                                       final TUMRoomFinderRequestFetchListener listener, final String url){
        facilitiesBackgroundTask = new AsyncTask<Void, Void, List<Map<String, String>>>() {

            boolean isOnline;

            @Override
            protected List<Map<String, String>> doInBackground(
                    Void... params) {
                isOnline = NetUtils.isConnected(context);
                if (!isOnline) {
                    return null;
                }

                Optional<JSONArray> jsonArray = net.downloadJsonArray(url, CacheManager.VALIDITY_ONE_MONTH, false);

                List<Map<String, String>> facilitiesList = new ArrayList<>();
                try {
                    if (jsonArray.isPresent()) {
                        JSONArray arr = jsonArray.get();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            Map<String, String> roomMap = new HashMap<>();
                            roomMap.put(KEY_CAMPUS_ID, obj.getString(KEY_CAMPUS_ID));
                            roomMap.put(KEY_CAMPUS_TITLE, obj.getString(KEY_CAMPUS_TITLE));
                            roomMap.put(KEY_BUILDING_TITLE, obj.getString(KEY_BUILDING_TITLE));
                            roomMap.put(KEY_FACILITY_PURPOSE, obj.getString(KEY_FACILITY_PURPOSE));
                            roomMap.put(KEY_ROOM_TITLE, obj.getString(KEY_ROOM_TITLE));
                            roomMap.put(KEY_ARCH_ID, obj.getString(KEY_ARCH_ID));
                            roomMap.put(KEY_ROOM_ID, obj.getString(KEY_ROOM_ID));

                            facilitiesList.add(roomMap);
                        }
                    }
                } catch (JSONException e) {
                    Utils.log(e);
                }

                return facilitiesList;
            }

            @Override
            protected void onPostExecute(List<Map<String, String>> result) {
                // handle result
                if (!isOnline) {
                    listener.onNoInternetError();
                    return;
                }
                if (result == null) {
                    listener.onFetchError(context
                            .getString(R.string.empty_result));
                    return;
                }
                listener.onFetch(result);
            }

        };

        facilitiesBackgroundTask.execute();
    }

    private void fetchBuildings2GpsAndFacilities(final Context context, final TUMRoomFinderRequestFetchListener listener){
        final String url = API_URL_BUILDINGS_TO_GPS;

        facilitiesBackgroundTask = new AsyncTask<Void, Void, List<Map<String, String>>>() {

            boolean isOnline;

            @Override
            protected List<Map<String, String>> doInBackground(
                    Void... params) {
                isOnline = NetUtils.isConnected(context);
                if (!isOnline) {
                    return null;
                }

                // TODO: 7/16/2017 Improve caching method. This is no good.
                Optional<JSONArray> jsonArray = net.downloadJsonArray(url, CacheManager.VALIDITY_ONE_MONTH, false);

                List<Map<String, String>> buildingsToGps = new ArrayList<>();
                try {
                    if (jsonArray.isPresent()) {
                        JSONArray arr = jsonArray.get();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            Map<String, String>  building2Gps = new HashMap<>();
                            String id = obj.getString(KEY_BUILDING_TO_GPS_Building_ID);
                            String longitude = obj.getString(KEY_BUILDING_TO_GPS_LONGITUDE);
                            String latitude = obj.getString(KEY_BUILDING_TO_GPS_LATITUDE);

                            building2Gps.put(KEY_BUILDING_TO_GPS_Building_ID, id);
                            building2Gps.put(KEY_BUILDING_TO_GPS_LONGITUDE, longitude);
                            building2Gps.put(KEY_BUILDING_TO_GPS_LATITUDE, latitude);

                            buildingsToGps.add(building2Gps);
                        }
                    }
                } catch (JSONException e) {
                    Utils.log(e);
                }

                return buildingsToGps;
            }

            @Override
            protected void onPostExecute(List<Map<String, String>> result) {
                if (!isOnline) {
                    listener.onNoInternetError();
                    return;
                }
                if (result == null) {
                    listener.onFetchError(context.getString(R.string.empty_result));
                    return;
                }

                LocationManager locationManager = new LocationManager(context);
                String buildingID = locationManager.getBuildingIDFromCurrentLocation(result);

                if (buildingID == null || buildingID.equals("") || buildingID.equals("null")){
                    listener.onFetchError(context.getString(R.string.empty_result));
                    return;
                }

                String url = API_URL_Nerby_FACILITIES + "/" + buildingID;
                fetchListOfFacilities(context, listener, url);
            }

        };

        facilitiesBackgroundTask.execute();
    }

    public List<BarrierfreeContact> fetchResponsiblePersonList() {

        String url = API_URL_RESPONSIBLEPERSON;
        Optional<JSONArray> jsonArray = net.downloadJsonArray(url, CacheManager.VALIDITY_ONE_MONTH, false);

        List<BarrierfreeContact> contactsList = new ArrayList<>();
        try {
            if (jsonArray.isPresent()) {
                JSONArray arr = jsonArray.get();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);

                    String name = obj.getString(KEY_PERSON_NAME);
                    String phone = obj.getString(KEY_PERSON_TELEPHONE);
                    String email = obj.getString(KEY_PERSON_EMAIL);
                    String faculty = obj.getString(KEY_PERSON_FACULTY);
                    String tumID = obj.getString(KEY_PERSON_TUM_ID);

                    if (!name.equals("null")){
                        BarrierfreeContact contact = new BarrierfreeContact(name, phone, email, faculty, tumID);

                        contactsList.add(contact);
                    }

                }
            }
        } catch (JSONException e) {
            Utils.log(e);
        }

        return contactsList;
    }

    public List<BarrierfreeMoreInfo> fetchMoreInfoList(){
        String url = API_URL_MORE_INFO;
        Optional<JSONArray> jsonArray = net.downloadJsonArray(url, CacheManager.VALIDITY_ONE_MONTH, false);

        List<BarrierfreeMoreInfo> infoList = new ArrayList<>();
        try {
            if (jsonArray.isPresent()) {
                JSONArray arr = jsonArray.get();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);

                    String title = obj.getString(KEY_MORE_INFO_TITLE);
                    String category = obj.getString(KEY_MORE_INFO_CATEGORY);
                    String infoUrl = obj.getString(KEY_MORE_INFO_URL);

                    if (!title.equals("null")){
                        BarrierfreeMoreInfo info = new BarrierfreeMoreInfo(title, category, infoUrl);
                        infoList.add(info);
                    }
                }
            }
        } catch (JSONException e) {
            Utils.log(e);
        }

        return infoList;
    }

    public void cancelRequest(boolean mayInterruptIfRunning) {
        // Cancel background task just if one has been established
        if (facilitiesBackgroundTask != null) {
            facilitiesBackgroundTask.cancel(mayInterruptIfRunning);
        }
    }
}
