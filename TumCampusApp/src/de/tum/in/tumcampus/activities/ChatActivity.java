package de.tum.in.tumcampus.activities;

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

/**
 * 
 * @author Jana Pejic
 */
public class ChatActivity extends Activity implements OnClickListener {
	
	/** UI elements */
	private ListView lvMessageHistory;
	private EditText etMessage;
	private Button btnSend;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		getActionBar().setTitle("Hello world App"); // TODO: Change to subject name
		
		lvMessageHistory = (ListView) findViewById(R.id.lvMessageHistory);
		loadChatHistory(null); // TODO: Pass chat history
		etMessage = (EditText) findViewById(R.id.etMessage);
		btnSend = (Button) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == btnSend.getId()) {
			// TODO: SEND MESSAGE
		}
	}
	
	private void loadChatHistory(List<String> messageHistory) { // TODO: Use a new class Message instead of String
		lvMessageHistory.setAdapter(new ChatHistoryAdapter());
	}
}
