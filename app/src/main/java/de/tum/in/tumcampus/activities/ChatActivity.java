package de.tum.in.tumcampus.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.text.Html;
import android.view.KeyEvent;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.adapters.ChatHistoryAdapter;
import de.tum.in.tumcampus.auxiliary.ChatMessageValidator;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatClient;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatMessage;
import de.tum.in.tumcampus.models.ChatPublicKey;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.ChatVerification;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.ChatMessageManager;
import de.tum.in.tumcampus.models.managers.ChatRoomManager;
import de.tum.in.tumcampus.services.SendMessageService;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Shows an ongoing chat conversation
 * <p/>
 * NEEDS: Const.CURRENT_CHAT_ROOM set in incoming bundle (json serialised object of class ChatRoom)
 * Const.CURRENT_CHAT_MEMBER set in incoming bundle (json serialised object of class ChatMember)
 */
public class ChatActivity extends ActionBarActivity implements DialogInterface.OnClickListener, OnClickListener, AbsListView.OnScrollListener,
        EmojiconGridView.OnEmojiconClickedListener, EmojiconsPopup.OnSoftKeyboardOpenCloseListener,
        EmojiconsPopup.OnEmojiconBackspaceClickedListener, AdapterView.OnItemLongClickListener {

    // Key for the string that's delivered in the action's intent
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";

    /**
     * UI elements
     */
    private ListView lvMessageHistory;
    private ChatHistoryAdapter chatHistoryAdapter = null;
    private EditText etMessage;
    private ImageButton btnSend;
    private ProgressBar bar;
    private ImageButton btnEmotion;

    private ChatRoom currentChatRoom;
    private ChatMember currentChatMember;

    private boolean loadingMore = false;
    private EmojiconsPopup popup;
    private boolean iconShow = false;
    private ChatMessageManager chatManager;
    public static ChatRoom mCurrentOpenChatRoom = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);
        this.setContentView(R.layout.activity_chat);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        this.getIntentData();
        this.bindUIElements();

        // Update the times shown in the list every 10 seconds
        mUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(chatHistoryAdapter!=null)
                    chatHistoryAdapter.notifyDataSetChanged();
                mUpdateHandler.postDelayed(this, 10000);
            }
        }, 10000);

    }

    private Handler mUpdateHandler = new Handler();

    @Override
    protected void onResume() {
        super.onResume();
        getNextHistoryFromServer();
        mCurrentOpenChatRoom = currentChatRoom;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(currentChatRoom.getId()<<4+ CardManager.CARD_CHAT);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("chat-message-received"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCurrentOpenChatRoom = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
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
        final ChatRoom room = new Gson().fromJson(intent.getExtras().getString(Const.CURRENT_CHAT_ROOM), ChatRoom.class);

        //Check, maybe it wasn't there
        if (room != null) {
            //If currently in a room which does not match the one from the notification --> Switch
            if (room.getId() != currentChatRoom.getId()) {
                currentChatRoom = room;
                getSupportActionBar().setSubtitle(currentChatRoom.getName().substring(4));
                chatHistoryAdapter = null;
                chatManager = new ChatMessageManager(this, currentChatRoom.getId());
                getNextHistoryFromServer();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_activity_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_leave_chat_room:
                new AlertDialog.Builder(ChatActivity.this).setTitle(R.string.leave_chat_room)
                        .setMessage(getResources().getString(R.string.leave_chat_room_body))
                        .setPositiveButton(getResources().getString(android.R.string.ok), this)
                        .setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            if (etMessage.getText().toString().length() == 0) {
                return;
            }

            this.sendMessage(etMessage.getText().toString());

            //Set TextField to empty, when done
            etMessage.setText("");
        } else if (view.getId() == btnEmotion.getId()) { // Show/hide emoticons
            if (!iconShow && popup.isKeyBoardOpen()) {
                popup.showAtBottom();
                btnEmotion.setImageResource(R.drawable.ic_keyboard);
            } else if (!iconShow) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT);
            } else {
                popup.dismiss();
                btnEmotion.setImageResource(R.drawable.ic_emoticon);
            }
            iconShow = !iconShow;
        }
    }

    private void sendMessage(String text) {
        if (mEditedItem == null) {
            final ChatMessage message = new ChatMessage(text, currentChatMember);
            chatHistoryAdapter.add(message);
            chatManager.addToUnsent(message);
        } else {
            mEditedItem.setText(etMessage.getText().toString());
            chatManager.addToUnsent(mEditedItem);
            mEditedItem.setStatus(ChatMessage.STATUS_SENDING);
            chatManager.replaceMessage(mEditedItem);
            chatHistoryAdapter.changeCursor(chatManager.getAll());
            mEditedItem = null;
        }

        // start service to send the message
        startService(new Intent(this, SendMessageService.class));
    }

    /**
     * Sets the actionbar title to the current chat room
     */
    private void getIntentData() {
        Bundle extras = getIntent().getExtras();
        currentChatRoom = new Gson().fromJson(extras.getString(Const.CURRENT_CHAT_ROOM), ChatRoom.class);
        currentChatMember = new Gson().fromJson(extras.getString(Const.CURRENT_CHAT_MEMBER), ChatMember.class);
        getSupportActionBar().setTitle(currentChatRoom.getName().substring(4));
        chatManager = new ChatMessageManager(this, currentChatRoom.getId());

        CharSequence message = getMessageText(getIntent());
        if (message != null) {
            sendMessage(message.toString());
        }
    }

    /**
     * Sets UI elements listeners
     */
    private void bindUIElements() {
        lvMessageHistory = (ListView) findViewById(R.id.lvMessageHistory);
        lvMessageHistory.setOnItemLongClickListener(this);
        lvMessageHistory.setDividerHeight(0);
        lvMessageHistory.setOnScrollListener(this);

        // Add the button for loading more messages to list header
        bar = new ProgressBar(this);
        lvMessageHistory.addHeaderView(bar);
        lvMessageHistory.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        etMessage = (EditText) findViewById(R.id.etMessage);
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        btnEmotion = (ImageButton) findViewById(R.id.btnEmoji);
        btnEmotion.setOnClickListener(this);

        // Give the topmost view of your activity layout hierarchy. This will be used to measure soft keyboard height
        popup = new EmojiconsPopup(findViewById(R.id.chat_layout), this);

        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard();
        popup.setOnEmojiconClickedListener(this);
        popup.setOnEmojiconBackspaceClickedListener(this);
        popup.setOnSoftKeyboardOpenCloseListener(this);
    }


    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        etMessage.append(emojicon.getEmoji());
    }

    @Override
    public void onKeyboardOpen(int keyBoardHeight) {
        if (iconShow && !popup.isShowing()) {
            popup.showAtBottom();
            btnEmotion.setImageResource(R.drawable.ic_keyboard);
        }
    }

    @Override
    public void onKeyboardClose() {
        if (popup.isShowing()) {
            popup.dismiss();
            iconShow = false;
            btnEmotion.setImageResource(R.drawable.ic_emoticon);
        }
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        etMessage.dispatchKeyEvent(event);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //is the top item is visible & not loading more already ? Load more !
        if ((firstVisibleItem == 0) && !loadingMore && chatHistoryAdapter != null) {
            getNextHistoryFromServer();
        }
    }

    /**
     * Loads older chat messages from the server and sets the adapter accordingly
     */
    private void getNextHistoryFromServer() {
        loadingMore = true;
        new Thread(new Runnable() {
            @Override
            public void run() {

                // Download chat messages in new Thread
                ArrayList<ChatMessage> downloadedChatHistory;

                // If currently nothing has been shown load newest messages from server
                ChatVerification verification = new ChatVerification(Utils.getPrivateKeyFromSharedPrefs(ChatActivity.this), currentChatMember);
                if (chatHistoryAdapter == null || chatHistoryAdapter.getSentCount() == 0) {
                    downloadedChatHistory = ChatClient.getInstance(ChatActivity.this).getNewMessages(currentChatRoom.getId(), verification);
                } else {
                    long id = chatHistoryAdapter.getItemId(ChatMessageManager.COL_ID);
                    downloadedChatHistory = ChatClient.getInstance(ChatActivity.this).getMessages(currentChatRoom.getId(), id, verification);
                }

                //Save it to our local cache
                chatManager.replaceInto(downloadedChatHistory);

                // Got results from webservice
                Utils.logv("Success loading additional chat history: " + downloadedChatHistory.size());

                final Cursor cur = chatManager.getAll();

                // Update results in UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (chatHistoryAdapter == null) {
                            chatHistoryAdapter = new ChatHistoryAdapter(ChatActivity.this, cur, currentChatMember);
                            lvMessageHistory.setAdapter(chatHistoryAdapter);
                        } else if (!loadingMore) {
                            chatHistoryAdapter.changeCursor(cur);
                            chatHistoryAdapter.notifyDataSetChanged();
                        }

                        // If all messages are loaded hide header view
                        if ((cur.moveToFirst() && cur.getLong(ChatMessageManager.COL_PREVIOUS) == 0) || cur.getCount() == 0) {
                            lvMessageHistory.removeHeaderView(bar);
                        } else {
                            loadingMore = false;
                        }
                    }
                });
            }
        }).start();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String chatRoomString = extras.getString("room");
            String memberString = extras.getString("member");

            Utils.log("Broadcast receiver got room="+chatRoomString+" member="+memberString);

            //If same room just refresh
            if (chatRoomString.equals(""+currentChatRoom.getId()) && chatHistoryAdapter!=null) {
                if (memberString.equals(""+currentChatMember.getId())) {
                    // Remove this message from the adapter
                    chatHistoryAdapter.setUnsentMessages(chatManager.getAllUnsent());
                } else {
                    //Check first, if sounds are enabled
                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {

                        //Play a nice notification sound
                        MediaPlayer mediaPlayer = MediaPlayer.create(ChatActivity.this, R.raw.message);
                        mediaPlayer.start();
                    } else if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) { //Possibly only vibration is enabled
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(500);
                    }
                }

                //Update the history
                chatHistoryAdapter.changeCursor(chatManager.getAll());
            }
        }
    };

    /**
     * When user confirms the leave dialog send the request to the server
     *
     * @param dialog Dialog handle
     * @param which  The users choice (ignored because this is only called when the user confirms)
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {

        // Send request to the server to remove the user from this room
        ChatVerification verification = new ChatVerification(Utils.getPrivateKeyFromSharedPrefs(this), currentChatMember);
        ChatClient.getInstance(ChatActivity.this).leaveChatRoom(currentChatRoom, verification, new Callback<ChatRoom>() {
            @Override
            public void success(ChatRoom room, Response arg1) {
                Utils.logv("Success leaving chat room: " + room.getName());
                new ChatRoomManager(ChatActivity.this).leave(currentChatRoom);

                // Move back to ChatRoomsActivity
                Intent intent = new Intent(ChatActivity.this, ChatRoomsActivity.class);
                startActivity(intent);
            }

            @Override
            public void failure(RetrofitError e) {
                Utils.log(e, "Failure leaving chat room");
            }
        });
    }

    /**
     * Gets the text from speech input and returns null if no input was provided
     */
    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(EXTRA_VOICE_REPLY);
        }
        return null;
    }

    private ActionMode mActionMode = null;

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
        ChatMessage message = (ChatMessage) chatHistoryAdapter.getItem(position);

        if((System.currentTimeMillis()-message.getTimestampDate().getTime())<120000 &&
                message.getMember().getId() == currentChatMember.getId()) {
            // Hide keyboard if opened
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);

            // Start the CAB using the ActionMode.Callback defined above
            mActionMode = startSupportActionMode(mActionModeCallback);
            chatHistoryAdapter.mCheckedItem = message;
            chatHistoryAdapter.notifyDataSetChanged();
        } else {
            showInfo(message);
        }
        return true;
    }


    private ChatMessage mEditedItem = null;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.chat_context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            ChatMessage msg = chatHistoryAdapter.mCheckedItem;
            switch (item.getItemId()) {
                case R.id.action_edit:
                    // If item is not sent at the moment, stop sending
                    if(msg.getStatus()==ChatMessage.STATUS_SENDING) {
                        chatManager.removeFromUnsent(msg);
                        chatHistoryAdapter.removeUnsent(msg);
                    } else { // set editing item
                        mEditedItem = msg;
                    }
                    etMessage.setText(msg.getText());
                    InputMethodManager imm = (InputMethodManager)ChatActivity.this.getSystemService(Service.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(etMessage, 0);
                    mode.finish();
                    return true;
                case R.id.action_info:
                    showInfo(msg);
                    mode.finish();
                    return true;
                default:
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

    private void showInfo(final ChatMessage message) {
        //Verify the message with RSA
        ChatClient.getInstance(ChatActivity.this).getPublicKeysForMember(message.getMember(), new Callback<List<ChatPublicKey>>() {
            @Override
            public void success(List<ChatPublicKey> publicKeys, Response arg1) {
                ChatMessageValidator validator = new ChatMessageValidator(publicKeys);
                final boolean result = validator.validate(message);

                //Show a nice dialog with more information about the message
                String messageStr = String.format(getString(R.string.message_detail_text),
                        message.getMember().getDisplayName(),
                        message.getMember().getLrzId(),
                        DateFormat.getDateTimeInstance().format(message.getTimestampDate()),
                        getString(message.getStatusStringRes()),
                        getString(result ? R.string.valid : R.string.not_valid));

                new AlertDialog.Builder(ChatActivity.this)
                        .setTitle(R.string.message_details)
                        .setMessage(Html.fromHtml(messageStr))
                        .create().show();
            }

            @Override
            public void failure(RetrofitError e) {
                Utils.log(e, "Failure verifying message");
            }
        });
    }
}
