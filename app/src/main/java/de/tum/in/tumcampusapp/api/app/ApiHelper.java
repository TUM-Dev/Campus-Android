package de.tum.in.tumcampusapp.api.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.utils.Utils;
import okhttp3.CertificatePinner;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static de.tum.in.tumcampusapp.utils.Const.API_HOSTNAME;
import static de.tum.in.tumcampusapp.utils.Const.API_HOSTNAME_NEW;
import static de.tum.in.tumcampusapp.utils.Const.STUDY_ROOMS_HOSTNAME;

public final class ApiHelper {

    private static final String TAG = "TUM_API_CALL";
    private static final int HTTP_TIMEOUT = 25000;
    private static OkHttpClient client;

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

        builder.addNetworkInterceptor(new TumHttpLoggingInterceptor(message -> Utils.logwithTag(TAG, message)));

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
                                          .url()
                                          .toString());
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

    /**
     * encodes an url
     *
     * @param pUrl input url
     * @return encoded url
     */
    public static String encodeUrl(String pUrl) {
        try {
            return URLEncoder.encode(pUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an offline QR-Code
     *
     * @param message to be encoded
     * @return QR-Code or null if there was an error
     */
    public static Bitmap createQRCode(String message) {
        Writer multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(message, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            Utils.log(e);
            return null;
        }
    }

}
