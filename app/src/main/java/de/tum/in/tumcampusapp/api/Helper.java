package de.tum.in.tumcampusapp.api;

import android.content.Context;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.tum.in.tumcampusapp.auxiliary.AuthenticationManager;
import okhttp3.CertificatePinner;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static de.tum.in.tumcampusapp.auxiliary.Const.API_HOSTNAME;
import static de.tum.in.tumcampusapp.models.managers.StudyRoomGroupManager.STUDYROOM_HOST;
import static de.tum.in.tumcampusapp.models.managers.StudyRoomGroupManager.STUDYROOM_URL;

public class Helper {
    private static final int HTTP_TIMEOUT = 25000;
    private static OkHttpClient client = null;

    public static OkHttpClient getOkClient(Context c) {
        if (client != null) {
            return client;
        }

        //Pin our known fingerprints, which I retrieved on 28. June 2015
        final CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add(API_HOSTNAME, "sha1/eeoui1Gne7kkDN/6HlgoxHkD18s=") //Fakultaet fuer Informatik
                .add(API_HOSTNAME, "sha1/AC508zHZltt8Aa1ZpUg5C9tMNJ8=") //Technische Universitaet Muenchen
                .add(API_HOSTNAME, "sha1/7+NhGLCLRZ1RDbncIhu3ksHeOok=") //DFN-Verein PCA Global
                .add(API_HOSTNAME, "sha1/8GO6fJoWdEqc21TsI81nKY58SU0=") //Deutsche Telekom Root CA 2
                .add(STUDYROOM_HOST, "sha1/8GO6fJoWdEqc21TsI81nKY58SU0=") //Add the DT Root for now
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

        //Save it to the static handle and return
        client = builder.build();
        return client;
    }

    private static Interceptor getDeviceInterceptor(final Context c) {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder()
                        .addHeader("X-DEVICE-ID", AuthenticationManager.getDeviceID(c))
                        .build();
                return chain.proceed(newRequest);
            }
        };
    }
}
