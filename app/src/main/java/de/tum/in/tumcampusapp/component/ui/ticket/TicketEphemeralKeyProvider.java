package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.stripe.android.EphemeralKeyProvider;
import com.stripe.android.EphemeralKeyUpdateListener;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;

public class TicketEphemeralKeyProvider implements EphemeralKeyProvider {

    private @NonNull
    ProgressListener mProgressListener;
    private @NonNull
    Context mContext;

    public TicketEphemeralKeyProvider(@NonNull ProgressListener progressListener,
                                      Context context) {
        mProgressListener = progressListener;
        mContext = context;
    }

    @Override
    public void createEphemeralKey(@NonNull @Size(min = 4) String apiVersion,
                                   @NonNull final EphemeralKeyUpdateListener keyUpdateListener) {


        String key = "";
        Thread thread = new Thread() {
            public void run() {
                try {
                    URL reqURL = new URL("http://localhost/api/event/ticket/payment/stripe/ephemeralkey"); //the URL we will send the request to
                    HttpURLConnection request = (HttpURLConnection) (reqURL.openConnection());
                    String post = "api_version=2017-06-05&customer_mail='example@user.de'";
                    request.setDoOutput(true);

                    request.addRequestProperty("Content-Length", Integer.toString(post.length())); //add the content length of the post data
                    request.addRequestProperty("Content-Type", "application/x-www-form-urlencoded"); //add the content type of the request, most post data is of this type
                    request.setRequestMethod("POST");
                    request.connect();
                    OutputStreamWriter writer = new OutputStreamWriter(request.getOutputStream()); //we will write our request data here
                    writer.write(post);
                    writer.flush();

                    InputStream responseStream = new BufferedInputStream(request.getInputStream());
                    BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
                    String line = "";
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = responseStreamReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    responseStreamReader.close();

                    String response = stringBuilder.toString();
                    System.out.println("Response: " + response);
                    keyUpdateListener.onKeyUpdate(response);
                    mProgressListener.onStringResponse(response);

                    JSONObject jsonResponse = new JSONObject(response);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }



        };
        thread.start();

        //keyUpdateListener.onKeyUpdate(key);



        /*
        String customerMail = "alex_989@gmx.de";

        try {
            key = TUMCabeClient.getInstance(mContext).retrieveEphemeralKey(apiVersion, customerMail);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        mProgressListener.onStringResponse(key);
        keyUpdateListener.onKeyUpdate(key);*/
    }

    public interface ProgressListener {
        void onStringResponse(String string);
    }

}
