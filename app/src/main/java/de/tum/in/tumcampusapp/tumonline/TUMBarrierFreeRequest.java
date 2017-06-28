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

import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.CacheManager;

public class TUMBarrierFreeRequest {
    // Json keys
    public static final String KEY_PERSON_NAME = "name";
    public static final String KEY_PERSON_TELEPHONE = "telephone";
    public static final String KEY_PERSON_EMAIL= "email";
    public static final String KEY_PERSON_FACULTY = "faculty";
    public static final String KEY_PERSON_OFFICE = "office";
    public static final String KEY_PERSON_OFFICEHOUR = "officeHour";

    // Api urls
    //private static final String API_BASE_URL = "https://tumcabe.in.tum.de/Api/roomfinder/";
    // local testing
    private static final String API_BASE_URL = "http://10.0.2.2/Api/barrierfree/";

    private static final String API_URL_RESPONSIBLEPERSON = API_BASE_URL + "persons/";

    private final NetUtils net;


    public TUMBarrierFreeRequest(Context context) {
        net = new NetUtils(context);
    }

    public List<Map<String, String>> fetchResponsiblePersonList() {

        String url = API_URL_RESPONSIBLEPERSON;
        Optional<JSONArray> jsonArray = net.downloadJsonArray(url, CacheManager.VALIDITY_DO_NOT_CACHE, true);

        List<Map<String, String>> personList = new ArrayList<>();
        try {
            if (jsonArray.isPresent()) {
                JSONArray arr = jsonArray.get();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Map<String, String> roomMap = new HashMap<>();
                    roomMap.put(KEY_PERSON_NAME, obj.getString(KEY_PERSON_NAME));
                    roomMap.put(KEY_PERSON_TELEPHONE, obj.getString(KEY_PERSON_TELEPHONE));
                    roomMap.put(KEY_PERSON_EMAIL, obj.getString(KEY_PERSON_EMAIL));
                    roomMap.put(KEY_PERSON_FACULTY, obj.getString(KEY_PERSON_FACULTY));
                    roomMap.put(KEY_PERSON_OFFICE, obj.getString(KEY_PERSON_OFFICE));
                    roomMap.put(KEY_PERSON_OFFICEHOUR, obj.getString(KEY_PERSON_OFFICEHOUR));

                    // adding HashList to ArrayList
                    personList.add(roomMap);
                }
            }
        } catch (JSONException e) {
            Utils.log(e);
        }

        return personList;
    }
}
