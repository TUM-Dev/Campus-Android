package de.tum.in.tumcampus.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
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

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.adapters.ChatHistoryAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.RSASigner;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatClient;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.CreateChatMessage;
import de.tum.in.tumcampus.models.ListChatMessage;
import de.tum.in.tumcampus.models.managers.ChatMessageManager;
import de.tum.in.tumcampus.models.managers.ChatRoomManager;
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

    private boolean messageSentSuccessfully = false;
    private int numberOfAttempts = 0;
    private boolean loadingMore = false;
    private EmojiconsPopup popup;
    private boolean iconShow = false;
    private ChatMessageManager chatManager;
    public static ChatRoom mCurrentOpenChatRoom = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);
        setContentView(R.layout.activity_chat);

        getIntentData();
        bindUIElements();

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("chat-message-received"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getNextHistoryFromServer();
        mCurrentOpenChatRoom = currentChatRoom;
        chatManager = new ChatMessageManager(this, currentChatRoom);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCurrentOpenChatRoom = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final ChatRoom room = new Gson().fromJson(intent.getExtras().getString(Const.CURRENT_CHAT_ROOM), ChatRoom.class);

        if (room != null) {
            //TODO compare ids instead of names, currently null
            if (!room.getName().equals(currentChatRoom.getName())) {
                currentChatRoom = room;
                getSupportActionBar().setSubtitle(currentChatRoom.getName().substring(4));
                chatHistoryAdapter = null;
                chatManager = new ChatMessageManager(this, currentChatRoom);
                getNextHistoryFromServer();
                chatHistoryAdapter.notifyDataSetChanged();
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
        if (view.getId() == btnSend.getId()) { // SEND MESSAGE
            //Check if something was entered
            if (etMessage.getText().toString().length() == 0) {
                return;
            }

            final CreateChatMessage newMessage = new CreateChatMessage(etMessage.getText().toString(), currentChatMember.getUrl());
            newMessage.setNow();
            final ListChatMessage msg = new ListChatMessage(newMessage, currentChatMember);
            chatHistoryAdapter.add(msg);

            //Post to webservice
            new Thread(new Runnable() {
                @Override
                public void run() {

                    // Generate signature
                    RSASigner signer = new RSASigner(getPrivateKeyFromSharedPrefs());
                    String signature = signer.sign(newMessage.getText());
                    newMessage.setSignature(signature);

                    while (!messageSentSuccessfully && numberOfAttempts < 5) {
                        try {
                            // Send the message to the server
                            final CreateChatMessage newlyCreatedMessage = ChatClient.getInstance(ChatActivity.this).sendMessage(currentChatRoom.getGroupId(), newMessage);
                            chatManager.replaceInto(new ListChatMessage(newlyCreatedMessage, currentChatMember));
                            final Cursor cur = chatManager.getAll();

                            ChatActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chatHistoryAdapter.sent(msg, cur);
                                }
                            });
                            messageSentSuccessfully = true;
                        } catch (RetrofitError e) {
                            Utils.log(e);
                            numberOfAttempts++;
                        }
                    }
                    messageSentSuccessfully = false;
                    numberOfAttempts = 0;
                }
            }).start();

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
        ListChatMessage message = (ListChatMessage) chatHistoryAdapter.getItem(position - 1);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        Date time;
        try {
            time = formatter.parse(message.getTimestamp());
        } catch (ParseException e) {
            Utils.log(e);
            time = new Date();
        }
        String messageStr = String.format(getString(R.string.message_detail_text),
                message.getMember().getDisplayName(),
                message.getMember().getLrzId(),
                DateFormat.getDateTimeInstance().format(time),
                Html.fromHtml(getString(message.getStatusStringRes())));

        new AlertDialog.Builder(this)
                .setTitle(R.string.message_details).setMessage(messageStr).create().show();

        /*ArrayList<ChatPublicKey> publicKeys = (ArrayList<ChatPublicKey>) ChatClient.getInstance(ChatActivity.this).getPublicKeysForMember(message.getMember().getUserId());
        ChatMessageValidator validator = new ChatMessageValidator(publicKeys);
        final boolean result = validator.validate(message);
        ChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showToast(ChatActivity.this, "Selected message is " + (result ? "" : "not ") + "valid");
            }
        });*/
        return true;
    }

    /**
     * Loads the private key from preferences
     *
     * @return The private key object
     */
    private PrivateKey getPrivateKeyFromSharedPrefs() {
        String privateKeyString = Utils.getInternalSettingString(this, Const.PRIVATE_KEY, "");
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
        return null;
    }

    /**
     * Sets the actionbar title to the current chat room
     */
    private void getIntentData() {
        Bundle extras = getIntent().getExtras();
        currentChatRoom = new Gson().fromJson(extras.getString(Const.CURRENT_CHAT_ROOM), ChatRoom.class);
        currentChatMember = new Gson().fromJson(extras.getString(Const.CURRENT_CHAT_MEMBER), ChatMember.class);
        getSupportActionBar().setSubtitle(currentChatRoom.getName().substring(4));
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
        //bar.setText(R.string.load_earlier_messages);
        lvMessageHistory.addHeaderView(bar);

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
                ArrayList<ListChatMessage> downloadedChatHistory;
                // If currently nothing has been shown load newest messages from server
                if(chatHistoryAdapter==null || chatHistoryAdapter.getSentCount()==0) {
                    downloadedChatHistory = ChatClient.getInstance(ChatActivity.this).getNewMessages(currentChatRoom.getId());
                } else {
                    long id = chatHistoryAdapter.getItemId(ChatMessageManager.COL_ID);
                    downloadedChatHistory = ChatClient.getInstance(ChatActivity.this).getMessages(currentChatRoom.getId(), id);
                }

                chatManager.replaceInto(downloadedChatHistory);

                // Got results from webservice
                Utils.logv("Success loading additional chat history: " + downloadedChatHistory.size());

                final Cursor cur = chatManager.getAll();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Update results in UI
                        if (chatHistoryAdapter == null) {
                            chatHistoryAdapter = new ChatHistoryAdapter(ChatActivity.this, cur, currentChatMember);
                            lvMessageHistory.setAdapter(chatHistoryAdapter);
                        } else if (!loadingMore) {
                            chatHistoryAdapter.changeCursor(cur);
                            chatHistoryAdapter.notifyDataSetChanged();
                        }

                        // If all messages are loaded hide header view
                        if ((cur.moveToFirst() && cur.getLong(ChatMessageManager.COL_PREVIOUS) == 0) ||cur.getCount()==0) {
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

            //If same room just refresh
            if (chatRoomString.equals(currentChatRoom.getGroupId())) {

                //Check first, if sounds are enabled
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {

                    //Play a nice notification sound
                    MediaPlayer mediaPlayer = MediaPlayer.create(ChatActivity.this, R.raw.message);
                    mediaPlayer.start();
                } else if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(500);
                }
                chatHistoryAdapter.changeCursor(chatManager.getAll());
            }
        }
    };

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (currentChatMember.getLrzId() != null) {
            // Generate signature
            RSASigner signer = new RSASigner(getPrivateKeyFromSharedPrefs());
            String signature = signer.sign(currentChatMember.getLrzId());
            currentChatMember.setSignature(signature);

            // Send request to the server to remove the user from this room
            ChatClient.getInstance(ChatActivity.this).leaveChatRoom(currentChatRoom, currentChatMember, new Callback<ChatRoom>() {
                @Override
                public void success(ChatRoom arg0, Response arg1) {
                    Utils.logv("Success leaving chat room: " + arg0.toString());
                    new ChatRoomManager(ChatActivity.this).leave(currentChatRoom);

                    // Move back to ChatRoomsSearchActivity
                    Intent intent = new Intent(ChatActivity.this, ChatRoomsSearchActivity.class);
                    startActivity(intent);
                }

                @Override
                public void failure(RetrofitError e) {
                    Utils.log(e, "Failure leaving chat room");
                }
            });
        }
    }
}
