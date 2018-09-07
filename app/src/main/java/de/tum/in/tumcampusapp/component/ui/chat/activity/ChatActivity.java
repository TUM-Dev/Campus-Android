package de.tum.in.tumcampusapp.component.ui.chat.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
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
import de.tum.in.tumcampusapp.api.app.exception.NoPrivateKey;
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
public class ChatActivity extends ActivityForDownloadingExternal implements DialogInterface.OnClickListener, OnClickListener, AbsListView.OnScrollListener, AdapterView.OnItemLongClickListener {

    // Key for the string that's delivered in the action's intent
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    //private static final int MAX_EDIT_TIMESPAN = 120000;

    public static ChatRoom mCurrentOpenChatRoom; // determines whether there will be a notification or not
    private final Handler mUpdateHandler = new Handler();
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private ChatMessageViewModel chatMessageViewModel;
    /**
     * UI elements.
     */
    private ListView lvMessageHistory;
    private ChatHistoryAdapter chatHistoryAdapter;
    private EditText etMessage;
    private ImageButton btnSend;
    private ProgressBar bar;
    private ChatRoom currentChatRoom;
    private ChatMember currentChatMember;
    private boolean loadingMore;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Utils.logv("Message send. Trying to parse...");
            FcmChat extras = (FcmChat) intent.getSerializableExtra("GCMChat");
            if (extras == null) {
                return;
            }
            Utils.log("Broadcast receiver got room=" + extras.getRoom() + " member=" + extras.getMember());
            handleRoomBroadcast(extras);
        }
    };
    private ActionMode mActionMode;
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
                imm.showSoftInput(etMessage, 0);

                etMessage.setText(msg.getText());
                int position = msg.getText()
                                  .length();
                etMessage.setSelection(position);
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

    public ChatActivity() {
        super(Const.CURRENT_CHAT_ROOM, R.layout.activity_chat);
    }

    /**
     * Gets the text from speech input and returns null if no input was provided
     */
    private static CharSequence getMessageText(Intent intent) {
        Bundle remoteInput;
        remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(EXTRA_VOICE_REPLY);
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(android.os.Build.VERSION_CODES.O)
    private static void vibrate(Vibrator v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(500);
        }
    }

    /**
     * Method to handle any incoming GCM/Firebase notifications
     *
     * @param extras model that contains infos about the message we should get
     */
    private void handleRoomBroadcast(FcmChat extras) {

        if (extras.getRoom() != currentChatRoom.getId() || chatHistoryAdapter == null) {
            return;
        }

        if (extras.getMember() != currentChatMember.getId()
            && extras.getMessage() == -1) { // this is a new message from a different user

            //Check first, if sounds are enabled
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {

                //Play a nice notification sound
                MediaPlayer mediaPlayer = MediaPlayer.create(ChatActivity.this, R.raw.message);
                mediaPlayer.start();
            } else if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) { //Possibly only vibration is enabled
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrate(v);
            }
        }

        //Update the history
        getNextHistoryFromServer(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        this.getIntentData();
        TcaDb tcaDb = TcaDb.getInstance(this);
        ChatMessageRemoteRepository remoteRepository = ChatMessageRemoteRepository.INSTANCE;
        remoteRepository.setTumCabeClient(TUMCabeClient.getInstance(this));
        ChatMessageLocalRepository localRepository = ChatMessageLocalRepository.INSTANCE;
        localRepository.setDb(tcaDb);
        chatMessageViewModel = new ChatMessageViewModel(localRepository, remoteRepository, mDisposable);
        this.bindUIElements();
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatMessageViewModel.markAsRead(currentChatRoom.getId());
        mCurrentOpenChatRoom = null;
        LocalBroadcastManager.getInstance(this)
                             .unregisterReceiver(receiver);
    }

    /**
     * User pressed on the notification and wants to view the room with the new messages
     *
     * @param intent Intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //Try to get the room from the extras
        ChatRoom room = null;
        if (intent.getExtras() != null) {
            room = new Gson().fromJson(intent.getExtras()
                                             .getString(Const.CURRENT_CHAT_ROOM), ChatRoom.class);
        }

        //Check, maybe it wasn't there
        if (room != null && room.getId() != currentChatRoom.getId()) {
            //If currently in a room which does not match the one from the notification --> Switch
            currentChatRoom = room;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(currentChatRoom.getName()
                                                                 .substring(4));
            }
            chatHistoryAdapter = null;
            getNextHistoryFromServer(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getNextHistoryFromServer(true);
        mCurrentOpenChatRoom = currentChatRoom;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel((currentChatRoom.getId() << 4) + CardManager.CARD_CHAT);
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(receiver, new IntentFilter(Const.CHAT_BROADCAST_NAME));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_activity_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_add_chat_member) {
            Intent intent = new Intent(this, AddChatMemberActivity.class);
            intent.putExtra(Const.CURRENT_CHAT_ROOM, currentChatRoom.getId());
            intent.putExtra(Const.CHAT_ROOM_NAME, currentChatRoom.getName());
            startActivity(intent);
            return true;
        } else if (i == R.id.action_leave_chat_room) {
            new AlertDialog.Builder(this).setTitle(R.string.leave_chat_room)
                                         .setMessage(getResources().getString(R.string.leave_chat_room_body))
                                         .setPositiveButton(android.R.string.ok, this)
                                         .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                                         .create()
                                         .show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void sendMessage(String text) {

        if (chatHistoryAdapter.mEditedItem == null) {
            final ChatMessage message = new ChatMessage(text, currentChatMember);
            message.setRoom(currentChatRoom.getId());
            chatHistoryAdapter.add(message);
            chatMessageViewModel.addToUnsent(message);
        } else {
            chatHistoryAdapter.mEditedItem.setText(etMessage.getText()
                                                            .toString());
            chatHistoryAdapter.mEditedItem.setRoom(currentChatRoom.getId());
            chatHistoryAdapter.mEditedItem.setSendingStatus(ChatMessage.STATUS_SENDING);
            chatHistoryAdapter.mEditedItem.setMember(currentChatMember);
            chatMessageViewModel.addToUnsent(chatHistoryAdapter.mEditedItem);
            chatHistoryAdapter.mEditedItem = null;
            chatMessageViewModel.markAsRead(currentChatRoom.getId());
            chatHistoryAdapter.updateHistory(chatMessageViewModel.getAll(currentChatRoom.getId()));
        }

        // let the service handle the actual sending of the message
        SendMessageService.enqueueWork(this, new Intent());
    }

    /**
     * Sets the actionbar title to the current chat room.
     */
    private void getIntentData() {
        Bundle extras = getIntent().getExtras();
        currentChatRoom = new Gson().fromJson(extras.getString(Const.CURRENT_CHAT_ROOM), ChatRoom.class);
        currentChatMember = Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentChatRoom.getName()
                                                          .substring(4));
        }

        CharSequence message = getMessageText(getIntent());
        if (message != null) {
            sendMessage(message.toString());
        }
    }

    /**
     * Sets UI elements listeners
     */
    private void bindUIElements() {
        lvMessageHistory = findViewById(R.id.lvMessageHistory);
        lvMessageHistory.setOnItemLongClickListener(this);
        lvMessageHistory.setOnScrollListener(this);

        // Add the button for loading more messages to list header
        bar = new ProgressBar(this);
        lvMessageHistory.addHeaderView(bar);
        lvMessageHistory.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Noop
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //is the top item is visible & not loading more already ? Load more !
        if (firstVisibleItem == 0 && !loadingMore && chatHistoryAdapter != null) {
            getNextHistoryFromServer(false);
        }
    }

    /**
     * Loads older chat messages from the server and sets the adapter accordingly
     */
    private void getNextHistoryFromServer(final boolean newMsg) {
        loadingMore = true;
        new Thread(() -> {
            // Download chat messages in new Thread

            // If currently nothing has been shown load newest messages from server
            TUMCabeVerification verification = TUMCabeVerification.createMemberVerification(this, null);
            if (verification == null) {
                return; //In this case we simply cannot do anything
            }

            if (chatHistoryAdapter == null || chatHistoryAdapter.getCount() == 0 || newMsg) {
                chatMessageViewModel.getNewMessages(currentChatRoom.getId(), verification, this::onMessagesLoaded);
            } else {
                long id = chatHistoryAdapter.getItemId(0);
                chatMessageViewModel.getOlderMessages(currentChatRoom.getId(), id, verification, this::onMessagesLoaded);
            }
        }).start();
    }

    private void onMessagesLoaded() {
        final List<ChatMessage> msgs = chatMessageViewModel.getAll(currentChatRoom.getId());
        // Update results in UI
        runOnUiThread(() -> {
            if (chatHistoryAdapter == null) {
                chatHistoryAdapter = new ChatHistoryAdapter(ChatActivity.this, msgs, currentChatMember);
                lvMessageHistory.setAdapter(chatHistoryAdapter);
            } else {
                chatHistoryAdapter.updateHistory(chatMessageViewModel.getAll(currentChatRoom.getId()));
            }

            // If all messages are loaded hide header view
            if ((!msgs.isEmpty() && msgs.get(0)
                                        .getPrevious() == 0) || chatHistoryAdapter.getCount() == 0) {
                lvMessageHistory.removeHeaderView(bar);
            } else {
                loadingMore = false;
            }
        });
    }

    /**
     * When user confirms the leave dialog send the request to the server.
     *
     * @param dialog Dialog handle
     * @param which  The users choice (ignored because this is only called when the user confirms)
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        // Send request to the server to remove the user from this room
        TUMCabeVerification verification = TUMCabeVerification.createMemberVerification(this, null);
        if (verification == null) {
            return;
        }

        TUMCabeClient.getInstance(this)
                     .leaveChatRoom(currentChatRoom, verification, new Callback<ChatRoom>() {
                         @Override
                         public void onResponse(Call<ChatRoom> call, Response<ChatRoom> room) {
                             Utils.logv("Success leaving chat room: " + room.body()
                                                                            .getName());
                             new ChatRoomController(ChatActivity.this).leave(currentChatRoom);

                             // Move back to ChatRoomsActivity
                             Intent intent = new Intent(ChatActivity.this, ChatRoomsActivity.class);
                             startActivity(intent);
                         }

                         @Override
                         public void onFailure(Call<ChatRoom> call, Throwable t) {
                             Utils.log(t, "Failure leaving chat room");
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
        if (mActionMode != null) {
            return false;
        }
        //Calculate the proper position of the item without the header from pull to refresh
        int positionActual = position - lvMessageHistory.getHeaderViewsCount();

        //Get the correct message
        ChatMessage message = chatHistoryAdapter.getItem(positionActual);

        // TODO(jacqueline8711): If we are in a certain timespan and its the users own message allow editing
        /*if ((System.currentTimeMillis() - message.getTimestampDate()
                           .getTime()) < ChatActivity.MAX_EDIT_TIMESPAN && message.getMember()
                           .getId() == currentChatMember.getId()) {

            // Hide keyboard if opened
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);

            // Start the CAB using the ActionMode.Callback defined above
            mActionMode = this.startSupportActionMode(mActionModeCallback);
            chatHistoryAdapter.mCheckedItem = message;
            chatHistoryAdapter.notifyDataSetChanged();
        } else {
            this.showInfo(message);
        }*/
        this.showInfo(message);
        return true;
    }

    private void showInfo(final ChatMessage message) {
        //Verify the message with RSA
        TUMCabeClient.getInstance(ChatActivity.this)
                     .getPublicKeysForMember(message.getMember(), new Callback<List<ChatPublicKey>>() {
                         @Override
                         public void onResponse(Call<List<ChatPublicKey>> call, Response<List<ChatPublicKey>> response) {
                             ChatMessageValidator validator = new ChatMessageValidator(response.body());
                             final boolean result = validator.validate(message);

                             //Show a nice dialog with more information about the message
                             String messageStr = String.format(getString(R.string.message_detail_text),
                                                               message.getMember()
                                                                      .getDisplayName(),
                                                               message.getMember()
                                                                      .getLrzId(),
                                                               DateTimeFormat.mediumDateTime()
                                                                             .print(message.getTimestamp()),
                                                               getString(message.getStatusStringRes()),
                                                               getString(result ? R.string.valid : R.string.not_valid));

                             new AlertDialog.Builder(ChatActivity.this)
                                     .setTitle(R.string.message_details)
                                     .setMessage(Utils.fromHtml(messageStr))
                                     .create()
                                     .show();
                         }

                         @Override
                         public void onFailure(Call<List<ChatPublicKey>> call, Throwable t) {
                             Utils.log(t, "Failure verifying message");
                         }

                     });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUpdateHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Handles clicks on send and load messages buttons
     *
     * @param view Handle of the button
     */
    @Override
    public void onClick(View view) {
        // Create / send message
        if (view.getId() == btnSend.getId()) {

            //Check if something was entered
            if (etMessage.getText()
                         .toString()
                         .isEmpty()) {
                return;
            }

            this.sendMessage(etMessage.getText()
                                      .toString());

            //Set TextField to empty, when done
            etMessage.setText("");
        }
    }
}
