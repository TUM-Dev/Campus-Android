package de.tum.in.tumcampusapp.service;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;
import java.util.Date;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.exception.NoPrivateKey;
import de.tum.in.tumcampusapp.api.app.model.DeviceUploadGcmToken;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeStatus;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GcmIdentificationService extends FirebaseInstanceIdService {
    private final Context mContext;

    public GcmIdentificationService() {
        mContext = null;
    }

    public GcmIdentificationService(Context c) {
        mContext = c;
    }

    /**
     * Registers this phone with InstanceID and returns a valid token to be transmitted to the server
     *
     * @return String token that can be used to transmit messages to this client
     */
    public String register() throws IOException {
        FirebaseInstanceId iid = FirebaseInstanceId.getInstance();
        String token = iid.getToken();
        Utils.setSetting(mContext, Const.GCM_INSTANCE_ID, iid.getId());
        Utils.setSetting(mContext, Const.GCM_TOKEN_ID, token);

        return token;
    }

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance()
                                                  .getToken();
        Utils.setSetting(this, Const.GCM_TOKEN_ID, refreshedToken);
    }

    public String getCurrentToken() {
        return Utils.getSetting(this.mContext, Const.GCM_TOKEN_ID, "");
    }

    public void checkSetup() {
        String token = this.getCurrentToken();

        //If we failed, we need to re register
        if (token.isEmpty()) {
            this.registerInBackground();
        } else {
            // If the regId is not empty, we still need to check whether it was successfully sent to the
            // TCA server, because this can fail due to user not confirming their private key
            if (!Utils.getSettingBool(mContext, Const.GCM_REG_ID_SENT_TO_SERVER, false)) {
                this.sendTokenToBackend(token);
            }

            //Update the reg id in steady intervals
            this.checkRegisterIdUpdate(token);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        try {
            //Register a new id
            String token = this.register();

            //Reset the lock in case we are updating and maybe failed
            Utils.setSetting(mContext, Const.GCM_REG_ID_SENT_TO_SERVER, false);
            Utils.setSetting(mContext, Const.GCM_REG_ID_LAST_TRANSMISSION, new Date().getTime());

            // Let the server know of our new registration id
            this.sendTokenToBackend(token);

            Utils.log("GCM registration successful");
        } catch (IOException ex) {
            Utils.log("Error :" + ex.getMessage());
        }
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendTokenToBackend(String token) {
        //Check if all parameters are present
        if (token == null || token.isEmpty()) {
            Utils.logv("Parameter missing for sending reg id");
            return;
        }

        //Try to create the message
        DeviceUploadGcmToken dgcm;
        try {
            dgcm = DeviceUploadGcmToken.Companion.getDeviceUploadGcmToken(mContext, token);
        } catch (NoPrivateKey noPrivateKey) {
            return;
        }

        TUMCabeClient
                .getInstance(mContext)
                .deviceUploadGcmToken(dgcm, new Callback<TUMCabeStatus>() {
                    @Override
                    public void onResponse(@NonNull Call<TUMCabeStatus> call, @NonNull Response<TUMCabeStatus> response) {
                        TUMCabeStatus s = response.body();
                        if (response.isSuccessful() && s != null) {
                            Utils.logv("Success uploading GCM registration id: " + s.getStatus());

                            // Store in shared preferences the information that the GCM registration id
                            // was sent to the TCA server successfully
                            Utils.setSetting(mContext, Const.GCM_REG_ID_SENT_TO_SERVER, true);
                        } else {
                            Utils.logv("Uploading GCM registration failed...");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TUMCabeStatus> call, @NonNull Throwable t) {
                        Utils.log(t, "Failure uploading GCM registration id");
                        Utils.setSetting(mContext, Const.GCM_REG_ID_SENT_TO_SERVER, false);
                    }
                });
    }

    /**
     * Helper function to check if we need to update the regid
     *
     * @param regId registration ID
     */
    private void checkRegisterIdUpdate(String regId) {
        //Regularly (once a day) update the server with the reg id
        long lastTransmission = Utils.getSettingLong(mContext, Const.GCM_REG_ID_LAST_TRANSMISSION, 0L);
        Date now = new Date();
        if (now.getTime() - 24 * 3600000 > lastTransmission) {
            this.sendTokenToBackend(regId);
        }
    }

}
