package de.tum.in.tumcampusapp.api.tumonline;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.common.base.Optional;
import com.google.common.net.UrlEscapers;
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle;
import com.trello.rxlifecycle2.LifecycleProvider;

import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.model.TokenConfirmation;
import de.tum.in.tumcampusapp.utils.CacheManager;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * This class will handle all action needed to communicate with the TUMOnline
 * XML-RPC backend. ALl communications is based on the base-url which is
 * attached by the Token and additional parameters.
 */
public final class TUMOnlineRequest<T> {
    // server address
    private static final String SERVICE_BASE_URL = "https://campus.tum.de/tumonline/wbservicesbasic.";
    //private static final String SERVICE_BASE_URL = "https://campusquality.tum.de/QSYSTEM_TUM/wbservicesbasic.";

    /**
     * String possibly contained in response from server
     */
    private static final String NO_FUNCTION_RIGHTS = "Keine Rechte für Funktion";

    /**
     * Valid response (no TUMOnline error) but no data available (e.g. no grades yet)
     */
    static final String NO_ENTRIES = "<rowset>\n</rowset>";

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
    private final Optional<LifecycleProvider<Lifecycle.Event>> provider;
    // force to fetch data and fill cache
    private boolean fillCache;
    // set to null, if not needed
    private String accessToken;
    /**
     * method to call
     */
    private TUMOnlineConst<T> method;
    /**
     * a list/map for the needed parameters
     */
    private Map<String, String> parameters;
    private String lastError = "";

