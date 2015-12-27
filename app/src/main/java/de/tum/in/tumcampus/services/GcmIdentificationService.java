package de.tum.in.tumcampus.services;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

import java.io.IOException;
import java.util.Date;

import de.tum.in.tumcampus.activities.ChatRoomsActivity;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.RSASigner;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatRegistrationId;
import de.tum.in.tumcampus.models.TUMCabeClient;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by kordianbruck on 12/24/15.
 */
public class GcmIdentificationService extends InstanceIDListenerService {

    private static final String senderId = "944892355389";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private Context mContext;

    public GcmIdentificationService(Context c) {
        mContext = c;
    }

    /**
     * Registers this phone with InstanceID and returns a valid token to be transmitted to the server
     *
     * @return String token that can be used to transmit messages to this client
     */
    public String register() throws IOException {
        String iid = InstanceID.getInstance(mContext).getId();
        String token = InstanceID.getInstance(mContext).getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
        Utils.setInternalSetting(mContext, Const.GCM_INSTANCE_ID, iid);
        Utils.setInternalSetting(mContext, Const.GCM_TOKEN_ID, token);

        return token;
    }

    public void unregister() throws IOException {
        InstanceID.getInstance(mContext).deleteInstanceID();
        Utils.setInternalSetting(mContext, Const.GCM_INSTANCE_ID, "");
        Utils.setInternalSetting(mContext, Const.GCM_TOKEN_ID, "");
    }

    public void onTokenRefresh() {
        InstanceID iid = InstanceID.getInstance(this);

        try {
            String token = iid.getToken(GcmIdentificationService.senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
            Utils.setInternalSetting(this, Const.GCM_TOKEN_ID, token);
        } catch (IOException e) {
            Utils.log(e, "Failed to refresh token");
        }
        // send this tokenItem.token to your server

    }

    public String getCurrentToken() {
        return Utils.getInternalSettingString(this.mContext, Const.GCM_TOKEN_ID, "");
    }

    public void checkSetup() {
        String regId = this.getCurrentToken();

        //If we failed, we need to re register
        if (regId.isEmpty()) {
            this.registerInBackground();
        } else {
            // If the regId is not empty, we still need to check whether it was successfully sent to the TCA server, because this can fail due to user not confirming their private key
            if (!Utils.getInternalSettingBool(mContext, Const.GCM_REG_ID_SENT_TO_SERVER, false)) {
                this.sendRegistrationIdToBackend(regId);
            }

            //Update the reg id in steady intervals
            this.checkRegisterIdUpdate(regId);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(final Activity a) {
        final int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(a);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {
                a.runOnUiThread(new Runnable() {
                    public void run() {
                        GoogleApiAvailability.getInstance().getErrorDialog(a, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
                    }
                });
            } else {
                Utils.log("This device is not supported by Google Play services.");
            }
            return false;
        }
        return true;

    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    //Register a new id
                    String regId = GcmIdentificationService.this.register();

                    //Reset the lock in case we are updating and maybe failed
                    Utils.setInternalSetting(mContext, Const.GCM_REG_ID_SENT_TO_SERVER, false);
                    Utils.setInternalSetting(mContext, Const.GCM_REG_ID_LAST_TRANSMISSION, (new Date()).getTime());

                    // Let the server know of our new registration id
                    GcmIdentificationService.this.sendRegistrationIdToBackend(regId);

                    return "GCM registration successful";
                } catch (IOException ex) {

                    //Return the error message
                    return "Error :" + ex.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String msg) {
                Utils.log(msg);
            }
        }.execute();
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend(String regId) {
        //@todo
        //Check if all parameters are present
        /*if (regId == null || currentPrivateKey == null || currentChatMember == null || currentChatMember.getLrzId() == null) {
            Utils.logv("Parameter missing for sending reg id");
            return;
        }

        // Generate signature
        RSASigner signer = new RSASigner(currentPrivateKey);
        String signature = signer.sign(currentChatMember.getLrzId());

        TUMCabeClient.getInstance(GcmIdentificationService.this).uploadRegistrationId(currentChatMember.getId(), new ChatRegistrationId(regId, signature), new Callback<ChatRegistrationId>() {
            @Override
            public void success(ChatRegistrationId arg0, Response arg1) {
                Utils.logv("Success uploading GCM registration id: " + arg0);

                // Store in shared preferences the information that the GCM registration id was sent to the TCA server successfully
                Utils.setInternalSetting(GcmIdentificationService.this, Const.GCM_REG_ID_SENT_TO_SERVER, true);
            }

            @Override
            public void failure(RetrofitError e) {
                Utils.log(e, "Failure uploading GCM registration id");
            }
        });*/
    }

    /**
     * Helper function to check if we need to update the regid
     *
     * @param regId registration ID
     */
    private void checkRegisterIdUpdate(String regId) {
        //Regularly (once a day) update the server with the reg id
        long lastTransmission = Utils.getInternalSettingLong(mContext, Const.GCM_REG_ID_LAST_TRANSMISSION, 0);
        Date now = new Date();
        if (now.getTime() - 24 * 3600000 > lastTransmission) {
            this.sendRegistrationIdToBackend(regId);
        }
    }


}
