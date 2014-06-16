package de.tum.in.tumcampus.activities;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.os.Bundle;
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
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatMessage;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		getIntentData();
		bindUIElements();
		
		// TODO: Pass chat history
		List<ChatMessage> messageHistory = new ArrayList<ChatMessage>();
		for (int i = 0; i < 5; i++) {
			messageHistory.add(new ChatMessage("Jana", "Test Message"/*, new Date()*/));
		}
		loadChatHistory(messageHistory);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == btnSend.getId()) {
			// SEND MESSAGE
			ChatMessage newMessage = new ChatMessage(etMessage.getText().toString(), "http://192.168.1.4:8888/members/1/" /*currentChatMember.getUrl()*/);
			ChatClient.getInstance().sendMessage(currentChatRoom.getGroupId(), newMessage, new Callback<ChatMessage>() {
				@Override
				public void success(ChatMessage arg0, Response arg1) {
					Log.e("Success", arg0.toString());
				}
				@Override
				public void failure(RetrofitError arg0) {
					Log.e("Failure", arg0.toString());
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
	
	private void loadChatHistory(List<ChatMessage> messageHistory) {
		lvMessageHistory.setAdapter(new ChatHistoryAdapter(this, messageHistory));
	}
}