    private TUMOnlineRequest(Context context) {
        mContext = context;
        if (context instanceof LifecycleOwner) {
            provider = Optional.of(AndroidLifecycle.createLifecycleProvider((LifecycleOwner) context));
        } else {
            provider = Optional.absent();
        }

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

    public static boolean checkTokenInactive(Context c) {
        TUMOnlineRequest<TokenConfirmation> checkActiveToken = new TUMOnlineRequest<>(TUMOnlineConst.Companion.getTOKEN_CONFIRMED(), c, true);
        Optional<TokenConfirmation> tc = checkActiveToken.fetch();
        if (tc.isPresent()) { //Check that the token is actually active
            if (tc.get()
                  .isConfirmed()) {
                Utils.setSetting(c, Const.TUMO_DISABLED, false);
            } else {
                Utils.setSetting(c, Const.TUMO_DISABLED, true);//Nope its not, deactivate all requests to TUMOnline
                return true;
            }
        }
        //If we don't get anything, fail gracefully
        return false;
    }

    /**
     * Fetches the result of the HTTPRequest (which can be seen by using {@link #getRequestURL()})
     *
     * @return output will be a raw String
     */
    public Optional<T> fetch() {
        // set parameter on the TUMOnline request an fetch the results
        String url = this.getRequestURL();

        if (!TUMOnlineConst.Companion.getREQUEST_TOKEN().equals(method)){
            //If there were some requests that failed and we verified that the token is not active anymore, block all requests directly
            if (!method.equals(TUMOnlineConst.Companion.getTOKEN_CONFIRMED())
                && Utils.getSettingBool(mContext, Const.TUMO_DISABLED, false)) {
                Utils.log("aborting fetch URL, as the token is not active any longer " + url);
                return Optional.absent();
            }

            //Check for error lock
            String lockedError = this.tumManager.checkLock(url);
            if (lockedError != null) {
                //If the token is not active, then fail hard and do not allow any further requests
                if ("Token ist nicht bestätigt oder ungültig!".equals(lockedError)) {
                    TUMOnlineRequest.checkTokenInactive(mContext);
                }

                //Set the error and return
                Utils.log("aborting fetch URL (" + lockedError + ") " + url);
                lastError = lockedError;
                return Optional.absent();
            }
        }

        Utils.log("fetching URL " + url);

        Optional<String> result;
        try {
            result = cacheManager.getFromCache(url);
            if (NetUtils.isConnected(mContext) && (!result.isPresent() || fillCache)) {
                result = net.downloadStringHttp(url);
            }
        } catch (IOException e) {
            lastError = e.getMessage();
            result = Optional.absent();
        }

        T res = null;
        if (result.isPresent()) {
            try {
                res = new Persister().read(method.getResponse(), result.get());
                cacheManager.addToCache(url, result.get(), method.getValidity(), CacheManager.CACHE_TYP_DATA);
                Utils.logv("added to cache " + url);
                Utils.logv(result.get() + " " + res.toString());

                //Release any lock present in the database
                tumManager.releaseLock(url);
            } catch (Exception e) {
                Utils.log(e, "TUMonline request failed");
                //Serialisation failed - lock for a specific time, save the error message
                lastError = tumManager.addLock(url, result.get());
            }
        }

        return Optional.fromNullable(res);
    }

    private <S> ObservableTransformer<S, S> handleLifecycle() {
        if (provider.isPresent()) {
            return provider.get()
                           .bindToLifecycle();
        }
        return observable -> observable;
    }

    /**
     * this fetch method will fetch the data from the TUMOnline Request and will
     * address the listeners onFetch if the fetch succeeded, else the
     * onFetchError will be called.
     *
     * @param context  the current context (may provide the current activity)
     * @param listener the listener, which takes the result
     */
    public void fetchInteractive(final Context context, final TUMOnlineRequestFetchListener<T> listener) {

        if (!loadAccessTokenFromPreferences(context)) {
            listener.onFetchCancelled();
        }

        // fetch information in a background task and show progress dialog in meantime
        Observable.fromCallable(this::fetch)
                  .compose(handleLifecycle())
                  .subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe((result) -> {
                      if (result.isPresent()) {
                          Utils.logv("Received result <" + result + '>');
                      } else {
                          Utils.log("No result available");
                      }

                      // Handles result
                      if (!NetUtils.isConnected(mContext)) {
                          if (result.isPresent()) {
                              Utils.showToast(mContext, R.string.no_internet_connection);
                          } else {
                              listener.onNoInternetError();
                              return;
                          }
                      }

                      //Check for common errors
                      if (!result.isPresent()) {
                          if (lastError.contains(NO_ENTRIES)) {
                              listener.onNoDataToShow();
                              return;
                          }

                          String error;
                          if (lastError.contains(TOKEN_NOT_CONFIRMED)) {
                              error = context.getString(R.string.dialog_access_token_invalid);
                          } else if (lastError.contains(NO_FUNCTION_RIGHTS)) {
                              error = context.getString(R.string.dialog_no_rights_function);
                          } else if (lastError.isEmpty()) {
                              error = context.getString(R.string.empty_result);
                          } else {
                              error = lastError;
                          }
                          listener.onFetchError(error);
                          return;
                      }

                      //Release any lock present in the database
                      tumManager.releaseLock(TUMOnlineRequest.this.getRequestURL());

                      // If there could not be found any problems return usual on Fetch method
                      listener.onFetch(result.get());
                  });
    }

    /**
     * This will return the URL to the TUMOnlineRequest with regard to the set parameters.
     *
     * @return a String URL
     */
    public String getRequestURL() {
        StringBuilder url = new StringBuilder(SERVICE_BASE_URL).append(method)
                                                               .append('?');

        // Builds to be fetched URL based on the base-url and additional parameters
        for (Entry<String, String> pairs : parameters.entrySet()) {
            url.append(pairs.getKey())
               .append('=')
               .append(pairs.getValue())
               .append('&');
        }
        return url.toString();
    }

    /**
     * Check if TUMOnline access token can be retrieved from shared preferences.
     *
     * @param context The context
     * @return true if access token is available; false otherwise
     */
    private boolean loadAccessTokenFromPreferences(Context context) {
        accessToken = PreferenceManager.getDefaultSharedPreferences(context)
                                       .getString(Const.ACCESS_TOKEN, null);

        // no access token set, or it is obviously wrong
        if (accessToken == null || accessToken.length() < 1) {
            return false;
        }

        // ok, access token seems valid (at first)
        setParameter(Const.P_TOKEN, accessToken);
        return true;
    }

    /**
     * Reset parameters to an empty Map.
     */
    private void resetParameters() {
        parameters = new HashMap<>();
        // set accessToken as parameter if available
        if (accessToken != null) {
            parameters.put(Const.P_TOKEN, accessToken);
        }
    }

    /**
     * Sets one parameter name to its given value.
     *
     * @param name  identifier of the parameter
     * @param value value of the parameter
     */
    public void setParameter(String name, String value) {
        parameters.put(name, UrlEscapers.urlPathSegmentEscaper().escape(value));
    }

    public void setParameterEncoded(String name, String value) {
        parameters.put(name, Uri.encode(value));
    }

    public void setForce(boolean force) {
        fillCache = force;
    }

    String getLastError() {
        return this.lastError;
    }
}
