package de.tum.in.tumcampusapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.tum.in.tumcampusapp.api.app.Helper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetUtils {
    private final Context mContext;
    private final OkHttpClient client;

    public NetUtils(Context context) {
        //Manager caches all requests
        mContext = context;

        //Set our max wait time for each request
        client = Helper.getOkHttpClient(context);
    }

    @Deprecated
    public static Optional<JSONObject> downloadJson(Context context, String url) {
        return new NetUtils(context).downloadJson(url);
    }

    /**
     * Check if a network connection is available or can be available soon
     *
     * @return true if available
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityMgr == null) {
            return false;
        }

        NetworkInfo netInfo = connectivityMgr.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Check if a network connection is available or can be available soon
     * and if the available connection is a wifi internet connection
     *
     * @return true if available
     */
    public static boolean isConnectedWifi(Context context) {
        ConnectivityManager connectivityMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityMgr == null) {
            return false;
        }

        NetworkInfo netInfo = connectivityMgr.getActiveNetworkInfo();
        return netInfo != null
                && netInfo.isConnectedOrConnecting()
                && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    @Deprecated
    private Optional<ResponseBody> getOkHttpResponse(String url) throws IOException {
        // if we are not online, fetch makes no sense
        boolean isOnline = isConnected(mContext);
        if (!isOnline || Strings.isNullOrEmpty(url) || url.equals("null")) {
            return Optional.absent();
        }

        Utils.logv("Download URL: " + url);
        Request.Builder builder = new Request.Builder().url(url);

        //Execute the request
        Response res = client.newCall(builder.build())
                             .execute();
        return Optional.fromNullable(res.body());
    }

    /**
     * Downloads the content of a HTTP URL as String
     *
     * @param url Download URL location
     * @return The content string
     * @throws IOException when the http call fails
     */
    @Deprecated
    private Optional<String> downloadStringHttp(String url) throws IOException {
        Optional<ResponseBody> response = getOkHttpResponse(url);
        if (response.isPresent()) {
            ResponseBody b = response.get();
            return Optional.of(b.string());

        }
        return Optional.absent();
    }

    private Optional<String> downloadStringAndCache(String url) {
        try {
            Optional<String> content = downloadStringHttp(url);
            if (content.isPresent()) {
                return content;
            }
            return Optional.absent();
        } catch (IOException e) {
            Utils.log(e);
            return Optional.absent();
        }
    }

    /**
     * Download a JSON stream from a URL
     *
     * @param url Valid URL
     * @return JSONObject
     */
    @Deprecated
    private Optional<JSONObject> downloadJson(String url) {
        try {
            Optional<ResponseBody> response = getOkHttpResponse(url);
            if (response.isPresent()) {
                String data = response.get().string();
                Utils.logv("downloadJson " + data);
                return Optional.of(new JSONObject(data));
            }
        } catch (IOException | JSONException e) {
            Utils.log(e);
        }
        return Optional.absent();
    }

    /**
     * Download a JSON stream from a URL or load it from cache
     *
     * @param url   Valid URL
     * @return JSONObject
     */
    @Deprecated
    public Optional<JSONObject> downloadJsonObject(String url) {
        Optional<String> download = downloadStringAndCache(url);
        JSONObject result = null;
        if (download.isPresent()) {
            try {
                result = new JSONObject(download.get());
            } catch (Exception e) {
                Utils.log(e);
            }
        }
        return Optional.fromNullable(result);
    }
}
