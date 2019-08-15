package de.tum.in.tumcampusapp.component.ui.chat.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.WorkManager;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeVerification;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.component.ui.chat.AddChatMemberActivity;
import de.tum.in.tumcampusapp.component.ui.chat.ChatMessageViewModel;
import de.tum.in.tumcampusapp.component.ui.chat.ChatRoomController;
import de.tum.in.tumcampusapp.component.ui.chat.FcmChat;
import de.tum.in.tumcampusapp.component.ui.chat.adapter.ChatHistoryAdapter;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoom;
import de.tum.in.tumcampusapp.component.ui.chat.repository.ChatMessageLocalRepository;
import de.tum.in.tumcampusapp.component.ui.chat.repository.ChatMessageRemoteRepository;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.service.DownloadWorker;
import de.tum.in.tumcampusapp.service.SendMessageWorker;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Build.VERSION.SDK_INT;

/**
 * Shows an ongoing chat conversation.
 * <p/>
 * NEEDS: Const.CURRENT_CHAT_ROOM set in incoming bundle (json serialised object of class ChatRoom)
 * Const.CURRENT_CHAT_MEMBER set in incoming bundle (json serialised object of class ChatMember)
 */
public class ChatActivity extends ActivityForDownloadingExternal
        implements AbsListView.OnScrollListener, ChatHistoryAdapter.OnRetrySendListener {

    public static ChatRoom mCurrentOpenChatRoom; // determines whether there will be a notification or not

    private ChatMessageViewModel chatMessageViewModel;
    private CompositeDisposable disposables = new CompositeDisposable();

    private ListView messagesListView;
    private ChatHistoryAdapter chatHistoryAdapter;
    private EditText messageEditText;
    private ProgressBar progressbar;

    private ChatRoom currentChatRoom;
    private ChatMember currentChatMember;
    private boolean isLoadingMore;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleBroadcastReceive(intent);
        }
    };

    public ChatActivity() {
        super(R.layout.activity_chat);
        // TODO: Const.CURRENT_CHAT_ROOM was previously non-existent
    }

    @Nullable
    @Override
    public DownloadWorker.Action getMethod() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupToolbarTitle();
        initChatMessageViewModel();
        bindUIElements();
    }

    private void setupToolbarTitle() {
        String encodedChatRoom = getIntent().getStringExtra(Const.CURRENT_CHAT_ROOM);
        currentChatRoom = new Gson().fromJson(encodedChatRoom, ChatRoom.class);
        currentChatMember = Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentChatRoom.getTitle());
        }
    }

    private void initChatMessageViewModel() {
        TcaDb tcaDb = TcaDb.Companion.getInstance(this);

        ChatMessageRemoteRepository remoteRepository = ChatMessageRemoteRepository.INSTANCE;
        remoteRepository.setTumCabeClient(TUMCabeClient.getInstance(this));

        ChatMessageLocalRepository localRepository = ChatMessageLocalRepository.INSTANCE;
        localRepository.setDb(tcaDb);

        chatMessageViewModel = new ChatMessageViewModel(localRepository, remoteRepository);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getNextHistoryFromServer(true);
        mCurrentOpenChatRoom = currentChatRoom;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel((currentChatRoom.getId() << 4) + CardManager.CARD_CHAT);
        }

        IntentFilter filter = new IntentFilter(Const.CHAT_BROADCAST_NAME);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private void handleBroadcastReceive(Intent intent) {
        Utils.logv("Message sent. Trying to parse...");

        FcmChat chat = (FcmChat) intent.getSerializableExtra(Const.FCM_CHAT);
        if (chat != null) {
            handleSuccessBroadcast(chat);
        } else {
            handleFailureBroadcast();
        }
    }

    private void handleFailureBroadcast() {
        Utils.showToast(this, R.string.chat_message_send_error);
        getNextHistoryFromServer(true);
    }

    private void handleSuccessBroadcast(FcmChat chat) {
        if (chat.getRoom() != currentChatRoom.getId() || chatHistoryAdapter == null) {
            return;
        }

        if (chat.getMember() != currentChatMember.getId() && chat.getMessage() == -1) {
            // This is a new message from a different user
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am != null && am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                // Play a notification sound
                MediaPlayer mediaPlayer = MediaPlayer.create(ChatActivity.this, R.raw.message);
                mediaPlayer.start();
            } else if (am != null && am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                // Possibly only vibration is enabled
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    vibrate(vibrator);
                }
            }
        }

        getNextHistoryFromServer(true);
    }

    @SuppressWarnings("deprecation")
    private void vibrate(@NonNull Vibrator vibrator) {
        if (SDK_INT >= VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, 128));
        } else {
            vibrator.vibrate(500);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatMessageViewModel.markAsRead(currentChatRoom.getId());
        mCurrentOpenChatRoom = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    /*
     * Method to handle any incoming GCM/Firebase notifications
     *
     * @param extras model that contains infos about the message we should get
     */
    /*
    private void handleRoomBroadcast(FcmChat extras) {
        if (extras.getRoom() != currentChatRoom.getId() || chatHistoryAdapter == null) {
            return;
        }

        if (extras.getMember() != currentChatMember.getId() && extras.getMessage() == -1) {
            // This is a new message from a different user
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am != null && am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                // Play a notification sound
                MediaPlayer mediaPlayer = MediaPlayer.create(ChatActivity.this, R.raw.message);
                mediaPlayer.start();
            } else if (am != null && am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                // Possibly only vibration is enabled
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    vibrator.vibrate(500);
                }
            }
        }

        getNextHistoryFromServer(true);
    }
    */

    /**
     * User pressed on the notification and wants to view the room with the new messages
     *
     * @param intent Intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Try to get the room from the extras
        ChatRoom room = null;
        if (intent.getExtras() != null) {
            String value = intent.getExtras().getString(Const.CURRENT_CHAT_ROOM);
            room = new Gson().fromJson(value, ChatRoom.class);
        }

        // Check, maybe it wasn't there
        if (room != null && room.getId() != currentChatRoom.getId()) {
            // If currently in a room which does not match the one from the notification --> Switch
            currentChatRoom = room;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(currentChatRoom.getTitle());
            }
            chatHistoryAdapter = null;
            getNextHistoryFromServer(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_activity_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_add_chat_member:
                openAddChatMemberActivity();
                return true;
            case R.id.action_leave_chat_room:
                showLeaveChatRoomDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openAddChatMemberActivity() {
        Intent intent = new Intent(this, AddChatMemberActivity.class);
        intent.putExtra(Const.CURRENT_CHAT_ROOM, currentChatRoom.getId());
        intent.putExtra(Const.CHAT_ROOM_NAME, currentChatRoom.getCombinedName());
        startActivity(intent);
    }

    private void showLeaveChatRoomDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.leave_chat_room)
                .setMessage(getResources().getString(R.string.leave_chat_room_body))
                .setPositiveButton(R.string.leave, (dialogInterface, i) -> leaveChatRoom())
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        dialog.show();
    }

    private void leaveChatRoom() {
        TUMCabeVerification verification = TUMCabeVerification.create(this, null);
        if (verification == null) {
            return;
        }

        TUMCabeClient.getInstance(this)
                .leaveChatRoom(currentChatRoom, verification, new Callback<ChatRoom>() {
                    @Override
                    public void onResponse(@NonNull Call<ChatRoom> call,
                                           @NonNull Response<ChatRoom> response) {
                        ChatRoom room = response.body();
                        if (response.isSuccessful() && room != null) {
                            new ChatRoomController(ChatActivity.this).leave(currentChatRoom);

                            Intent intent = new Intent(ChatActivity.this, ChatRoomsActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Utils.showToast(ChatActivity.this, R.string.error_something_wrong);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ChatRoom> call, @NonNull Throwable t) {
                        Utils.log(t, "Failure leaving chat room");
                        Utils.showToast(ChatActivity.this, R.string.error_something_wrong);
                    }
                });
    }

    private void sendMessage(String text) {
        if (currentChatMember == null) {
            Utils.showToast(this, R.string.chat_message_send_error);
            return;
        }

        final ChatMessage message = new ChatMessage(text, currentChatMember);
        message.setRoom(currentChatRoom.getId());
        message.setSendingStatus(ChatMessage.STATUS_SENDING);
        chatHistoryAdapter.add(message);
        chatMessageViewModel.addToUnsent(message);

        WorkManager.getInstance()
                .enqueue(SendMessageWorker.getWorkRequest());
    }

    @Override
    public void onRetrySending(ChatMessage message) {
        //chatMessageViewModel.removeUnsent(message);
        message.setSendingStatus(ChatMessage.STATUS_SENDING);
        sendMessage(message.getText());

        List<ChatMessage> messages = chatMessageViewModel.getAll(currentChatRoom.getId());
        chatHistoryAdapter.updateHistory(messages);
    }

    /**
     * Sets UI elements listeners
     */
    private void bindUIElements() {
        messagesListView = findViewById(R.id.lvMessageHistory);
        messagesListView.setOnScrollListener(this);

        chatHistoryAdapter = new ChatHistoryAdapter(this, currentChatMember);
        messagesListView.setAdapter(chatHistoryAdapter);

        // Add the button for loading more messages to list header
        progressbar = new ProgressBar(this);
        messagesListView.addHeaderView(progressbar);
        messagesListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        messageEditText = findViewById(R.id.etMessage);

        ImageButton sendButton = findViewById(R.id.btnSend);
        sendButton.setOnClickListener(view -> {
            if (messageEditText.getText().toString().isEmpty()) {
                return;
            }

            sendMessage(messageEditText.getText().toString());
            messageEditText.getText().clear();
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Noop
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // If the top item is visible and not loading more already, then load more
        if (firstVisibleItem == 0 && !isLoadingMore && chatHistoryAdapter != null) {
            getNextHistoryFromServer(false);
        }
    }

    /**
     * Loads older chat messages from the server and sets the adapter accordingly
     */
    private void getNextHistoryFromServer(boolean hasNewMessage) {
        isLoadingMore = true;

        TUMCabeVerification verification = TUMCabeVerification.create(this, null);
        if (verification == null) {
            return;
        }

        Observable<List<ChatMessage>> observable;

        if (hasNewMessage || chatHistoryAdapter.isEmpty()) {
            observable = chatMessageViewModel.getNewMessages(currentChatRoom, verification);
            //chatMessageViewModel.getNewMessages(currentChatRoom.getId(), verification, this::onMessagesLoaded);
        } else {
            ChatMessage latestMessage = chatHistoryAdapter.getItem(0);
            long latestId = latestMessage.getId();
            observable = chatMessageViewModel.getOlderMessages(currentChatRoom, latestId, verification);
            //chatMessageViewModel.getOlderMessages(currentChatRoom.getId(), latestId, verification, this::onMessagesLoaded);
        }

        disposables.add(observable.subscribe(this::showMessages, Utils::log));

        /*
        new Thread(() -> {
            // If currently nothing has been shown, load newest messages from server
            if (chatHistoryAdapter == null || chatHistoryAdapter.getCount() == 0 || hasNewMessage) {
                chatMessageViewModel.getNewMessages(currentChatRoom.getId(), verification, this::onMessagesLoaded);
            } else {
                long id = chatHistoryAdapter.getItemId(0);
                chatMessageViewModel.getOlderMessages(currentChatRoom.getId(), id, verification, this::onMessagesLoaded);
            }
        }).start();
        */
    }

    private void showMessages(List<ChatMessage> messages) {
        List<ChatMessage> unsent = chatMessageViewModel.getUnsentInChatRoom(currentChatRoom);
        messages.addAll(unsent);

        Collections.sort(messages, (lhs, rhs) -> lhs.getTimestamp().compareTo(rhs.getTimestamp()));
        chatHistoryAdapter.updateHistory(messages);

        if (messages.isEmpty()) {
            messagesListView.removeHeaderView(progressbar);
            return;
        }

        // We remove the progress indicator in the header view if all messages are loaded
        ChatMessage firstMessage = messages.get(0);
        if (firstMessage.getPrevious() == 0 || chatHistoryAdapter.isEmpty()) {
            messagesListView.removeHeaderView(progressbar);
        } else {
            isLoadingMore = false;
        }
    }

    /*
    private void onMessagesLoaded() {
        final List<ChatMessage> messages = chatMessageViewModel.getAll(currentChatRoom.getId());

        // Update results in UI
        runOnUiThread(() -> {
            if (chatHistoryAdapter == null) {
                chatHistoryAdapter = new ChatHistoryAdapter(ChatActivity.this, messages, currentChatMember);
                messagesListView.setAdapter(chatHistoryAdapter);
            } else {
                chatHistoryAdapter.updateHistory(chatMessageViewModel.getAll(currentChatRoom.getId()));
            }

            // If all messages are loaded hide header view
            if ((!messages.isEmpty() && messages.get(0)
                                        .getPrevious() == 0) || chatHistoryAdapter.getCount() == 0) {
                messagesListView.removeHeaderView(progressbar);
            } else {
                isLoadingMore = false;
            }
        });
    }
    */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }
}
