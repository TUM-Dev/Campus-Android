package de.tum.in.tumcampusapp.component.ui.chat.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import org.joda.time.format.DateTimeFormat;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeVerification;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.component.ui.chat.AddChatMemberActivity;
import de.tum.in.tumcampusapp.component.ui.chat.ChatMessageValidator;
import de.tum.in.tumcampusapp.component.ui.chat.ChatMessageViewModel;
import de.tum.in.tumcampusapp.component.ui.chat.ChatRoomController;
import de.tum.in.tumcampusapp.component.ui.chat.FcmChat;
import de.tum.in.tumcampusapp.component.ui.chat.adapter.ChatHistoryAdapter;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatPublicKey;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoom;
import de.tum.in.tumcampusapp.component.ui.chat.repository.ChatMessageLocalRepository;
import de.tum.in.tumcampusapp.component.ui.chat.repository.ChatMessageRemoteRepository;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.service.SendMessageService;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shows an ongoing chat conversation.
 * <p/>
 * NEEDS: Const.CURRENT_CHAT_ROOM set in incoming bundle (json serialised object of class ChatRoom)
 * Const.CURRENT_CHAT_MEMBER set in incoming bundle (json serialised object of class ChatMember)
 */
public class ChatActivity extends ActivityForDownloadingExternal
        implements AbsListView.OnScrollListener, AdapterView.OnItemLongClickListener {

    // Key for the string that's delivered in the action's intent
    //public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";

    public static ChatRoom mCurrentOpenChatRoom; // determines whether there will be a notification or not

    //private final Handler mUpdateHandler = new Handler();
    //private final CompositeDisposable mDisposable = new CompositeDisposable();
    private ChatMessageViewModel chatMessageViewModel;

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
            /*
            //Utils.logv("Message sent. Trying to parse...");
            FcmChat extras = (FcmChat) intent.getSerializableExtra("GCMChat");
            if (extras == null) {
                return;
            }

            Utils.log("Broadcast receiver got room=" + extras.getRoom() + " member=" + extras.getMember());
            handleRoomBroadcast(extras);
            */
        }
    };

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
        // TODO: Show Toast
        // TODO: Offer option to resend
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
                    vibrator.vibrate(500);
                }
            }
        }

        getNextHistoryFromServer(true);
    }

    //private ActionMode mActionMode;

    /*
    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.chat_context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            ChatMessage msg = chatHistoryAdapter.mCheckedItem;
            int i = item.getItemId();
            if (i == R.id.action_edit) {
                // If item is not sent at the moment, stop sending
                if (msg.getSendingStatus() == ChatMessage.STATUS_SENDING) {
                    chatMessageViewModel.removeUnsent(msg);
                } else { // set editing item
                    chatHistoryAdapter.mEditedItem = msg;
                }

                // Show soft keyboard
                InputMethodManager imm = (InputMethodManager) ChatActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(messageEditText, 0);

                messageEditText.setText(msg.getText());
                int position = msg.getText()
                                  .length();
                messageEditText.setSelection(position);
                mode.finish();
                return true;
            } else if (i == R.id.action_info) {
                showInfo(msg);
                mode.finish();
                return true;
            } else {
                return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            chatHistoryAdapter.mCheckedItem = null;
            chatHistoryAdapter.notifyDataSetChanged();
        }
    };
    */

    public ChatActivity() {
        super(Const.CURRENT_CHAT_ROOM, R.layout.activity_chat);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        setupToolbarTitle();
        initChatMessageViewModel();
        bindUIElements();

        /*RemoteInputRemoteInput
        CharSequence message = getMessageText(getIntent());
        if (message != null) {
            sendMessage(message.toString());
        }
        */
    }

    private void setupToolbarTitle() {
        currentChatRoom = new Gson().fromJson(getIntent().getStringExtra(Const.CURRENT_CHAT_ROOM), ChatRoom.class);
        currentChatMember = Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentChatRoom.getName());
        }
    }

    private void initChatMessageViewModel() {
        TcaDb tcaDb = TcaDb.getInstance(this);

        ChatMessageRemoteRepository remoteRepository = ChatMessageRemoteRepository.INSTANCE;
        remoteRepository.setTumCabeClient(TUMCabeClient.getInstance(this));

        ChatMessageLocalRepository localRepository = ChatMessageLocalRepository.INSTANCE;
        localRepository.setDb(tcaDb);

        chatMessageViewModel = new ChatMessageViewModel(
                localRepository, remoteRepository, new CompositeDisposable());
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

    @Override
    protected void onPause() {
        super.onPause();
        chatMessageViewModel.markAsRead(currentChatRoom.getId());
        mCurrentOpenChatRoom = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    /*
     * Gets the text from speech input and returns null if no input was provided
     */
    /*
    private static CharSequence getMessageText(Intent intent) {
        Bundle remoteInput;
        remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(EXTRA_VOICE_REPLY);
        }
        return null;
    }
    */

    //@TargetApi(android.os.Build.VERSION_CODES.O)
    /*
    private static void vibrate(Vibrator v) {
        //v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        v.vibrate(500);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        } else {
        }
    }
    */

    /**
     * Method to handle any incoming GCM/Firebase notifications
     *
     * @param extras model that contains infos about the message we should get
     */
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
                getSupportActionBar().setSubtitle(currentChatRoom.getName().substring(4));
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
        intent.putExtra(Const.CHAT_ROOM_NAME, currentChatRoom.getName());
        startActivity(intent);
    }

    private void showLeaveChatRoomDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.leave_chat_room)
                .setMessage(getResources().getString(R.string.leave_chat_room_body))
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> removeUserFromChatRoom())
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        dialog.show();
    }

    private void removeUserFromChatRoom() {
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
        if (chatHistoryAdapter.mEditedItem == null) {
            final ChatMessage message = new ChatMessage(text, currentChatMember);
            message.setRoom(currentChatRoom.getId());
            chatHistoryAdapter.add(message);
            chatMessageViewModel.addToUnsent(message);
        } else {
            chatHistoryAdapter.mEditedItem.setText(messageEditText.getText().toString());
            chatHistoryAdapter.mEditedItem.setRoom(currentChatRoom.getId());
            chatHistoryAdapter.mEditedItem.setSendingStatus(ChatMessage.STATUS_SENDING);
            chatHistoryAdapter.mEditedItem.setMember(currentChatMember);

            chatMessageViewModel.addToUnsent(chatHistoryAdapter.mEditedItem);
            chatHistoryAdapter.mEditedItem = null;
            chatMessageViewModel.markAsRead(currentChatRoom.getId());
            chatHistoryAdapter.updateHistory(chatMessageViewModel.getAll(currentChatRoom.getId()));
        }

        // Let the service handle the actual sending of the message
        SendMessageService.enqueueWork(this, new Intent());
    }

    /**
     * Sets UI elements listeners
     */
    private void bindUIElements() {
        messagesListView = findViewById(R.id.lvMessageHistory);
        messagesListView.setOnItemLongClickListener(this);
        messagesListView.setOnScrollListener(this);

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
    private void getNextHistoryFromServer(final boolean hasNewMessage) {
        isLoadingMore = true;
        new Thread(() -> {
            TUMCabeVerification verification = TUMCabeVerification.create(this, null);
            if (verification == null) {
                return;
            }

            // If currently nothing has been shown, load newest messages from server
            if (chatHistoryAdapter == null || chatHistoryAdapter.getCount() == 0 || hasNewMessage) {
                chatMessageViewModel.getNewMessages(currentChatRoom.getId(), verification, this::onMessagesLoaded);
            } else {
                long id = chatHistoryAdapter.getItemId(0);
                chatMessageViewModel.getOlderMessages(currentChatRoom.getId(), id, verification, this::onMessagesLoaded);
            }
        }).start();
    }

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

    /**
     * Validates chat message if long clicked on an item
     *
     * @param parent   ListView
     * @param view     View of the selected message
     * @param position Index of the selected view
     * @param id       Id of the selected item
     * @return True if the method consumed the on long click event
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        /*
        if (mActionMode != null) {
            return false;
        }
        */

        //Calculate the proper position of the item without the header from pull to refresh
        int positionActual = position - messagesListView.getHeaderViewsCount();

        //Get the correct message
        ChatMessage message = chatHistoryAdapter.getItem(positionActual);

        // TODO(jacqueline8711): If we are in a certain timespan and its the users own message allow editing
        /*if ((System.currentTimeMillis() - message.getTimestampDate()
                           .getTime()) < ChatActivity.MAX_EDIT_TIMESPAN && message.getMember()
                           .getId() == currentChatMember.getId()) {

            // Hide keyboard if opened
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);

            // Start the CAB using the ActionMode.Callback defined above
            mActionMode = this.startSupportActionMode(mActionModeCallback);
            chatHistoryAdapter.mCheckedItem = message;
            chatHistoryAdapter.notifyDataSetChanged();
        } else {
            this.showInfo(message);
        }*/
        showInfo(message);
        return true;
    }

    private void showInfo(final ChatMessage message) {
        //Verify the message with RSA
        TUMCabeClient.getInstance(ChatActivity.this)
                     .getPublicKeysForMember(message.getMember(), new Callback<List<ChatPublicKey>>() {
                         @Override
                         public void onResponse(@NonNull Call<List<ChatPublicKey>> call,
                                                @NonNull Response<List<ChatPublicKey>> response) {
                             List<ChatPublicKey> keys = response.body();
                             if (response.isSuccessful() && keys != null) {
                                 showMessageDetailsDialog(message, keys);
                             }
                         }

                         @Override
                         public void onFailure(@NonNull Call<List<ChatPublicKey>> call,
                                               @NonNull Throwable t) {
                             Utils.log(t, "Failure verifying message");
                         }
                     });
    }

    private void showMessageDetailsDialog(ChatMessage message, List<ChatPublicKey> keys) {
        ChatMessageValidator validator = new ChatMessageValidator(keys);
        final boolean result = validator.validate(message);

        // Show a nice dialog with more information about the message
        String messageStr = String.format(getString(R.string.message_detail_text),
                message.getMember().getDisplayName(),
                message.getMember().getLrzId(),
                DateTimeFormat.mediumDateTime().print(message.getDateTime()),
                getString(message.getStatusStringRes()),
                getString(result ? R.string.valid : R.string.not_valid));

        AlertDialog dialog = new AlertDialog.Builder(ChatActivity.this)
                .setTitle(R.string.message_details)
                .setMessage(Utils.fromHtml(messageStr))
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    R.drawable.rounded_corners_background);
        }

        dialog.show();
    }

    /*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUpdateHandler.removeCallbacksAndMessages(null);
    }
    */

}
