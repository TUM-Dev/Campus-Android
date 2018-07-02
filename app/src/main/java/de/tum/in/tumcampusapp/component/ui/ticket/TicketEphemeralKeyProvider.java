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
import java.util.HashMap;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;

public class TicketEphemeralKeyProvider implements EphemeralKeyProvider {

    private @NonNull ProgressListener mProgressListener;
    private Context mContext;
    private String mCustomerMail;

    public TicketEphemeralKeyProvider(@NonNull ProgressListener progressListener,
                                      Context context, String customerMail) {
        mProgressListener = progressListener;
        mContext = context;
        mCustomerMail = customerMail;
    }

    @Override
    public void createEphemeralKey(@NonNull @Size(min = 4) String apiVersion,
                                   @NonNull final EphemeralKeyUpdateListener keyUpdateListener) {
        Thread thread = new Thread() {
            public void run() {
                try {
                    HashMap<String, Object> response = TUMCabeClient.getInstance(mContext).retrieveEphemeralKey("2017-06-05", mCustomerMail);
                    String id = response.toString();
                    keyUpdateListener.onKeyUpdate(id);
                    mProgressListener.onStringResponse(id);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public interface ProgressListener {
        void onStringResponse(String string);
    }

}
