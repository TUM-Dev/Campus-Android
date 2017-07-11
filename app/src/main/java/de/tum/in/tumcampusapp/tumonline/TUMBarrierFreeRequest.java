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
import de.tum.in.tumcampusapp.models.barrierfree.BarrierfreeContact;

import static de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequest.KEY_BUILDING_TITLE;
import static de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequest.KEY_CAMPUS_ID;
import static de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequest.KEY_CAMPUS_TITLE;
import static de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequest.KEY_ARCH_ID;
import static de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequest.KEY_ROOM_ID;
import static de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequest.KEY_ROOM_TITLE;


public class TUMBarrierFreeRequest {
    // Json keys
    public static final String KEY_PERSON_NAME = "name";
    public static final String KEY_PERSON_TELEPHONE = "telephone";
    public static final String KEY_PERSON_EMAIL= "email";
    public static final String KEY_PERSON_FACULTY = "faculty";
    public static final String KEY_PERSON_TUM_ID = "tumID";

    // Api urls
    //private static final String API_BASE_URL = "https://tumcabe.in.tum.de/Api/roomfinder/";
    // local testing
    // TODO: 7/5/2017 Change to real server when used
    private static final String API_BASE_URL = "http://10.0.2.2/api/barrierfree/";

    private static final String API_URL_RESPONSIBLEPERSON = API_BASE_URL + "contacts";
    private static final String API_URL_LIST_OF_TOILETS = API_BASE_URL + "listOfToilets";
    private static final String API_URL_LIST_OF_ELEVATORS = API_BASE_URL + "listOfElevators";

    private final NetUtils net;

    private AsyncTask<Void, Void, List<BarrierfreeContact>> contactBackgroundTask;

    private AsyncTask<Void, Void, List<Map<String, String>>> facilitiesBackgroundTask;

    public TUMBarrierFreeRequest(Context context) {
        net = new NetUtils(context);
    }


    public void fetchContactsInteractive(final Context context,
                                       final TUMBarrierFreeRequestFetchListener<BarrierfreeContact> listener) {

        // fetch information in a background task and show progress dialog in
        // meantime
        contactBackgroundTask = new AsyncTask<Void, Void, List<BarrierfreeContact>>() {
            boolean isOnline;

            @Override
            protected List<BarrierfreeContact> doInBackground(Void... params) {
                isOnline = NetUtils.isConnected(context);
                if (!isOnline) {
                    // not online, fetch does not make sense
                    return null;
                }
                // we are online, return fetch result

                return fetchResponsiblePersonList();
            }

            @Override
            protected void onPostExecute(List<BarrierfreeContact> result) {
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
                // If there could not be found any problems return usual on
                // Fetch method
                listener.onFetch(result);
            }
        };

        contactBackgroundTask.execute();
    }

    public void fetchListOfElevators(final Context context, final TUMRoomFinderRequestFetchListener listener){
        String url = API_URL_LIST_OF_ELEVATORS;
        fetchListOfFacilities(context, listener, url);
    }

    public void fetchListOfToilets(final Context context, final TUMRoomFinderRequestFetchListener listener){
        String url = API_URL_LIST_OF_TOILETS;
        fetchListOfFacilities(context, listener, url);
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

                Optional<JSONArray> jsonArray = net.downloadJsonArray(url, CacheManager.VALIDITY_DO_NOT_CACHE, true);

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

    public List<BarrierfreeContact> fetchResponsiblePersonList() {

        String url = API_URL_RESPONSIBLEPERSON;
        Optional<JSONArray> jsonArray = net.downloadJsonArray(url, CacheManager.VALIDITY_DO_NOT_CACHE, true);

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
                        BarrierfreeContact contact = new BarrierfreeContact();
                        contact.setName(name);
                        contact.setFaculty(faculty);
                        contact.setPhone(phone);
                        contact.setEmail(email);
                        contact.setTumonlineID(tumID);

                        contactsList.add(contact);
                    }

                }
            }
        } catch (JSONException e) {
            Utils.log(e);
        }

        return contactsList;
    }

    public void cancelContactsRequest(boolean mayInterruptIfRunning) {
        // Cancel background task just if one has been established
        if (contactBackgroundTask != null) {
            contactBackgroundTask.cancel(mayInterruptIfRunning);
        }
    }
}
