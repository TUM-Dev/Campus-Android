package de.tum.in.tumcampusapp.auxiliary;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ImageView;

import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.tum.in.tumcampusapp.models.managers.CacheManager;
import de.tum.in.tumcampusapp.trace.G;

public class NetUtils {
    private static final int HTTP_TIMEOUT = 25000;
    private final Context mContext;
    private final CacheManager cacheManager;
    private final OkHttpClient client = new OkHttpClient();

    public NetUtils(Context context) {
        //Manager caches all requests
        mContext = context;
        cacheManager = new CacheManager(mContext);

        //Set our max wait time for each request
        client.setConnectTimeout(HTTP_TIMEOUT, TimeUnit.MILLISECONDS);
        client.setReadTimeout(HTTP_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public static Optional<JSONObject> downloadJson(Context context, String url) throws IOException, JSONException {
        return new NetUtils(context).downloadJson(url);
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
     * Check if a network connection is available or can be available soon
     * and if the available connection is a wifi internet connection
     *
     * @return true if available
     */
    public static boolean isConnectedWifi(Context con) {
        ConnectivityManager cm = (ConnectivityManager) con
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting() && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    private void setHttpConnectionParams(Request.Builder builder) {
        //Clearly identify all requests from this app
        String userAgent = "TCA Client";
        if (G.appVersion != null && !G.appVersion.equals(G.UNKNOWN)) {
            userAgent += ' ' + G.appVersion;
            if (G.appVersionCode != -1) {
                userAgent += "/" + G.appVersionCode;
            }
        }

        try {
            builder.header("User-Agent", userAgent);
            builder.addHeader("X-DEVICE-ID", AuthenticationManager.getDeviceID(mContext));
            builder.addHeader("X-ANDROID-VERSION", Build.VERSION.RELEASE);
            builder.addHeader("X-APP-VERSION", mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            //Don't log any errors, as we don't really care!
        }
    }

    private Optional<ResponseBody> getOkHttpResponse(String url) throws IOException {
        // if we are not online, fetch makes no sense
        boolean isOnline = isConnected(mContext);
        if (!isOnline || url == null) {
            return Optional.absent();
        }

        Utils.logv("Download URL: " + url);

        Request.Builder builder = new Request.Builder().url(url);
        setHttpConnectionParams(builder);

        //Execute the request
        Request req = builder.build();
        Response res = client.newCall(req).execute();
        return Optional.of(res.body());
    }

    /**
     * Downloads the content of a HTTP URL as String
     *
     * @param url Download URL location
     * @return The content string
     * @throws IOException
     */
    public Optional<String> downloadStringHttp(String url) throws IOException {
        Optional<ResponseBody> body = getOkHttpResponse(url);
        if (body.isPresent()) {
            return Optional.of(body.get().string());
        }
        return Optional.absent();
    }

    public Optional<String> downloadStringAndCache(String url, int validity, boolean force) {
        try {
            Optional<String> content;
            if (!force) {
                content = cacheManager.getFromCache(url);
                if (content.isPresent()) {
                    return content;
                }
            }

            content = downloadStringHttp(url);
            if (content.isPresent()) {
                cacheManager.addToCache(url, content.get(), validity, CacheManager.CACHE_TYP_DATA);
                return content;
            }
            return Optional.absent();
        } catch (IOException e) {
            Utils.log(e);
            return Optional.absent();
        }

    }

    /**
     * Download a file in the same thread.
     * If file already exists the method returns immediately
     * without downloading anything
     *
     * @param url    Download location
     * @param target Target filename in local file system
     * @throws IOException When the download failed
     */
    public void downloadToFile(String url, String target) throws IOException {
        File f = new File(target);
        if (f.exists()) {
            return;
        }

        File file = new File(target);
        FileOutputStream out = new FileOutputStream(file);
        try {
            Optional<ResponseBody> body = getOkHttpResponse(url);
            if (!body.isPresent()) {
                throw new IOException();
            }
            byte[] buffer = body.get().bytes();
            out.write(buffer, 0, buffer.length);
            out.flush();
        } finally {
            out.close();
        }
    }

    /**
     * Downloads an image synchronously from the given url
     *
     * @param pUrl Image url
     * @return Downloaded image as {@link Bitmap}
     */
    public Optional<File> downloadImage(String pUrl) {
        try {
            String url = pUrl.replaceAll(" ", "%20");

            Optional<String> file = cacheManager.getFromCache(url);
            if (file.isPresent()) {
                return Optional.of(new File(file.get()));
            }

            file = Optional.of(mContext.getCacheDir().getAbsolutePath() + '/' + Utils.md5(url) + ".jpg");
            File f = new File(file.get());
            downloadToFile(url, file.get());

            // At this point, we are certain, that the file really has been downloaded and can safely be added to the cache
            cacheManager.addToCache(url, file.get(), CacheManager.VALIDITY_TEN_DAYS, CacheManager.CACHE_TYP_IMAGE);
            return Optional.of(f);
        } catch (IOException e) {
            Utils.log(e, pUrl);
            return Optional.absent();
        }
    }

    /**
     * Downloads an image synchronously from the given url
     *
     * @param url Image url
     * @return Downloaded image as {@link Bitmap}
     */
    public Optional<Bitmap> downloadImageToBitmap(final String url) {
        Optional<File> f = downloadImage(url);
        if (f.isPresent()) {
            return Optional.of(BitmapFactory.decodeFile(f.get().getAbsolutePath()));
        }
        return Optional.absent();
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
        new AsyncTask<Void, Void, Optional<Bitmap>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                CacheManager.imageViews.put(imageView, url);
                imageView.setImageBitmap(null);
            }

            @Override
            protected Optional<Bitmap> doInBackground(Void... voids) {
                return downloadImageToBitmap(url);
            }

            @Override
            protected void onPostExecute(Optional<Bitmap> bitmap) {
                if (!bitmap.isPresent()) {
                    return;
                }
                synchronized (CacheManager.bitmapCache) {
                    CacheManager.bitmapCache.put(url, bitmap.get());
                }
                String tag = CacheManager.imageViews.get(imageView);
                if (tag != null && tag.equals(url)) {
                    imageView.setImageBitmap(bitmap.get());
                }
            }
        }.execute();
    }

    /**
     * Download a JSON stream from a URL
     *
     * @param url Valid URL
     * @return JSONObject
     * @throws IOException, JSONException
     */
    public Optional<JSONObject> downloadJson(String url) throws IOException, JSONException {
        Optional<String> data = downloadStringHttp(url);

        if (data.isPresent()) {
            Utils.logv("downloadJson " + data);
            return Optional.of(new JSONObject(data.get()));
        }
        return Optional.absent();
    }

    /**
     * Download a JSON stream from a URL or load it from cache
     *
     * @param url   Valid URL
     * @param force Load data anyway and fill cache, even if valid cached version exists
     * @return JSONObject
     */
    public Optional<JSONArray> downloadJsonArray(String url, int validity, boolean force) {
        Optional<String> download = downloadStringAndCache(url, validity, force);
        JSONArray result = null;
        if (download.isPresent()) {
            try {
                result = new JSONArray(download.get());
            } catch (JSONException e) {
                Utils.log(e);
            }
        }
        return Optional.fromNullable(result);
    }

    /**
     * Download a JSON stream from a URL or load it from cache
     *
     * @param url   Valid URL
     * @param force Load data anyway and fill cache, even if valid cached version exists
     * @return JSONObject
     */
    public Optional<JSONObject> downloadJsonObject(String url, int validity, boolean force) {
        Optional<String> download = downloadStringAndCache(url, validity, force);
        JSONObject result = null;
        if (download.isPresent()) {
            try {
                result = new JSONObject(download.get());
            } catch (JSONException e) {
                Utils.log(e);
            }
        }
        return Optional.fromNullable(result);
    }
}
