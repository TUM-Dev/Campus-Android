package de.tum.in.tumcampusapp.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumcabe.Question;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;

/**
 * Created by dmitriipetukhov on 11/12/16.
 */

public class UCentralClient {

    private static final String API_BASEURL = "https://ucentral.in.tum.de/cgi-bin/";

    private static final String API_LOGIN = "login.cgi";
    private static final String API_LOGOUT = "index.cgi";
    private static final String API_PRINT = "print.cgi";

    private static UCentralClient instance;
    private final UCentralClient.UCentralAPIService service;

    private UCentralClient(final Context c) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(API_BASEURL)
                .addConverterFactory(GsonConverterFactory.create());

        builder.client(Helper.getOkClient(c));
        service = builder.build().create(UCentralClient.UCentralAPIService.class);
    }

    public static synchronized UCentralClient getInstance(Context c) {
        if (instance == null) {
            instance = new UCentralClient(c.getApplicationContext());
        }
        return instance;
    }

    public void login(String username, String password, Callback<Void> cb) {
        service.login(username, password, "Login").enqueue(cb);
    }

    public void login(String username, String password) {
        try {
            Response<Void> r = service.login(username, password, "Login").execute();
            Utils.log(r.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logout(Callback<Void> cb) {
        service.logout("Logout").enqueue(cb);
    }

    public void logout() {
        try {
            Response<Void> r = service.logout("Logout").execute();
            Utils.log(r.message());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printFile(File file, Callback<Void> cb) {
        RequestBody body = RequestBody.create(MediaType.parse("application/pdf"), file);
        RequestBody printer = RequestBody.create(MediaType.parse("form-data"), "xerhalle");
        RequestBody printcount = RequestBody.create(MediaType.parse("form-data"), "1");
        RequestBody orientation = RequestBody.create(MediaType.parse("form-data"), "portrait");
        RequestBody duplex = RequestBody.create(MediaType.parse("form-data"), "duplex");
        RequestBody color = RequestBody.create(MediaType.parse("form-data"), "bw");
        RequestBody pages = RequestBody.create(MediaType.parse("form-data"), "");
        RequestBody submit = RequestBody.create(MediaType.parse("form-data"), "Print");

        // finally, execute the request
        service.printDocument(
                printer,
                printcount,
                orientation,
                duplex,
                color,
                pages,
                body,
                submit).enqueue(cb);
    }

    public void printFile(File file) {
        RequestBody body = RequestBody.create(MediaType.parse("application/pdf"), file);
        RequestBody printer = RequestBody.create(MediaType.parse("form-data"), "xerhalle");
        RequestBody printcount = RequestBody.create(MediaType.parse("form-data"), "1");
        RequestBody orientation = RequestBody.create(MediaType.parse("form-data"), "portrait");
        RequestBody duplex = RequestBody.create(MediaType.parse("form-data"), "duplex");
        RequestBody color = RequestBody.create(MediaType.parse("form-data"), "bw");
        RequestBody pages = RequestBody.create(MediaType.parse("form-data"), "");
        RequestBody submit = RequestBody.create(MediaType.parse("form-data"), "Print");

        // finally, execute the request
        try {
            Response<Void> r = service.printDocument(
                    printer,
                    printcount,
                    orientation,
                    duplex,
                    color,
                    pages,
                    body,
                    submit).execute();
            Utils.log(r.message());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private interface UCentralAPIService {
        @FormUrlEncoded
        @POST(API_BASEURL + API_LOGIN)
        Call<Void> login(@Field("user") String username, @Field("pwd") String password, @Field("login") String field);

        @FormUrlEncoded
        @POST(API_BASEURL + API_LOGOUT)
        Call<Void> logout(@Field("logout") String field);

        @Multipart
        @Headers("Cache-Control: max-age=0")
        @POST(API_BASEURL + API_PRINT)
        Call<Void> printDocument(@Part("printer") RequestBody printer,
                                 @Part("printcount") RequestBody printcount,
                                 @Part("orientation") RequestBody orientation,
                                 @Part("duplex") RequestBody duplex,
                                 @Part("color") RequestBody color,
                                 @Part("pages") RequestBody pages,
                                 @Part("fileupload\"; filename=\"awesome.pdf\"") RequestBody file,
                                 @Part("submit") RequestBody submit);
    }
}
