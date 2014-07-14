package de.tum.in.tumcampus.activities;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.adapters.ChatHistoryAdapter;
import de.tum.in.tumcampus.auxiliary.ChatClient;
import de.tum.in.tumcampus.auxiliary.ChatMessageValidator;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.RSASigner;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatMessage;
import de.tum.in.tumcampus.models.ChatMessage2;
import de.tum.in.tumcampus.models.ChatRoom;

/**
 * 
 * 
 * linked files: res.layout.activity_chat
 * 
 * @author Jana Pejic
 */
public class ChatActivity extends SherlockActivity implements OnClickListener, OnItemLongClickListener {
	
	/** UI elements */
	private ListView lvMessageHistory;
	private ChatHistoryAdapter chatHistoryAdapter;
	private ArrayList<ChatMessage2> chatHistory;
	
	private EditText etMessage;
	private Button btnSend;
	
	private ChatRoom currentChatRoom;
	private ChatMember currentChatMember;
	
	private boolean messageSentSuccessfully = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		getIntentData();
		bindUIElements();
		loadChatHistory();
	}
	
	@Override
	public void onClick(View view) {
		// SEND MESSAGE
		if (view.getId() == btnSend.getId()) {
			ChatMessage newMessage = new ChatMessage(etMessage.getText().toString(), currentChatMember.getUrl());
			
			// Generate signature
			RSASigner signer = new RSASigner(getPrivateKeyFromSharedPrefs());
			String signature = signer.sign(newMessage.getText());
 			newMessage.setSignature(signature);
 			
 			while (!messageSentSuccessfully) {
				try {
					// Send the message to the server
					ChatMessage newlyCreatedMessage = ChatClient.getInstance().sendMessage(currentChatRoom.getGroupId(), newMessage);
					
					// TODO: uncomment when we no longer need two message classes
					//chatHistory.add(newlyCreatedMessage);
					//chatHistoryAdapter.notifyDataSetChanged();
						
					messageSentSuccessfully = true;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 			}
			etMessage.setText("");	
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ChatMessage2 message = chatHistory.get(position);
        ArrayList<ChatPublicKey> publicKeys = (ArrayList<ChatPublicKey>) ChatClient.getInstance().getPublicKeysForMember(message.getMember().getUserId());
		ChatMessageValidator validator = new ChatMessageValidator(publicKeys);
		boolean result = validator.validate(message);
		Utils.showLongCenteredToast(this, "Selected message is " + (result ? "" : "not ") + "valid");
		return result;
	}
	
	private PrivateKey getPrivateKeyFromSharedPrefs() {
		String privateKeyString = PreferenceManager.getDefaultSharedPreferences(this).getString(Const.PRIVATE_KEY, "");
		byte[] privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT);
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			return keyFactory.generatePrivate(privateKeySpec);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void getIntentData() {
		Bundle extras = getIntent().getExtras();
		currentChatRoom = new Gson().fromJson(extras.getString(Const.CURRENT_CHAT_ROOM), ChatRoom.class);
		currentChatMember = new Gson().fromJson(extras.getString(Const.CURRENT_CHAT_MEMBER), ChatMember.class);
		getActionBar().setTitle(currentChatRoom.getName());
	}
	
	private void bindUIElements() {
		lvMessageHistory = (ListView) findViewById(R.id.lvMessageHistory);
		lvMessageHistory.setOnItemLongClickListener(this);
		etMessage = (EditText) findViewById(R.id.etMessage);
		btnSend = (Button) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(this);
	}
	
	// TODO: maybe make this sync
	private void loadChatHistory() {
		ChatClient.getInstance().getMessagesCb(currentChatRoom.getGroupId(), new Callback<List<ChatMessage2>>() {
			@Override
			public void success(List<ChatMessage2> downloadedChatHistory, Response arg1) {
				Log.d("Success loading chat history", arg1.toString());
				chatHistory = (ArrayList<ChatMessage2>) downloadedChatHistory;
				chatHistoryAdapter = new ChatHistoryAdapter(ChatActivity.this, chatHistory, currentChatMember.getUrl());
				lvMessageHistory.setAdapter(chatHistoryAdapter);
			}
			
			@Override
			public void failure(RetrofitError arg0) {
				Log.e("Failure loading chat history", arg0.toString());
			}
		});
	}
	
	// Action Bar
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getSupportMenuInflater().inflate(R.menu.menu_activity_chat, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_leave_chat_room:
			
			AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
			builder.setTitle(R.string.leave_chat_room)
				.setMessage(getResources().getString(R.string.leave_chat_room_body))
				.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (currentChatMember.getLrzId() != null) {
							// Generate signature
							RSASigner signer = new RSASigner(getPrivateKeyFromSharedPrefs());
							String signature = signer.sign(currentChatMember.getLrzId());
							currentChatMember.setSignature(signature);
							
							// Remove CHAT_TERMS_SHOWN for this room to enable rejoining the room
							PreferenceManager.getDefaultSharedPreferences(ChatActivity.this).edit().remove(Const.CHAT_TERMS_SHOWN + "_" + currentChatRoom.getName()).commit();
							
							// Send request to the server to remove the user from this room
							ChatClient.getInstance().leaveChatRoom(currentChatRoom.getGroupId(), currentChatMember.getUserId(), new Callback<String>() {
								@Override
								public void success(String arg0, Response arg1) {
									Log.d("Success leaving chat room", arg0.toString());
								}
								
								@Override
								public void failure(RetrofitError arg0) {
									Log.e("Failure leaving chat room", arg0.toString());
								}
							});
						}
					}
				})
				.setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
			
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
