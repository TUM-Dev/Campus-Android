package de.tum.in.tumcampusapp.component.ui.chat.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeVerification;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.other.generic.adapter.NoResultsAdapter;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.Lecture;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.LecturesResponse;
import de.tum.in.tumcampusapp.component.ui.chat.ChatRoomController;
import de.tum.in.tumcampusapp.component.ui.chat.adapter.ChatRoomListAdapter;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoom;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoomAndLastMessage;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * This activity presents the chat rooms of user's
 * lectures using the TUMOnline web service
 */
public class ChatRoomsActivity
        extends ActivityForAccessingTumOnline<LecturesResponse> implements OnItemClickListener {

    private static final int CAMERA_REQUEST_CODE = 34;
    private static final int JOIN_ROOM_REQUEST_CODE = 22;

    private ChatRoomController manager;
    private int mCurrentMode = ChatRoom.MODE_JOINED;

    private ChatRoom currentChatRoom;
    private ChatMember currentChatMember;

    private StickyListHeadersListView lvMyChatRoomList;
    private ChatRoomListAdapter chatRoomAdapter;

    public ChatRoomsActivity() {
        super(R.layout.activity_chat_rooms);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lvMyChatRoomList = findViewById(R.id.lvMyChatRoomList);
        lvMyChatRoomList.setOnItemClickListener(this);

        manager = new ChatRoomController(this);

        TabLayout tabLayout = findViewById(R.id.chat_rooms_tabs);
        // Create a tab listener that is called when the user changes tabs.
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // show the given tab
                mCurrentMode = 1 - tab.getPosition();
                loadPersonalLectures(CacheControl.USE_CACHE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // hide the given tab
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // TODO: Caching?
                // probably ignore this event
            }
        });

        tabLayout.addTab(tabLayout.newTab().setText(R.string.joined));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.not_joined));
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPersonalLectures(CacheControl.USE_CACHE);
    }

    @Override
    public void onRefresh() {
        loadPersonalLectures(CacheControl.BYPASS_CACHE);
    }

    private void loadPersonalLectures(CacheControl cacheControl) {
        Call<LecturesResponse> apiCall = apiClient.getPersonalLectures(cacheControl);
        fetch(apiCall);
    }

    @Override
    protected void onDownloadSuccessful(@NonNull LecturesResponse response) {
        List<Lecture> lectures = response.getLectures();

        // We're starting more background work, so we show a loading indicator again
        showLoadingStart();
        Handler handler = new Handler();
        handler.post(() -> createLectureRoomsAndUpdateDatabase(lectures));
    }

    @WorkerThread
    private void createLectureRoomsAndUpdateDatabase(List<Lecture> lectures) {
        manager.createLectureRooms(lectures);

        populateCurrentChatMember();

        if (currentChatMember != null) {
            try {
                TUMCabeVerification verification = TUMCabeVerification.create(this, null);
                if (verification == null) {
                    finish();
                    return;
                }

                List<ChatRoom> rooms = TUMCabeClient
                        .getInstance(this)
                        .getMemberRooms(currentChatMember.getId(), verification);
                manager.replaceIntoRooms(rooms);
            } catch (IOException e) {
                Utils.log(e);
            }
        }

        List<ChatRoomAndLastMessage> chatRoomAndLastMessages = manager.getAllByStatus(mCurrentMode);
        runOnUiThread(() -> {
                displayChatRoomsAndMessages(chatRoomAndLastMessages);
                showLoadingEnded();
        });
    }

    private void displayChatRoomsAndMessages(List<ChatRoomAndLastMessage> results) {
        if (results.isEmpty()) {
            lvMyChatRoomList.setAdapter(new NoResultsAdapter(this));
        } else {
            chatRoomAdapter = new ChatRoomListAdapter(this, results, mCurrentMode);
            lvMyChatRoomList.setAdapter(chatRoomAdapter);
        }
    }

    /**
     * Gets the saved local information for the user
     */
    private void populateCurrentChatMember() {
        if (currentChatMember == null) {
            currentChatMember = Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class);
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
        int i = item.getItemId();
        if (i == R.id.action_add_chat_room) {
            newChatRoom();
            return true;
        } else if (i == R.id.action_join_chat_room) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permissionCheck = checkSelfPermission(Manifest.permission.CAMERA);
                if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                } else {
                    startJoinRoom();
                }
            } else {
                startJoinRoom();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void startJoinRoom() {
        startActivityForResult(new Intent(this, JoinRoomScanActivity.class), JOIN_ROOM_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == JOIN_ROOM_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("name");
            if (name.charAt(3) == ':') {
                createOrJoinChatRoom(name);
            } else {
                Utils.showToast(this, R.string.invalid_chat_room);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE
            && grantResults.length >= 1
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startJoinRoom();
        }
    }

    /**
     * Prompt the user to type in a name for the new chat room
     */
    private void newChatRoom() {
        // Set an EditText view to get user input
        final View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_input, null);
        final EditText input = view.findViewById(R.id.inputEditText);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.new_chat_room)
                .setMessage(R.string.new_chat_room_desc)
                .setView(view)
                .setPositiveButton(R.string.create, (dialogInterface, whichButton) -> {
                    String value = input.getText().toString();
                    String randId = Integer.toHexString((int) (Math.random() * 4096));
                    createOrJoinChatRoom(randId + ':' + value);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        dialog.show();
    }

    /**
     * Creates a given chat room if it does not exist and joins it
     * Works asynchronously.
     */
    private void createOrJoinChatRoom(String name) {
        Utils.logv("create or join chat room " + name);
        if (this.currentChatMember == null) {
            Utils.showToast(this, getString(R.string.chat_not_setup));
            return;
        }

        currentChatRoom = new ChatRoom(name);

        TUMCabeVerification verification = TUMCabeVerification.create(this, null);
        if (verification == null) {
            finish();
            return;
        }

        Callback<ChatRoom> callback = new Callback<ChatRoom>() {
            @Override
            public void onResponse(@NonNull Call<ChatRoom> call, @NonNull Response<ChatRoom> response) {
                if (!response.isSuccessful()) {
                    Utils.logv("Error creating&joining chat room: " + response.message());
                    return;
                }

                // The POST request is successful: go to room. API should have auto joined it
                Utils.logv("Success creating&joining chat room: " + response.body());
                currentChatRoom = response.body();

                manager.join(currentChatRoom);

                // When we show joined chat rooms open chat room directly
                if (mCurrentMode == ChatRoom.MODE_JOINED) {
                    moveToChatActivity();
                } else { //Otherwise show a nice information, that we added the room
                    final List<ChatRoomAndLastMessage> rooms = manager.getAllByStatus(mCurrentMode);

                    runOnUiThread(() -> {
                        chatRoomAdapter.updateRooms(rooms);
                        Utils.showToast(ChatRoomsActivity.this, R.string.joined_chat_room);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatRoom> call, @NonNull Throwable t) {
                Utils.log(t, "Failure creating/joining chat room - trying to GET it from the server");
                Utils.showToastOnUIThread(ChatRoomsActivity.this, R.string.activate_key);
            }
        };

        TUMCabeClient.getInstance(this)
                     .createRoom(currentChatRoom, verification, callback);
    }

    /**
     * Handle click on chat room
     */
    @Override
    public void onItemClick(AdapterView<?> a, View v, int position, long id) {
        ChatRoomAndLastMessage item = (ChatRoomAndLastMessage) lvMyChatRoomList.getItemAtPosition(position);

        // set bundle for LectureDetails and show it
        Bundle bundle = new Bundle();
        final Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtras(bundle);

        String chatRoomUid = item.getChatRoomDbRow()
                                 .getSemesterId() + ':' + item.getChatRoomDbRow()
                                                              .getName();
        this.createOrJoinChatRoom(chatRoomUid);
    }

    /**
     * Opens {@link ChatActivity}
     */
    private void moveToChatActivity() {
        // We are sure that both currentChatRoom and currentChatMember exist at this point
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Const.CURRENT_CHAT_ROOM, new Gson().toJson(currentChatRoom));
        startActivity(intent);
    }

}
