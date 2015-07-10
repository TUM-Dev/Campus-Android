package de.tum.in.tumcampus.auxiliary;

import android.content.Context;
import android.content.pm.PackageManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MVGRequest {
    private static String MVG_SERVICE_URL = "https://www.mvg.de/fahrinfo/api/";

    private static String MVG_ROUTING = "routing/";

    private static String MVG_STATION = "location/query";

    private static String MVG_AUTH_KEY = "5af1beca494712ed38d313714d4caff6";

    private static int HTTP_TIMEOUT = 25000;

    private HttpClient mClient;

    private Context mContext;

    public MVGRequest(Context c) {
        mContext = c;

        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();

        //Don't allow to continue requests
        HttpProtocolParams.setUseExpectContinue(params, false);

        //Set our max wait time for each request
        HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
        HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);

        //Actually initiate our client with parameters we setup
        mClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
    }

    private JSONObject fetchURL(String url) throws IOException, JSONException {
        boolean isOnline = NetUtils.isConnected(mContext);
        if (!isOnline || url == null) {
            return null;
        }

        Utils.logv("Download URL: " + url);
        HttpGet request = new HttpGet(url);
        request.addHeader("Accept", "application/json, text/javascript, *'.'/*; q=0.01");
        request.addHeader("Accept-Language", "de,en-US;q=0.7,en;q=0.3");
        request.addHeader("X-MVG-Authorization-Key", MVG_AUTH_KEY);

        //Execute the request
        HttpResponse response = mClient.execute(request);
        return new JSONObject(EntityUtils.toString(response.getEntity()));
    }

    public JSONObject fetchStationId(String station) throws IOException, JSONException {
        return fetchURL(MVG_SERVICE_URL + MVG_STATION + "?q=" + station);
    }

    public JSONObject fetchRoute(int fromStation, String toLat, String toLong) throws IOException, JSONException {
        return fetchURL(MVG_SERVICE_URL + MVG_ROUTING + "?fromStation=" + fromStation + "&toLatitude=" + toLat + "&toLongitude=" + toLong);
    }

    public JSONObject fetchRoute(String fromLat, String fromLong, int toStation) throws IOException, JSONException {
        return fetchURL(MVG_SERVICE_URL + MVG_ROUTING + "?fromLatitude=" + fromLat + "&fromLongitude=" + fromLong + "&toStation=" + toStation);
    }
}
