package de.tum.in.tumcampus.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.adapters.ChatHistoryAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.ChatMessage;

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
	
	private String groupId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		groupId = getIntent().getExtras().getString(Const.CHAT_ROOM_UID);
		getActionBar().setTitle(groupId);
		
		bindUIElements();
		
		// TODO: Pass chat history
		List<ChatMessage> messageHistory = new ArrayList<ChatMessage>();
		for (int i = 0; i < 10; i++) {
			messageHistory.add(new ChatMessage("Jana", "Test Message", new Date()));
		}
		loadChatHistory(messageHistory);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == btnSend.getId()) {
			// SEND MESSAGE
			// String messageText = etMessage.getText().toString();
			//ChatClient.getInstance().sendMessage(groupId, new ChatMessage("Jana", messageText, null)); // TODO: rest of the args
		}
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
