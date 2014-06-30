package de.tum.in.tumcampus.activities;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
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
import de.tum.in.tumcampus.auxiliary.Utils;
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
				
				if (sharedPrefs.contains(Const.PRIVATE_KEY)) {
					// If the key is already generated, retrieve it from shared preferences
					String privateKeyString = sharedPrefs.getString(Const.PRIVATE_KEY, "");
					byte[] privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT);
		            KeyFactory keyFactory;
					try {
						keyFactory = KeyFactory.getInstance("RSA");
						PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
						privateKey = keyFactory.generatePrivate(privateKeySpec);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					} catch (InvalidKeySpecException e) {
						e.printStackTrace();
					}
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
			        String privateKeyString = Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);
					
			        // Save private key in shared preferences
					Editor editor = sharedPrefs.edit();
					editor.putString(Const.PRIVATE_KEY, privateKeyString);
					editor.commit();
					
					// Upload public key to the server
					ChatClient.getInstance().uploadPublicKey(currentChatMember.getUserId(), new ChatPublicKey(publicKeyString), new Callback<ChatPublicKey>() {
						@Override
						public void success(ChatPublicKey arg0, Response arg1) {
							Log.d("Success uploading public key", arg0.toString());
							Utils.showLongCenteredToast(ChatActivity.this, "Public key activation mail sent to " + currentChatMember.getLrzId() + "@mytum.de");
						}
			
						@Override
						public void failure(RetrofitError arg0) {
							Log.e("Failure uploading public key", arg0.toString());
						}
					});
				}
			}
			
			// Generate signature
			RSASigner signer = new RSASigner(privateKey);
			String signature = signer.sign(newMessage.getText());
 			newMessage.setSignature(signature);
			
			// Send the message to the server
			ChatClient.getInstance().sendMessage(currentChatRoom.getGroupId(), newMessage, new Callback<ChatMessage>() {
				@Override
				public void success(ChatMessage arg0, Response arg1) {
					Log.d("Success sending message", arg0.toString());
					// TODO: display message in list
				}
				@Override
				public void failure(RetrofitError arg0) {
					Log.e("Failure sending message", arg0.getMessage() + " " + arg0.getCause());
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
		ChatClient.getInstance().getMessagesCb(currentChatRoom.getGroupId(), new Callback<List<ChatMessage>>() {
			@Override
			public void success(List<ChatMessage> chatHistory, Response arg1) {
				Log.d("Success loading chat history", chatHistory.toString());
				lvMessageHistory.setAdapter(new ChatHistoryAdapter(ChatActivity.this, chatHistory));
			}
			
			@Override
			public void failure(RetrofitError arg0) {
				Log.e("Failure loading chat history", arg0.toString());
			}
		});
	}
}
