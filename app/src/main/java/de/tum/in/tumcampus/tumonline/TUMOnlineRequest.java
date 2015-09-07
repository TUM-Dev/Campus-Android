package de.tum.in.tumcampus.tumonline;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.simpleframework.xml.core.Persister;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CacheManager;
import de.tum.in.tumcampus.models.managers.TumManager;

/**
 * This class will handle all action needed to communicate with the TUMOnline
 * XML-RPC backend. ALl communications is based on the base-url which is
 * attached by the Token and additional parameters.
 */
public class TUMOnlineRequest<T> {
    // server address
    private static final String SERVICE_BASE_URL = "https://campus.tum.de/tumonline/wbservicesbasic.";
    //private static final String SERVICE_BASE_URL = "https://campusquality.tum.de/QSYSTEM_TUM/wbservicesbasic.";

    /**
     * String possibly contained in response from server
     */
    private static final String NO_FUNCTION_RIGHTS = "Keine Rechte für Funktion";

    /**
     * String possibly contained in response from server
     */
    private static final String TOKEN_NOT_CONFIRMED = "Token ist nicht bestätigt oder ungültig!";
    /**
     * NetUtils instance for fetching
     */
    private final NetUtils net;
    private final CacheManager cacheManager;
    private final TumManager tumManager;
    /**
     * Context
     */
    private final Context mContext;
    // force to fetch data and fill cache
    private boolean fillCache = false;
    // set to null, if not needed
    private String accessToken = null;
    /**
     * asynchronous task for interactive fetch
     */
    private AsyncTask<Void, Void, T> backgroundTask = null;
    /**
     * method to call
     */
    private TUMOnlineConst<T> method = null;
    /**
     * a list/map for the needed parameters
     */
    private Map<String, String> parameters;
    private String lastError = "";

    private TUMOnlineRequest(Context context) {
        mContext = context;

        cacheManager = new CacheManager(context);
        tumManager = new TumManager(context);
        net = new NetUtils(context);

        resetParameters();
    }

    public TUMOnlineRequest(TUMOnlineConst<T> method, Context context, boolean needsToken) {
        this(context);
        this.method = method;

        if (needsToken) {
            this.loadAccessTokenFromPreferences(context);
        }
    }

    public TUMOnlineRequest(TUMOnlineConst<T> method, Context context) {
        this(method, context, true);
        this.fillCache = true;
    }

    public void cancelRequest(boolean mayInterruptIfRunning) {
        // Cancel background task just if one has been established
        if (backgroundTask != null) {
            backgroundTask.cancel(mayInterruptIfRunning);
        }
    }

    /**
     * Fetches the result of the HTTPRequest (which can be seen by using {@link #getRequestURL()})
     *
     * @return output will be a raw String
     */
    public T fetch() {
        // set parameter on the TUMOnline request an fetch the results
        String result;
        String url = this.getRequestURL();

        //Check for error lock
        String error = this.tumManager.checkLock(url);
        if (error != null) {
            Utils.log("aborting fetch URL (" + error + ") " + url);
            lastError = error;
            return null;
        }

        Utils.log("fetching URL " + url);
        boolean addToCache = false;

        try {
            result = cacheManager.getFromCache(url);
            if (result == null || fillCache) {
                boolean isOnline = NetUtils.isConnected(mContext);
                if (!isOnline) {
                    // not online, fetch does not make sense
                    return null;
                }

                result = net.downloadStringHttp(url);
                addToCache = true;
            } else {
                Utils.logv("loaded from cache " + url);
            }
        } catch (Exception e) {
            Utils.log(e, "FetchError");
            lastError = e.getMessage();
            return null;
        }

        T res = null;
        try {
            res = (new Persister()).read(method.getResponse(), result);

            // Only add to cache if data is valid
            if (addToCache) {
                cacheManager.addToCache(url, result, method.getValidity(), CacheManager.CACHE_TYP_DATA);
                Utils.logv("added to cache " + url);
            }

            //Release any lock present in the database
            tumManager.releaseLock(url);
        } catch (Exception e) {
            //Serialisation failed - lock for a specific time, save the error message
            lastError = tumManager.addLock(url, result);
        }

        return res;
    }

