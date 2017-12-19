package de.tum.in.tumcampusapp.api;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.common.net.UrlEscapers;

import java.util.concurrent.TimeUnit;

import de.tum.in.tumcampusapp.auxiliary.AuthenticationManager;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.trace.G;
import okhttp3.CertificatePinner;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static de.tum.in.tumcampusapp.auxiliary.Const.API_HOSTNAME;
import static de.tum.in.tumcampusapp.managers.StudyRoomGroupManager.STUDYROOM_HOST;

public final class Helper {
    private static final String TAG = "TUM_API_CALL";
    private static final int HTTP_TIMEOUT = 25000;
    private static OkHttpClient client;

    public static OkHttpClient getOkClient(Context c) {
        if (client != null) {
            return client;
        }

        final CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add(API_HOSTNAME, "sha256/dVphPQ9xG7woPpEKXrNalw4eMUQ4Fw9r3OXTzxfuL5A=") //Fakultaet fuer Informatik
                .add(API_HOSTNAME, "sha256/SwdQoHL7SB/6o12XsIhbQJ9bANVnbrJoHTLzlu/qXT0=") //Technische Universitaet Muenchen
                .add(API_HOSTNAME, "sha256/VzL+FtAKvzb4N5igmFJyv83GD7CBK7Yyw+R6XdRRfmg=") //DFN-Verein PCA Global
                .add(API_HOSTNAME, "sha256/0d4q5hyN8vpiOWYWPUxz1GC/xCjldYW+a/65pWMj0bY=") //Deutsche Telekom Root CA 2
                .add(API_HOSTNAME, "sha256/YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg=") //Let's Encrypt Authority X3
                .add(API_HOSTNAME, "sha256/Vjs8r4z+80wjNcr1YKepWQboSIRi63WsWXhIMN+eWys=") //LE Cross Sign: DST Root CA X3
                .add(STUDYROOM_HOST, "sha256/dVphPQ9xG7woPpEKXrNalw4eMUQ4Fw9r3OXTzxfuL5A=") //wwwv2.tum.de
                .add(STUDYROOM_HOST, "sha256/K64RzAqr/RSxwfpHN6fe0DcmdaIVmzAyi511ufYaK1s=") //wwwv4.tum.de
                .add(STUDYROOM_HOST, "sha256/SwdQoHL7SB/6o12XsIhbQJ9bANVnbrJoHTLzlu/qXT0=") //Zertifizierungsstelle der TUM
                .add(STUDYROOM_HOST, "sha256/VzL+FtAKvzb4N5igmFJyv83GD7CBK7Yyw+R6XdRRfmg=") //DFN-Verein PCA Global - G01
                .add(STUDYROOM_HOST, "sha256/0d4q5hyN8vpiOWYWPUxz1GC/xCjldYW+a/65pWMj0bY=") //Deutsche Telekom Root CA 2
                .build();

        //We want to persist our cookies through app session
        ClearableCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(c));

        //Start building the http client
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .certificatePinner(certificatePinner);

        //Add the device identifying header
        builder.addInterceptor(Helper.getDeviceInterceptor(c));

        builder.connectTimeout(Helper.HTTP_TIMEOUT, TimeUnit.MILLISECONDS);
        builder.readTimeout(Helper.HTTP_TIMEOUT, TimeUnit.MILLISECONDS);

        builder.addNetworkInterceptor(new TumHttpLoggingInterceptor(message -> Utils.logwithTag(TAG, message)));

        //Save it to the static handle and return
        client = builder.build();
        return client;
    }

    private static Interceptor getDeviceInterceptor(final Context c) {
        //Clearly identify all requests from this app
        final StringBuilder userAgent = new StringBuilder("TCA Client");
        if (G.appVersion != null && !G.appVersion.equals(G.UNKNOWN)) {
            userAgent.append(' ')
                     .append(G.appVersion);
            if (G.appVersionCode != -1) {
                userAgent.append('/')
                         .append(G.appVersionCode);
            }
        }

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
        return UrlEscapers.urlPathSegmentEscaper()
                          .escape(pUrl);
    }

    private Helper() {
        // Helper is a utility class
    }
}
