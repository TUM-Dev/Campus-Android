package de.tum.in.tumcampus.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

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
import java.util.Date;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.adapters.ChatRoomListAdapter;
import de.tum.in.tumcampus.adapters.NoResultsAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.RSASigner;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatClient;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatPublicKey;
import de.tum.in.tumcampus.models.ChatRegistrationId;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.ChatVerification;
import de.tum.in.tumcampus.models.LecturesSearchRow;
import de.tum.in.tumcampus.models.LecturesSearchRowSet;
import de.tum.in.tumcampus.models.managers.ChatRoomManager;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * This activity presents the chat rooms of user's
 * lectures using the TUMOnline web service
 */
public class ChatRoomsSearchActivity extends ActivityForLoadingInBackground<Integer, Cursor> implements OnItemClickListener {
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "944892355389";

    private StickyListHeadersListView lvMyLecturesList;

    private ChatRoom currentChatRoom;
    private ChatMember currentChatMember;
    private PrivateKey currentPrivateKey;
    private TUMOnlineRequest<LecturesSearchRowSet> requestHandler;
    private ChatRoomManager manager;
    private int mCurrentMode = 1;
    private ChatRoomListAdapter adapter;


    public ChatRoomsSearchActivity() {
        super(R.layout.activity_lectures);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // bind UI elements
        lvMyLecturesList = (StickyListHeadersListView) findViewById(R.id.lvMyLecturesList);
        lvMyLecturesList.setOnItemClickListener(this);

        manager = new ChatRoomManager(this);

        //Load the lectures list
        requestHandler = new TUMOnlineRequest<LecturesSearchRowSet>(TUMOnlineConst.LECTURES_PERSONAL, this, true);

        final ActionBar actionBar = getSupportActionBar();

        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // show the given tab
                mCurrentMode = 1 - tab.getPosition();
                startLoading(mCurrentMode);
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };

