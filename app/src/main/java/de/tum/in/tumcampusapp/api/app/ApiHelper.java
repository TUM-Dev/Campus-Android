package de.tum.in.tumcampusapp.api.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.util.concurrent.TimeUnit;

import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.utils.Utils;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public final class ApiHelper {

    private static final String TAG = "TUM_API_CALL";
    private static final int HTTP_TIMEOUT = 25000;
    private static OkHttpClient client;

    private ApiHelper() {}

    public static OkHttpClient getOkHttpClient(Context c) {
        if (client != null) {
            return client;
        }

        //We want to persist our cookies through app session
        CookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(c));

        //Start building the http client
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cookieJar(cookieJar);

        // Disable gzip for requests as TUMonline
        builder.addInterceptor(ApiHelper.disableGzip());

        //Add the device identifying header
        builder.addInterceptor(ApiHelper.getDeviceInterceptor(c));

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(new ChaosMonkeyInterceptor());
        }

        builder.connectTimeout(ApiHelper.HTTP_TIMEOUT, TimeUnit.MILLISECONDS);
        builder.readTimeout(ApiHelper.HTTP_TIMEOUT, TimeUnit.MILLISECONDS);

        builder.addNetworkInterceptor(new TumHttpLoggingInterceptor(message -> Utils.logWithTag(TAG, message)));

        //Save it to the static handle and return
        client = builder.build();
        return client;
    }

    private static Interceptor disableGzip() {
        return chain -> {
            Request.Builder newRequest = chain.request()
                                              .newBuilder()
                                              .addHeader("Accept-Encoding", "identity");
            return chain.proceed(newRequest.build());
        };
    }

    private static Interceptor getDeviceInterceptor(final Context c) {
        //Clearly identify all requests from this app
        final StringBuilder userAgent = new StringBuilder("TCA Client ");
        userAgent.append(Utils.getAppVersion(c));

        return chain -> {
            Utils.log("Fetching: " + chain.request()
                                          .url());
            Request.Builder newRequest = chain.request()
                                              .newBuilder()
                                              .addHeader("X-DEVICE-ID", AuthenticationManager.getDeviceID(c))
                                              .addHeader("User-Agent", userAgent.toString())
                                              .addHeader("X-ANDROID-VERSION", Build.VERSION.RELEASE);
            try {
                newRequest.addHeader("X-APP-VERSION", c.getPackageManager()
                                                       .getPackageInfo(c.getPackageName(), 0).versionName);
            } catch (PackageManager.NameNotFoundException e) { //NOPMD
                //We don't care. In that case we simply don't send the information
            }

            return chain.proceed(newRequest.build());
        };
    }

}
