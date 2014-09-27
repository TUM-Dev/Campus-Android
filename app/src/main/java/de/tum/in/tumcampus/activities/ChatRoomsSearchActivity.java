package de.tum.in.tumcampus.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.adapters.LecturesListAdapter;
import de.tum.in.tumcampus.adapters.NoResultsAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.RSASigner;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatClient;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatPublicKey;
import de.tum.in.tumcampus.models.ChatRegistrationId;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.LecturesSearchRow;
import de.tum.in.tumcampus.models.LecturesSearchRowSet;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * This activity presents the chat rooms of user's
 * lectures using the TUMOnline web service
 */
public class ChatRoomsSearchActivity extends ActivityForAccessingTumOnline<LecturesSearchRowSet> {
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "1028528438269";

    private StickyListHeadersListView lvMyLecturesList;

    private ChatRoom currentChatRoom;
    private ChatMember currentChatMember;
    private PrivateKey currentPrivateKey;

    private GoogleCloudMessaging gcm;
    private String regId;

    public ChatRoomsSearchActivity() {
        super(TUMOnlineConst.LECTURES_PERSONAL, LecturesSearchRowSet.class, R.layout.activity_lectures);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // bind UI elements
        lvMyLecturesList = (StickyListHeadersListView) findViewById(R.id.lvMyLecturesList);
        lvMyLecturesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                LecturesSearchRow item = (LecturesSearchRow) lvMyLecturesList.getItemAtPosition(position);

                checkPlayServicesAndRegister();

                // set bundle for LectureDetails and show it
                Bundle bundle = new Bundle();
                final Intent intent = new Intent(ChatRoomsSearchActivity.this, ChatActivity.class);
                intent.putExtras(bundle);

                String chatRoomUid = item.getSemester_id() + ":" + item.getTitel();

                currentChatRoom = new ChatRoom(chatRoomUid);
                ChatClient.getInstance().createGroup(currentChatRoom, new Callback<ChatRoom>() {
                    @Override
                    public void success(ChatRoom newlyCreatedChatRoom, Response arg1) {
                        // The POST request is successful because the chat room did not exist
                        // The newly created chat room is returned
                        Utils.logv("Success creating chat room: " + newlyCreatedChatRoom.toString());
                        currentChatRoom = newlyCreatedChatRoom;

                        showTermsIfNeeded(intent);
                    }

                    @Override
                    public void failure(RetrofitError arg0) {
                        // The POST request in unsuccessful because the chat room already exists,
                        // so we are trying to retrieve it with an additional GET request
                        Utils.logv("Failure creating chat room - trying to GET it from the server: " + arg0.toString());
                        List<ChatRoom> chatRooms = ChatClient.getInstance().getChatRoomWithName(currentChatRoom);
                        if(chatRooms!=null)
                            currentChatRoom = chatRooms.get(0);

                        showTermsIfNeeded(intent);
                    }
                });
            }
        });

        requestFetch();

        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        populateCurrentChatMember(sharedPrefs);
    }

    /**
     * Checks device for Play Services APK.
     * Initializes current chat member, if not already initialized
     * shows dialog to enter display name.
     *
     * @param sharedPrefs Default shared preferences object
     */
    private void populateCurrentChatMember(final SharedPreferences sharedPrefs) {
        try {
            if (currentChatMember == null) {
                String lrzId = sharedPrefs.getString(Const.LRZ_ID, "");
                if (sharedPrefs.contains(Const.CHAT_ROOM_DISPLAY_NAME)) {
                    // If this is not the first time this user is opening the chat,
                    // we GET their data from the server using their lrzId
                    List<ChatMember> members = ChatClient.getInstance().getMember(lrzId);
                    currentChatMember = members.get(0);

                    checkPlayServicesAndRegister();
                } else {
                    // If the user is opening the chat for the first time, we need to display
                    // a dialog where they can enter their desired display name
                    currentChatMember = new ChatMember(lrzId);

                    LinearLayout layout = new LinearLayout(this);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    final EditText etDisplayName = new EditText(this);
                    etDisplayName.setHint(R.string.display_name);
                    layout.addView(etDisplayName);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ChatRoomsSearchActivity.this);
                    builder.setTitle(R.string.chat_display_name_title)
                            .setView(layout)
                            .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    currentChatMember.setDisplayName(etDisplayName.getText().toString()); // TODO: Disallow empty display name

                                    // Save display name in shared preferences
                                    Editor editor = sharedPrefs.edit();
                                    editor.putString(Const.CHAT_ROOM_DISPLAY_NAME, currentChatMember.getDisplayName());
                                    editor.apply();

                                    // After the user has entered their display name,
                                    // send a request to the server to create the new member
                                    currentChatMember = ChatClient.getInstance().createMember(currentChatMember);

                                    checkPlayServicesAndRegister();
                                }
                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        } catch (RetrofitError e) {
            Utils.log(e);
            Utils.showToast(this, "Server is currently unavailable");
        }
    }


    @Override
    public void onFetch(LecturesSearchRowSet lecturesList) {
        List<LecturesSearchRow> lectures = lecturesList.getLehrveranstaltungen();

        if (lectures == null) { //TODO set required false in model
            // no results found
            lvMyLecturesList.setAdapter(new NoResultsAdapter(this));
            return;
        }

        // Sort lectures by semester id
        Collections.sort(lectures);

        // set ListView to data via the LecturesListAdapter
        lvMyLecturesList.setAdapter(new LecturesListAdapter(ChatRoomsSearchActivity.this, lectures));
        showLoadingEnded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        populateCurrentChatMember(PreferenceManager.getDefaultSharedPreferences(this));
    }

    /**
     * Displays chat terms if activity is opened for the first time
     * @param intent Intent to start after chat terms have been accepted
     */
    private void showTermsIfNeeded(final Intent intent) {
        // If the terms have not been shown for this chat room, show them
        if (!Utils.getInternalSettingBool(this, Const.CHAT_TERMS_SHOWN + "_" + currentChatRoom.getName(), false)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ChatRoomsSearchActivity.this);
            builder.setTitle(R.string.chat_terms_title)
                    .setMessage(getResources().getString(R.string.chat_terms_body))
                    .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (currentChatMember.getLrzId() != null) {
                                // Generate signature
                                RSASigner signer = new RSASigner(currentPrivateKey);
                                String signature = signer.sign(currentChatMember.getLrzId());
                                currentChatMember.setSignature(signature);

                                ChatClient.getInstance().joinChatRoom(currentChatRoom, currentChatMember, new Callback<ChatRoom>() {
                                    @Override
                                    public void success(ChatRoom arg0, Response arg1) {
                                        Utils.logv("Success joining chat room: " + arg0.toString());
                                        // Remember in sharedPrefs that the terms dialog was shown
                                        Utils.setInternalSetting(ChatRoomsSearchActivity.this, Const.CHAT_TERMS_SHOWN + "_" + currentChatRoom.getName(), true);

                                        moveToChatActivity(intent);
                                    }

                                    @Override
                                    public void failure(RetrofitError e) {
                                        Utils.log(e, "Failure joining chat room");
                                        Utils.showToast(ChatRoomsSearchActivity.this, "Please activate your public key first");
                                    }
                                });
                            }
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else { // If the terms were already shown, just enter the chat room
            moveToChatActivity(intent);
        }
    }

    /**
     * Opens {@link ChatActivity}
     * @param intent Intent for {@link ChatActivity}
     */
    private void moveToChatActivity(final Intent intent) {
        // We need to move to the next activity now and provide the necessary data for it
        // We are sure that both currentChatRoom and currentChatMember exist
        intent.putExtra(Const.CURRENT_CHAT_ROOM, new Gson().toJson(currentChatRoom));
        intent.putExtra(Const.CURRENT_CHAT_MEMBER, new Gson().toJson(currentChatMember));
        startActivity(intent);
    }

    /**
     * Gets private key from preferences or generates one.
     * @return Private key instance
     */
    private PrivateKey retrieveOrGeneratePrivateKey() {
        // Generate/Retrieve private key
        String privateKeyString = Utils.getInternalSettingString(this, Const.PRIVATE_KEY, "");

        if (!privateKeyString.isEmpty()) {
            // If the key is already generated, retrieve it from shared preferences
            byte[] privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT);
            KeyFactory keyFactory;
            try {
                keyFactory = KeyFactory.getInstance("RSA");
                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                return keyFactory.generatePrivate(privateKeySpec);
            } catch (NoSuchAlgorithmException e) {
                Utils.log(e);
            } catch (InvalidKeySpecException e) {
                Utils.log(e);
            }
        } else {
            // If the key is not in shared preferences, generate key-pair
            KeyPairGenerator keyGen;
            try {
                keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(1024);
                KeyPair keyPair = keyGen.generateKeyPair();

                String publicKeyString = Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT);
                privateKeyString = Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.DEFAULT);

                // Save private key in shared preferences
                Utils.setInternalSetting(this, Const.PRIVATE_KEY, privateKeyString);

                // Upload public key to the server
                ChatClient.getInstance().uploadPublicKey(currentChatMember.getUserId(), new ChatPublicKey(publicKeyString), new Callback<ChatPublicKey>() {
                    @Override
                    public void success(ChatPublicKey arg0, Response arg1) {
                        Utils.logv("Success uploading public key: " + arg0.toString());
                        Utils.showToast(ChatRoomsSearchActivity.this, "Public key activation mail sent to " + currentChatMember.getLrzId() + "@mytum.de");
                    }

                    @Override
                    public void failure(RetrofitError e) {
                        Utils.log(e, "Failure uploading public key");
                    }
                });

                return keyPair.getPrivate();
            } catch (NoSuchAlgorithmException e) {
                Utils.log(e);
            }
            return null;
        }
        return null;
    }

    /**
     * Checks if play services are available and registers for GCM
     */
    private void checkPlayServicesAndRegister() {
        currentPrivateKey = retrieveOrGeneratePrivateKey();

        // Check device for Play Services APK. If check succeeds,
        // proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            //getGCMPreferences(getApplicationContext()).edit().remove(Const.GCM_REG_ID).commit();
            regId = getRegistrationId(getApplicationContext());

            if (regId.isEmpty()) {
                registerInBackground();
            } else {
                // If the regId is not empty, we still need to check whether
                // it was successfully sent to the TCA server, because this
                // can fail due to user not confirming their private key
                boolean sentToTCAServer = Utils.getInternalSettingBool(this, Const.GCM_REG_ID_SENT_TO_SERVER, false);
                if (!sentToTCAServer) {
                    sendRegistrationIdToBackend();
                }
            }
        } else {
            Utils.log("No valid Google Play Services APK found.");
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Utils.log("This device is not supported by Google Play services.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     *
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        String registrationId = Utils.getInternalSettingString(this, Const.GCM_REG_ID, "");
        if (registrationId.isEmpty()) {
            Utils.log("Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = Utils.getInternalSettingInt(this, PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Utils.log("App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     *
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    Context context = getApplicationContext();
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(SENDER_ID);
                    msg = "GCM registration successful";

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Utils.log(msg);
            }
        }.execute(null, null, null);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Generate signature
        RSASigner signer = new RSASigner(currentPrivateKey);
        String signature = signer.sign(currentChatMember.getLrzId());

        ChatClient.getInstance().uploadRegistrationId(currentChatMember.getUserId(), new ChatRegistrationId(regId, signature), new Callback<ChatRegistrationId>() {
            @Override
            public void success(ChatRegistrationId arg0, Response arg1) {
                Utils.logv("Success uploading GCM registration id: " + arg0.toString());
                // Store in shared preferences the information that the
                // GCM registration id was sent to the TCA server successfully
                Utils.setInternalSetting(ChatRoomsSearchActivity.this, Const.GCM_REG_ID_SENT_TO_SERVER, true);
            }

            @Override
            public void failure(RetrofitError e) {
                Utils.log(e, "Failure uploading GCM registration id");
            }
        });
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        int appVersion = getAppVersion(context);

        Utils.logv("Saving regId on app version " + appVersion);
        Utils.setInternalSetting(this, Const.GCM_REG_ID, regId);
        Utils.setInternalSetting(this, PROPERTY_APP_VERSION, appVersion);
    }
}
