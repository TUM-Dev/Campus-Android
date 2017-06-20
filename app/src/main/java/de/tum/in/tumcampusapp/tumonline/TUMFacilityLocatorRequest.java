package de.tum.in.tumcampusapp.tumonline;

import android.content.Context;
import android.os.AsyncTask;

import com.google.common.base.Optional;
import com.google.common.net.UrlEscapers;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;

/**
 * Base class for communication with TUMRoomFinder
 */
public class TUMFacilityLocatorRequest {

    //    XML RPC urls
    private static final String RPC_API_BASE_URL = "http://roomfinder.ze.tum.de:8192/xmlrpc";
    private static final String RPC_API_GEO_MAPS =  "getGeoMaps";
    private static final String RPC_API_FLAG_GEO =  "flagGeo";
    private static final String API_URL_SEARCH_FACILITIES_BY_QUERY = "to_be_added_search_endpoint";
    private static final String API_URL_SEARCH_FACILITIES_BY_CATEGORY = "to_be_added_search_endpoint";

//    MOCK Data
    public static final String MOCK_FACILITIES="[{facility_id:1,name:'Garching Library',longitude:11.666862,latitude:48.262547,category_id:1}," +
            "{facility_id:2,name:'Main campus Library',longitude:11.568010,latitude:48.148848,category_id:1}," +
            "{facility_id:3,name:'Stucafe Informatics',latitude:48.262403, longitude:11.668032,category_id:2}," +
            "{facility_id:4,name:'Stucafe Mechanical',latitude:48.265767, longitude:11.667571,category_id:2}]";


    private final NetUtils net;
    /**
     * asynchronous task for interactive fetch
     */
    private AsyncTask<String, Void, Optional<JSONArray>> backgroundTask;

    public TUMFacilityLocatorRequest(Context context) {
        net = new NetUtils(context);
    }

    @SuppressWarnings("unchecked")
    public static String getMapWithLocation(double longitude, double latitude){

//        TODO:Replace with the new api call
        try {
            XMLRPCClient client = new XMLRPCClient(new URL(RPC_API_BASE_URL));
            Object[] maps = (Object[])client.call(RPC_API_GEO_MAPS,longitude, latitude);
            if(maps.length>0){
                Integer mapId=(Integer)((Object[])maps[0])[1];
                Object[] map=(Object[])client.call(RPC_API_FLAG_GEO,longitude, latitude,mapId);
                String imageString=(String)(map[0]);
                return imageString;
            }
            else{
                return null;
            }

        } catch(XMLRPCServerException ex) {
            ex.printStackTrace();
            return null;
            // The server throw an error.
        } catch(XMLRPCException ex) {
            ex.printStackTrace();
            return null;
            // An error occured in the client.
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
            // Any other exception
        }
    }

    public Optional<JSONArray> fetchFacilityCategories(){
        JSONArray mockCategories=null;
        try {
            mockCategories=new JSONArray("[{id:1,name:Library},{id:2,name:Cafeteria}]");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Optional.of(mockCategories);
    }


    public Optional<JSONArray> fetchFacilitiesByQuery(String searchString) {
//        TODO:replace with actual search call
        Optional<JSONArray> facilitiesList=getMockFacilitiesByQuery(searchString);
        return facilitiesList;
    }

    public Optional<JSONArray> fetchFacilitiesByCategory(String categoryId) {
//        TODO:replace with actual search call
        Optional<JSONArray> facilitiesList=getMockFacilitiesByCategory(categoryId);
        return facilitiesList;
    }

    private Optional<JSONArray> getMockFacilitiesByCategory(String id){
        JSONArray result=new JSONArray();
        try {
            JSONArray mockFacilitiesJson=new JSONArray(MOCK_FACILITIES);
            for (int i = 0; i < mockFacilitiesJson.length(); i++) {
                JSONObject item = mockFacilitiesJson.getJSONObject(i);
                if(item.getString("category_id").equals(id)){
                    result.put(item);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Optional.of(result);
    }



    private Optional<JSONArray> getMockFacilitiesByQuery(String query){
        JSONArray result=new JSONArray();
        try {
            JSONArray mockFacilitiesJson=new JSONArray(MOCK_FACILITIES);
            for (int i = 0; i < mockFacilitiesJson.length(); i++) {
                JSONObject item = mockFacilitiesJson.getJSONObject(i);
                if(item.getString("name").contains(query)){
                    result.put(item);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Optional.of(result);
    }

    public void fetchSearchInteractiveFacilities(final Context context,
                                                 final TUMFacilityLocatorRequestFetchListener listener,
                                                 String searchString, final boolean searchByCategory) {

        // fetch information in a background task and show progress dialog in
        // meantime
        backgroundTask = new AsyncTask<String, Void, Optional<JSONArray>>() {

            /**
             * property to determine if there is an internet connection
             */
            boolean isOnline;

            @Override
            protected Optional<JSONArray> doInBackground(String... searchString) {
                // set parameter on the TUMRoomFinder request an fetch the
                // results
                isOnline = NetUtils.isConnected(context);
                if (!isOnline) {
                    // not online, fetch does not make sense
                    return null;
                }
                // we are online, return fetch result
                if(searchByCategory)
                    return fetchFacilitiesByCategory(searchString[0]);
                else
                    return fetchFacilitiesByQuery(searchString[0]);
            }

            @Override
            protected void onPostExecute(Optional<JSONArray> result) {
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

        backgroundTask.execute(searchString);
    }



    public void cancelRequest(boolean mayInterruptIfRunning) {
        // Cancel background task just if one has been established
        if (backgroundTask != null) {
            backgroundTask.cancel(mayInterruptIfRunning);
        }
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

}
