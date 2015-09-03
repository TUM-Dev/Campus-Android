package de.tum.in.tumcampus.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.PrivateKey;
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
public class ChatRoomsActivity extends ActivityForLoadingInBackground<Void, Cursor> implements OnItemClickListener {
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "944892355389";

    private StickyListHeadersListView lvMyChatRoomList;

    private ChatRoom currentChatRoom;
    private ChatMember currentChatMember;
    private PrivateKey currentPrivateKey;
    private TUMOnlineRequest<LecturesSearchRowSet> requestHandler;
    private ChatRoomManager manager;
    private int mCurrentMode = 1;
    private ChatRoomListAdapter adapter;
    private boolean firstLoad = true;


    public ChatRoomsActivity() {
        super(R.layout.activity_chat_rooms);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // bind UI elements
        lvMyChatRoomList = (StickyListHeadersListView) findViewById(R.id.lvMyChatRoomList);
        lvMyChatRoomList.setOnItemClickListener(this);

        manager = new ChatRoomManager(this);

        //Load the lectures list
        requestHandler = new TUMOnlineRequest<>(TUMOnlineConst.LECTURES_PERSONAL, this, true);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.chat_rooms_tabs);
        // Create a tab listener that is called when the user changes tabs.
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // show the given tab
                mCurrentMode = 1 - tab.getPosition();
                firstLoad = true;
                startLoading();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // hide the given tab
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // probably ignore this event
            }
        });

        tabLayout.addTab(tabLayout.newTab().setText(R.string.joined));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.not_joined));
    }

    @Override
    protected void onResume() {
        super.onResume();
        firstLoad = true;
        startLoading();
    }

    /**
     * Checks device for Play Services APK.
     * Initializes current chat member, if not already initialized
     * shows dialog to enter display name.
     */
    private void populateCurrentChatMember() {
        try {
            if (currentChatMember == null) {

                // Remember this locally
                currentChatMember = Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class);

                // Load the private key from the shared prefs
                currentPrivateKey = Utils.getPrivateKeyFromSharedPrefs(this);

                // Proceed with registering
                checkPlayServicesAndRegister();
            }
        } catch (RetrofitError e) {
            Utils.log(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_activity_chat_rooms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_chat_room:
                newChatRoom();
                return true;
            case R.id.action_join_chat_room:
                startActivityForResult(new Intent(this, JoinRoomScanActivity.class), 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("name");
            if (name.charAt(3) == ':')
                createOrJoinChatRoom(name);
            else
                Utils.showToast(this, R.string.invalid_chat_room);
        }
    }

    /**
     * Prompt the user to type in a name for the new chat room
     */
    private void newChatRoom() {
        // Set an EditText view to get user input
        final EditText input = new EditText(this);

        new AlertDialog.Builder(this)
                .setTitle(R.string.new_chat_room)
                .setMessage(R.string.new_chat_room_desc)
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        String randId = Integer.toHexString((int) (Math.random() * 4096));
                        createOrJoinChatRoom(randId + ":" + value);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null).show();
    }

    /**
     * Creates a given chat room if it does not exist and joins it
     * Works asynchronously.
     */
    private void createOrJoinChatRoom(String name) {
        Utils.logv("create or join chat room " + name);
        currentChatRoom = new ChatRoom(name);

        ChatClient.getInstance(this).createRoom(currentChatRoom, new ChatVerification(this.currentPrivateKey, this.currentChatMember), new Callback<ChatRoom>() {
            @Override
            public void success(ChatRoom newlyCreatedChatRoom, Response arg1) {
                // The POST request is successful: go to room. API should have auto joined it
                Utils.logv("Success creating&joining chat room: " + newlyCreatedChatRoom.toString());
                currentChatRoom = newlyCreatedChatRoom;
                manager.join(currentChatRoom);

                // When we show joined chat rooms open chat room directly
                if (mCurrentMode == 1) {
                    moveToChatActivity();
                } else { //Otherwise show a nice information, that we added the room
                    final Cursor newCursor = manager.getAllByStatus(mCurrentMode);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.changeCursor(newCursor);
                            Utils.showToast(ChatRoomsActivity.this, R.string.joined_chat_room);
                        }
                    });
                }
            }

            @Override
            public void failure(RetrofitError arg0) {
                //Something went wrong while joining
                Utils.logv("Failure creating/joining chat room - trying to GET it from the server: " + arg0.toString());
                Utils.showToastOnUIThread(ChatRoomsActivity.this, R.string.activate_key);
            }
        });
    }

    @Override
    protected Cursor onLoadInBackground(Void... arg) {
        if (!firstLoad) {
            LecturesSearchRowSet lecturesList = requestHandler.fetch();
            if (lecturesList != null) {
                List<LecturesSearchRow> lectures = lecturesList.getLehrveranstaltungen();
                manager.replaceInto(lectures);
            }
        }

        this.populateCurrentChatMember();

        // Try to restore joined chat rooms from server
        if (!firstLoad && currentChatMember != null) {
            try {
                List<ChatRoom> rooms = ChatClient.getInstance(this).getMemberRooms(currentChatMember.getId(), new ChatVerification(currentPrivateKey, currentChatMember));
                manager.replaceIntoRooms(rooms);
            } catch (RetrofitError e) {
                Utils.log(e);
            }
        }
        firstLoad = false;
        return manager.getAllByStatus(mCurrentMode);
    }

    @Override
    protected void onLoadFinished(Cursor result) {
        showLoadingEnded();
        if (result.getCount() == 0) {
            lvMyChatRoomList.setAdapter(new NoResultsAdapter(this));
        } else {
            // set ListView to data via the LecturesListAdapter
            adapter = new ChatRoomListAdapter(this, result, mCurrentMode);
            lvMyChatRoomList.setAdapter(adapter);
        }
    }

    /**
     * Handle click on chat room
     */
    @Override
    public void onItemClick(AdapterView<?> a, View v, int position, long id) {
        Cursor item = (Cursor) lvMyChatRoomList.getItemAtPosition(position);

        if (firstLoad || //No clicking until everything is loaded
                !checkPlayServicesAndRegister())
            return;

        // set bundle for LectureDetails and show it
        Bundle bundle = new Bundle();
        final Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtras(bundle);

        String chatRoomUid = item.getString(ChatRoomManager.COL_SEMESTER_ID) + ":" + item.getString(ChatRoomManager.COL_NAME);
        this.createOrJoinChatRoom(chatRoomUid);
    }

    /**
     * Opens {@link ChatActivity}
     */
    private void moveToChatActivity() {
        // We need to move to the next activity now and provide the necessary data for it
        // We are sure that both currentChatRoom and currentChatMember exist
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Const.CURRENT_CHAT_ROOM, new Gson().toJson(currentChatRoom));
        startActivity(intent);
    }

    /**
     * Checks if play services are available and registers for GCM
     */
    private boolean checkPlayServicesAndRegister() {
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
            return true;
        } else {
            Utils.log("No valid Google Play Services APK found.");
            return false;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {

            final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    this.runOnUiThread(new Runnable() {
                        public void run() {
                            GooglePlayServicesUtil.getErrorDialog(resultCode, ChatRoomsActivity.this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
                        }
                    });
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
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ChatRoomsActivity.this);
                    String regId = gcm.register(SENDER_ID);

                    //Reset the lock in case we are updating and maybe failed
                    Utils.setInternalSetting(ChatRoomsActivity.this, Const.GCM_REG_ID_SENT_TO_SERVER, false);
                    Utils.setInternalSetting(ChatRoomsActivity.this, Const.GCM_REG_ID_LAST_TRANSMISSION, (new Date()).getTime());

                    // Let the server know of our new registration id
                    ChatRoomsActivity.this.sendRegistrationIdToBackend(regId);

                    // Persist the regID - no need to register again.
                    ChatRoomsActivity.this.storeRegistrationId(regId);

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
        //Check if all parameters are present
        if (regId == null || currentPrivateKey == null || currentChatMember == null || currentChatMember.getLrzId() == null) {
            Utils.logv("Parameter missing for sending reg id");
            return;
        }

        // Generate signature
        RSASigner signer = new RSASigner(currentPrivateKey);
        String signature = signer.sign(currentChatMember.getLrzId());

        ChatClient.getInstance(ChatRoomsActivity.this).uploadRegistrationId(currentChatMember.getId(), new ChatRegistrationId(regId, signature), new Callback<ChatRegistrationId>() {
            @Override
            public void success(ChatRegistrationId arg0, Response arg1) {
                Utils.logv("Success uploading GCM registration id: " + arg0);

                // Store in shared preferences the information that the GCM registration id was sent to the TCA server successfully
                Utils.setInternalSetting(ChatRoomsActivity.this, Const.GCM_REG_ID_SENT_TO_SERVER, true);
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
