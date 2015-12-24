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
import com.google.android.gms.common.GoogleApiAvailability;
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
import de.tum.in.tumcampus.models.TUMCabeClient;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatRegistrationId;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.ChatVerification;
import de.tum.in.tumcampus.models.LecturesSearchRow;
import de.tum.in.tumcampus.models.LecturesSearchRowSet;
import de.tum.in.tumcampus.models.managers.ChatRoomManager;
import de.tum.in.tumcampus.services.GcmIdentificationService;
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
        if(this.currentChatMember == null) {
            Utils.showToast(this, getString(R.string.chat_not_setup));
            return;
        }

        Utils.logv("create or join chat room " + name);
        currentChatRoom = new ChatRoom(name);

        TUMCabeClient.getInstance(this).createRoom(currentChatRoom, new ChatVerification(this.currentPrivateKey, this.currentChatMember), new Callback<ChatRoom>() {
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
                List<ChatRoom> rooms = TUMCabeClient.getInstance(this).getMemberRooms(currentChatMember.getId(), new ChatVerification(currentPrivateKey, currentChatMember));
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
        if (GcmIdentificationService.checkPlayServices(this)) {
            GcmIdentificationService idService = new GcmIdentificationService();

            //Check if already registered
            idService.checkSetup();
            return true;
        } else {
            Utils.log("No valid Google Play Services APK found.");
            return false;
        }
    }
}
