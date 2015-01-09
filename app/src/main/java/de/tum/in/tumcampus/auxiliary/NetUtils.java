package de.tum.in.tumcampus.auxiliary;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import de.tum.in.tumcampus.models.managers.CacheManager;
import de.tum.in.tumcampus.trace.G;

public class NetUtils {
    private static final int HTTP_TIMEOUT = 25000;

    /* Device id */
    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    private Context mContext;
    private CacheManager cacheManager;
    private DefaultHttpClient client;

    public NetUtils(Context context) {
        //Get the client used for all requests
        client = NetUtils.getClient();

        //Manager caches all requests
        mContext = context;
        cacheManager = new CacheManager(mContext);
    }

    private static DefaultHttpClient getClient() {
        //Get a basic client
        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();

        //Don't allow to continue requests
        HttpProtocolParams.setUseExpectContinue(params, false);

        //Set our max wait time for each request
        HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
        HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);

        //Clearly identify all requests from this app
        params.setParameter(CoreProtocolPNames.USER_AGENT, "TCA Client" + (G.appVersion != null && !G.appVersion.equals("unknown") ? " " + G.appVersion : ""));

        //Actually initiate our client with parameters we setup
        return new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
    }

    public static HttpResponse execute(HttpRequestBase request) throws IOException {
        return NetUtils.getClient().execute(request);
    }

    /**
     * Gets a http entity from the given URL.
     * Adds an X-DEVICE-ID to header
     *
     * @param url Download URL location
     * @return Gets an HttpEntity
     * @throws IOException
     */
    public HttpEntity getHttpEntity(String url) throws IOException {
        // if we are not online, fetch makes no sense
        boolean isOnline = isConnected(mContext);
        if (!isOnline || url == null) {
            return null;
        }

        Utils.logv("Download URL: " + url);
        HttpGet request = new HttpGet(url);
        request.addHeader("X-DEVICE-ID", NetUtils.getDeviceID(mContext));

        //Add some useful statical data
        request.addHeader("X-ANDROID-VERSION", android.os.Build.VERSION.RELEASE);
        try {
            request.addHeader("X-APP-VERSION", mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
        }

        //Execute the request
        HttpResponse response = client.execute(request);
        return response.getEntity();
    }

    /**
     * Downloads the content of a HTTP URL as String
     *
     * @param url Download URL location
     * @return The content string
     * @throws IOException
     */
    public String downloadStringHttp(String url) throws IOException {
        HttpEntity responseEntity = getHttpEntity(url);

        if (responseEntity != null) {
            return EntityUtils.toString(responseEntity);
        }
        return null;
    }

    public String downloadStringAndCache(String url, int validity, boolean force) {
        try {
            String content;
            if (!force) {
                content = cacheManager.getFromCache(url);
                if (content != null) {
                    return content;
                }
            }

            HttpEntity entity = getHttpEntity(url);
            if (entity != null) {
                content = EntityUtils.toString(entity);

                cacheManager.addToCache(url, content, validity, CacheManager.CACHE_TYP_DATA);
                return content;
            }
            return null;
        } catch (Exception e) {
            Utils.log(e);
            return null;
        }
    }

    /**
     * Download a file in the same thread.
     * If file already exists the method returns immediately
     * without downloading anything
     *
     * @param url    Download location
     * @param target Target filename in local file system
     * @throws Exception
     */
    private void downloadToFile(String url, String target) throws Exception {
        File f = new File(target);
        if (f.exists()) {
            return;
        }

        File file = new File(target);
        HttpEntity entity = getHttpEntity(url);
        if (entity == null) {
            return;
        }

        byte[] buffer = EntityUtils.toByteArray(entity);

        FileOutputStream out = new FileOutputStream(file);
        out.write(buffer, 0, buffer.length);
        out.flush();
        out.close();
    }

    /**
     * Downloads an image synchronously from the given url
     *
     * @param url Image url
     * @return Downloaded image as {@link android.graphics.Bitmap}
     */
    public File downloadImage(String url) {
        try {
            url = url.replaceAll(" ", "%20");

            String file = cacheManager.getFromCache(url);
            if (file == null) {
                File cache = mContext.getCacheDir();
                file = cache.getAbsolutePath() + "/" + Utils.md5(url) + ".jpg";
                cacheManager.addToCache(url, file, CacheManager.VALIDITY_TEN_DAYS, CacheManager.CACHE_TYP_IMAGE);
            }

            // If file already exists/was loaded it will return immediately
            // Use this to be sure cache has not been cleaned manually
            File f = new File(file);
            downloadToFile(url, file);

            return f;
        } catch (Exception e) {
            Utils.log(e, url);
            return null;
        }
    }

    /**
     * Downloads an image synchronously from the given url
     *
     * @param url Image url
     * @return Downloaded image as {@link android.graphics.Bitmap}
     */
    public Bitmap downloadImageToBitmap(final String url) {
        File f = downloadImage(url);
        if (f == null)
            return null;
        return BitmapFactory.decodeFile(f.getAbsolutePath());
    }

    /**
     * Download an image in background and sets the image to the image view
     *
     * @param url       URL
     * @param imageView Image
     */
    public void loadAndSetImage(final String url, final ImageView imageView) {
        synchronized (CacheManager.bitmapCache) {
            Bitmap bmp = CacheManager.bitmapCache.get(url);
            if (bmp != null) {
                imageView.setImageBitmap(bmp);
                return;
            }
        }
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                CacheManager.imageViews.put(imageView, url);
                imageView.setImageBitmap(null);
            }

            @Override
            protected Bitmap doInBackground(Void... voids) {
                return downloadImageToBitmap(url);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap == null)
                    return;
                synchronized (CacheManager.bitmapCache) {
                    CacheManager.bitmapCache.put(url, bitmap);
                }
                String tag = CacheManager.imageViews.get(imageView);
                if (tag != null && tag.equals(url)) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }.execute();
    }

    /**
     * Download a JSON stream from a URL
     *
     * @param url Valid URL
     * @return JSONObject
     * @throws Exception
     */
    public JSONObject downloadJson(String url) throws Exception {
        String data = downloadStringHttp(url);

        if (data != null) {
            Utils.logv("downloadJson " + data);
            return new JSONObject(data);
        }
        return null;
    }

    public static JSONObject downloadJson(Context context, String url) throws Exception {
        return new NetUtils(context).downloadJson(url);
    }

    /**
     * Download a JSON stream from a URL or load it from cache
     *
     * @param url   Valid URL
     * @param force Load data anyway and fill cache, even if valid cached version exists
     * @return JSONObject
     */
    public JSONArray downloadJsonArray(String url, int validity, boolean force) {
        try {
            String result = downloadStringAndCache(url, validity, force);
            if (result == null)
                return null;

            return new JSONArray(result);
        } catch (Exception e) {
            Utils.log(e);
            return null;
        }
    }


    /**
     * Check if a network connection is available or can be available soon
     *
     * @return true if available
     */
    public static boolean isConnected(Context con) {
        ConnectivityManager cm = (ConnectivityManager) con
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Check if a network connection is available or can be available soon
     * and if the available connection is a mobile internet connection
     *
     * @return true if available
     */
    public static boolean isConnectedMobileData(Context con) {
        ConnectivityManager cm = (ConnectivityManager) con
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting() && netInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * Gets an unique id that identifies this device
     *
     * @return Unique device id
     */
    public static synchronized String getDeviceID(Context context) {
        if (uniqueID == null) {
            uniqueID = Utils.getInternalSettingString(context, PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                Utils.setInternalSetting(context, PREF_UNIQUE_ID, uniqueID);
            }
        }
        return uniqueID;
    }
}
