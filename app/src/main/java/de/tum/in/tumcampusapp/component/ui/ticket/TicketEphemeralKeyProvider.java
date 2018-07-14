package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.stripe.android.EphemeralKeyProvider;
import com.stripe.android.EphemeralKeyUpdateListener;

import java.io.IOException;
import java.util.HashMap;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketEphemeralKeyProvider implements EphemeralKeyProvider {

    private @NonNull
    ProgressListener mProgressListener;
    private Context mContext;

    public TicketEphemeralKeyProvider(@NonNull ProgressListener progressListener,
                                      Context context) {
        mProgressListener = progressListener;
        mContext = context;
    }

    @Override
    public void createEphemeralKey(@NonNull @Size(min = 4) String apiVersion,
                                   @NonNull final EphemeralKeyUpdateListener keyUpdateListener) {
        try {
            TUMCabeClient.getInstance(mContext).retrieveEphemeralKey(mContext, apiVersion, new Callback<HashMap<String, Object>>() {
                @Override
                public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                    String id = response.body().toString();
                    keyUpdateListener.onKeyUpdate(id);
                    mProgressListener.onStringResponse(id);
                }

                @Override
                public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface ProgressListener {
        void onStringResponse(String string);
    }

}