        actionBar.addTab(actionBar.newTab().setText(R.string.joined).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(R.string.not_joined).setTabListener(tabListener));
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLoading(mCurrentMode);
    }

    /**
     * Checks device for Play Services APK.
     * Initializes current chat member, if not already initialized
     * shows dialog to enter display name.
     */
    private void populateCurrentChatMember() {
        try {
            if (currentChatMember == null) {
                //Fetch the stored LRZ ID from shared prefs
                String lrzId = Utils.getSetting(ChatRoomsSearchActivity.this, Const.LRZ_ID, "");

                // GET their data from the server using their lrzId
                List<ChatMember> members = ChatClient.getInstance(ChatRoomsSearchActivity.this).getMember(lrzId);

                //Catch a possible error, when we didn't get something returned
                if (members.size() == 0) {
                    Utils.showToastOnUIThread(ChatRoomsSearchActivity.this, R.string.error_setup_chat_member);
                    return;
                }

                //Remember this locally
                currentChatMember = members.get(0);

                //Load the private key from the shared prefs
                currentPrivateKey = ChatRoomsSearchActivity.this.retrieveOrGeneratePrivateKey();

                //Proceed with registering
                ChatRoomsSearchActivity.this.checkPlayServicesAndRegister();
            }
        } catch (RetrofitError e) {
            Utils.log(e, e.getMessage());
            Utils.showToastOnUIThread(ChatRoomsSearchActivity.this, R.string.no_internet_connection);
        }
    }

    @Override
    protected Cursor onLoadInBackground(Integer... arg) {
        LecturesSearchRowSet lecturesList = requestHandler.fetch();
        if (lecturesList != null) {
            List<LecturesSearchRow> lectures = lecturesList.getLehrveranstaltungen();
            manager.replaceInto(lectures);
        } else {
            Utils.showToastOnUIThread(this, R.string.no_internet_connection);
        }

        populateCurrentChatMember();

        // Try to restore from server
        try {
            List<ChatRoom> rooms = ChatClient.getInstance(this).getMemberRooms(currentChatMember.getUserId(), new ChatVerification(currentPrivateKey, currentChatMember));
            manager.replaceIntoRooms(rooms);
            return manager.getAllByStatus(arg[0]);
        } catch (RetrofitError e) {
            Utils.log(e);
            return null;
        }
    }

    @Override
    protected void onLoadFinished(Cursor result) {
        showLoadingEnded();
        if (result == null) {
            Utils.showToast(this, "Have you activated your key?\nPublic key activation mail sent to " + currentChatMember.getLrzId() + "@mytum.de");
        } else if (result.getCount() == 0) {
            lvMyLecturesList.setAdapter(new NoResultsAdapter(this));
        } else {
            // set ListView to data via the LecturesListAdapter
            adapter = new ChatRoomListAdapter(this, result);
            lvMyLecturesList.setAdapter(adapter);
        }
    }

    /**
     * Handle click on chat room
     */
    @Override
    public void onItemClick(AdapterView<?> a, View v, int position, long id) {
        Cursor item = (Cursor) lvMyLecturesList.getItemAtPosition(position);

        checkPlayServicesAndRegister();

        // set bundle for LectureDetails and show it
        Bundle bundle = new Bundle();
        final Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtras(bundle);

        String chatRoomUid = item.getString(ChatRoomManager.COL_SEMESTER_ID) + ":"
                + item.getString(ChatRoomManager.COL_NAME);

        currentChatRoom = new ChatRoom(chatRoomUid);
        ChatClient.getInstance(this).createGroup(currentChatRoom, new Callback<ChatRoom>() {
            @Override
            public void success(ChatRoom newlyCreatedChatRoom, Response arg1) {
                // The POST request is successful because the chat room did not exist
                // The newly created chat room is returned
                Utils.logv("Success creating chat room: " + newlyCreatedChatRoom.toString());
                currentChatRoom = newlyCreatedChatRoom;
                manager.join(currentChatRoom);
                moveToChatActivity(intent);
            }

            @Override
            public void failure(RetrofitError arg0) {
                // The POST request in unsuccessful because the chat room already exists,
                // so we are trying to retrieve it with an additional GET request
                Utils.logv("Failure creating chat room - trying to GET it from the server: " + arg0.toString());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<ChatRoom> chatRooms = ChatClient.getInstance(ChatRoomsSearchActivity.this).getChatRoomWithName(currentChatRoom);
                            if (chatRooms != null) {
                                currentChatRoom = chatRooms.get(0);
                            }

                            // When we show joined chat rooms open chat room directly
                            if (mCurrentMode == 1) {
                                ChatRoomsSearchActivity.this.moveToChatActivity(intent);
                            } else { // otherwise join chat room
                                ChatRoomsSearchActivity.this.joinChatRoom();
                            }
                        } catch (RetrofitError e) {
                            Utils.log(e);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showNoInternetLayout();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    /**
     * Joins the chat room and adds it to the list of my chat rooms
     */
    private void joinChatRoom() {
        ChatClient.getInstance(ChatRoomsSearchActivity.this).joinChatRoom(currentChatRoom, new ChatVerification(currentPrivateKey, currentChatMember), new Callback<ChatRoom>() {
            @Override
            public void success(ChatRoom arg0, Response arg1) {
                Utils.logv("Success joining chat room: " + arg0.toString());
                // Remember in sharedPrefs that the terms dialog was shown
                manager.join(currentChatRoom);
                final Cursor newCursor = manager.getAllByStatus(mCurrentMode);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.changeCursor(newCursor);
                        Utils.showToast(ChatRoomsSearchActivity.this, R.string.joined_chat_room);
                    }
                });
            }

            @Override
            public void failure(RetrofitError e) {
                Utils.log(e, "Failure joining chat room");
                Utils.showToastOnUIThread(ChatRoomsSearchActivity.this, R.string.activate_key);
            }
        });
    }

    /**
     * Opens {@link ChatActivity}
     *
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
     *
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
                ChatClient.getInstance(ChatRoomsSearchActivity.this).uploadPublicKey(currentChatMember.getUserId(), new ChatPublicKey(publicKeyString), new Callback<ChatPublicKey>() {
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
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (this.checkPlayServices()) {

            //Check if already registered
            String regId = this.getRegistrationId(getApplicationContext());

            //If we failed, we need to re register
            if (regId.isEmpty()) {
                this.registerInBackground();
            } else {
                // If the regId is not empty, we still need to check whether it was successfully sent to the TCA server, because this can fail due to user not confirming their private key
                if (!Utils.getInternalSettingBool(this, Const.GCM_REG_ID_SENT_TO_SERVER, false)) {
                    this.sendRegistrationIdToBackend(regId);
                }

                //Update the reg id in steady intervals
                this.checkRegisterIdUpdate(regId);
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
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
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
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        //Get the locally stored id
        String registrationId = Utils.getInternalSettingString(this, Const.GCM_REG_ID, "");

        //return if no available to trigger getting a new id
        if (registrationId.isEmpty()) {
            Utils.log("Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID since the existing regID is not guaranteed to work with the new app version.
        int registeredVersion = Utils.getInternalSettingInt(this, PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = Utils.getAppVersion(context);

        //return if no available to trigger getting a new id
        if (registeredVersion != currentVersion) {
            Utils.log("App version changed.");
            return "";
        }
        return registrationId;
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
                    //Get the services and register a new id
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ChatRoomsSearchActivity.this);
                    String regId = gcm.register(SENDER_ID);

                    //Reset the lock in case we are updating and maybe failed
                    Utils.setInternalSetting(ChatRoomsSearchActivity.this, Const.GCM_REG_ID_SENT_TO_SERVER, false);
                    Utils.setInternalSetting(ChatRoomsSearchActivity.this, Const.GCM_REG_ID_LAST_TRANSMISSION, (new Date()).getTime());

                    // Let the server know of our new registration id
                    ChatRoomsSearchActivity.this.sendRegistrationIdToBackend(regId);

                    // Persist the regID - no need to register again.
                    ChatRoomsSearchActivity.this.storeRegistrationId(regId);

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
        // Generate signature
        RSASigner signer = new RSASigner(currentPrivateKey);
        String signature = signer.sign(currentChatMember.getLrzId());

        ChatClient.getInstance(ChatRoomsSearchActivity.this).uploadRegistrationId(currentChatMember.getUserId(), new ChatRegistrationId(regId, signature), new Callback<ChatRegistrationId>() {
            @Override
            public void success(ChatRegistrationId arg0, Response arg1) {
                Utils.logv("Success uploading GCM registration id: " + arg0);

                // Store in shared preferences the information that the GCM registration id was sent to the TCA server successfully
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
     * @param regId registration ID
     */
    private void storeRegistrationId(String regId) {
        int appVersion = Utils.getAppVersion(this);

        Utils.setInternalSetting(this, Const.GCM_REG_ID, regId);
        Utils.setInternalSetting(this, PROPERTY_APP_VERSION, appVersion);

        Utils.logv("Saving regId on app version " + appVersion);
    }

    /**
     * Helper function to check if we need to update the regid
     *
     * @param regId registration ID
     */
    private void checkRegisterIdUpdate(String regId) {
        //Regularly (once a day) update the server with the reg id
        long lastTransmission = Utils.getInternalSettingLong(this, Const.GCM_REG_ID_LAST_TRANSMISSION, 0);
        Date now = new Date();
        if (now.getTime() - 24 * 3600000 > lastTransmission) {
            this.sendRegistrationIdToBackend(regId);
        }
    }
}