    /**
     * this fetch method will fetch the data from the TUMOnline Request and will
     * address the listeners onFetch if the fetch succeeded, else the
     * onFetchError will be called
     *
     * @param context  the current context (may provide the current activity)
     * @param listener the listener, which takes the result
     */
    public void fetchInteractive(final Context context, final TUMOnlineRequestFetchListener<T> listener) {

        if (!loadAccessTokenFromPreferences(context)) {
            listener.onFetchCancelled();
        }

        // fetch information in a background task and show progress dialog in
        // meantime
        backgroundTask = new AsyncTask<Void, Void, T>() {

            @Override
            protected T doInBackground(Void... params) {
                // we are online, return fetch result
                return fetch();
            }

            @Override
            protected void onPostExecute(T result) {
                if (result != null) {
                    Utils.logv("Received result <" + result + ">");
                } else {
                    Utils.log("No result available");
                }

                // Handles result
                if (!NetUtils.isConnected(mContext)) {
                    if (result == null) {
                        listener.onNoInternetError();
                        return;
                    } else {
                        Utils.showToast(mContext, R.string.no_internet_connection);
                    }
                }

                //Check for common errors
                if (result == null) {
                    if (lastError.contains(TOKEN_NOT_CONFIRMED)) {
                        listener.onFetchError(context.getString(R.string.dialog_access_token_invalid));
                        return;
                    } else if (lastError.contains(NO_FUNCTION_RIGHTS)) {
                        listener.onFetchError(context.getString(R.string.dialog_no_rights_function));
                        return;
                    } else if (lastError.length() > 0) {
                        listener.onFetchError(lastError);
                        return;
                    } else {
                        listener.onFetchError(context.getString(R.string.empty_result));
                        return;
                    }
                }

                //Release any lock present in the database
                tumManager.releaseLock(TUMOnlineRequest.this.getRequestURL());

                // If there could not be found any problems return usual on Fetch method
                listener.onFetch(result);
            }

        };
        backgroundTask.execute();
    }

    /**
     * This will return the URL to the TUMOnlineRequest with regard to the set parameters
     *
     * @return a String URL
     */
    public String getRequestURL() {
        String url = SERVICE_BASE_URL + method + "?";

        // Builds to be fetched URL based on the base-url and additional parameters
        for (Entry<String, String> pairs : parameters.entrySet()) {
            url += pairs.getKey() + "=" + pairs.getValue() + "&";
        }
        return url;
    }

    /**
     * Check if TUMOnline access token can be retrieved from shared preferences.
     *
     * @param context The context
     * @return true if access token is available; false otherwise
     */
    private boolean loadAccessTokenFromPreferences(Context context) {
        accessToken = PreferenceManager.getDefaultSharedPreferences(context).getString(Const.ACCESS_TOKEN, null);

        // no access token set, or it is obviously wrong
        if (accessToken == null || accessToken.length() < 1) {
            return false;
        }

        Utils.logv("AccessToken = " + accessToken);

        // ok, access token seems valid (at first)
        setParameter(Const.P_TOKEN, accessToken);
        return true;
    }

    /**
     * Reset parameters to an empty Map
     */
    void resetParameters() {
        parameters = new HashMap<>();
        // set accessToken as parameter if available
        if (accessToken != null) {
            parameters.put(Const.P_TOKEN, accessToken);
        }
    }

    /**
     * Sets one parameter name to its given value
     *
     * @param name  identifier of the parameter
     * @param value value of the parameter
     */
    public void setParameter(String name, String value) {
        try {
            parameters.put(name, URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Utils.log(e);
        }
    }

    public void setForce(boolean force) {
        fillCache = force;
    }

    public String getLastError(){
        return this.lastError;
    }
}
