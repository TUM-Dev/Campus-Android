package de.tum.in.tumcampus.activities;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.adapters.ChatHistoryAdapter;
import de.tum.in.tumcampus.auxiliary.ChatClient;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.RSASigner;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatMessage;
import de.tum.in.tumcampus.models.ChatPublicKey;
import de.tum.in.tumcampus.models.ChatRoom;

/**
 * 
 * 
 * linked files: res.layout.activity_chat
 * 
 * @author Jana Pejic
 */
public class ChatActivity extends Activity implements OnClickListener {
	
	/** UI elements */
	private ListView lvMessageHistory;
	private EditText etMessage;
	private Button btnSend;
	
	private ChatRoom currentChatRoom;
	private ChatMember currentChatMember;
	private PrivateKey privateKey = null;
	
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
			
			if (privateKey == null) {
				// Generate/Retrieve private key
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
				
				//sharedPrefs.edit().remove(Const.PRIVATE_KEY).commit();
				if (sharedPrefs.contains(Const.PRIVATE_KEY)) {
					// If the key is already generated, retrieve it from shared preferences
					privateKey = new Gson().fromJson(sharedPrefs.getString(Const.PRIVATE_KEY, ""), PrivateKey.class);
				} else {
					// If the key is not in shared preferences, generate key-pair
					KeyPairGenerator keyGen = null;
					try {
						keyGen = KeyPairGenerator.getInstance("RSA");
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
			        keyGen.initialize(1024);
			        KeyPair keyPair = keyGen.generateKeyPair();
			        
			        byte[] publicKey = keyPair.getPublic().getEncoded();
			        String publicKeyString = Base64.encodeToString(publicKey, Base64.DEFAULT);
			        
			        privateKey = keyPair.getPrivate();
					
					// Save private key in shared preferences
					Editor editor = sharedPrefs.edit();
					editor.putString(Const.PRIVATE_KEY, new Gson().toJson(privateKey));
					editor.commit();
					
					// Upload public key to the server
					ChatClient.getInstance().uploadPublicKey(currentChatMember.getUserId(), new ChatPublicKey(publicKeyString), new Callback<ChatPublicKey>() {
						@Override
						public void success(ChatPublicKey arg0, Response arg1) {
							Log.d("Success uploading public key", arg0.toString());
						}
			
						@Override
						public void failure(RetrofitError arg0) {
							Log.d("Failure uploading public key", arg0.toString());
						}
					});
				}
			}
			
			// Generate signature
			RSASigner signer = new RSASigner(privateKey);
			newMessage.setSignature(signer.sign(newMessage.getText()));
			
			// Send the message to the server
			ChatClient.getInstance().sendMessage(currentChatRoom.getGroupId(), newMessage, new Callback<ChatMessage>() {
				@Override
				public void success(ChatMessage arg0, Response arg1) {
					Log.d("Success sending message", arg0.toString());
					// TODO: display message in list
				}
				@Override
				public void failure(RetrofitError arg0) {
					Log.d("Failure sending message", arg0.toString());
					// TODO: somehow signal that the message was not sent
				}
			});
			etMessage.setText("");
		}
	}
	
	private void getIntentData() {
		Bundle extras = getIntent().getExtras();
		currentChatRoom = new Gson().fromJson(extras.getString(Const.CURRENT_CHAT_ROOM), ChatRoom.class);
		currentChatMember = new Gson().fromJson(extras.getString(Const.CURRENT_CHAT_MEMBER), ChatMember.class);
		getActionBar().setTitle(currentChatRoom.getName());
	}
	
	private void bindUIElements() {
		lvMessageHistory = (ListView) findViewById(R.id.lvMessageHistory);
		etMessage = (EditText) findViewById(R.id.etMessage);
		btnSend = (Button) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(this);
	}
	
	private void loadChatHistory() {
		final List<ChatMessage> messageHistory = new ArrayList<ChatMessage>();
		//messageHistory = ChatClient.getInstance().getMessages(currentChatRoom.getGroupId());
		
		ChatClient.getInstance().getMessagesCb(currentChatRoom.getGroupId(), new Callback<List<ChatMessage>>() {
			@Override
			public void success(List<ChatMessage> arg0, Response arg1) {
				Log.d("Success loading chat history", arg0.toString());
			}
			
			@Override
			public void failure(RetrofitError arg0) {
				Log.d("Failure loading chat history", arg0.toString());
			}
		});
		
		lvMessageHistory.setAdapter(new ChatHistoryAdapter(this, messageHistory));
	}
}
