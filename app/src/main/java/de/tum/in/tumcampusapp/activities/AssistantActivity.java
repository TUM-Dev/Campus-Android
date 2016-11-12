package de.tum.in.tumcampusapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.AssistantHistoryAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.ImplicitCounter;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;
import de.tum.in.tumcampusapp.services.AssistantService;

public class AssistantActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * UI elements
     */
    private RecyclerView rvMessageHistory;
    private AssistantHistoryAdapter assistantHistoryAdapter;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnSpeak;

    private ChatMember assistant;
    private ChatMember user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.count(this);
        setContentView(R.layout.activity_assistant);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.assistant_name);
        }

        user = Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class);
        assistant = new ChatMember(0, "", getResources().getString(R.string.assistant_name));

        // bindUIElements
        rvMessageHistory = (RecyclerView) findViewById(R.id.rvMessageHistory);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvMessageHistory.setLayoutManager(mLayoutManager);

        assistantHistoryAdapter = new AssistantHistoryAdapter(new ArrayList<ChatMessage>());
        rvMessageHistory.setAdapter(assistantHistoryAdapter);

        etMessage = (EditText) findViewById(R.id.etMessage);

        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(this);

        // LocalBroadcastManager to handle the result from Azure
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receiveMessage(intent.getStringExtra(AssistantService.EXTRA_RESULT));
            }
        };
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter(Const.ASSISTANT_BROADCAST_INTENT));

        // handle the intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sendMessage(extras.getString(Const.ASSISTANT_QUERY));
        }
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();
        if (vid == btnSend.getId()) {
            String text = etMessage.getText().toString();
            if (text.isEmpty()) {
                return;
            }
            sendMessage(text);
            etMessage.setText("");
        } else if (vid == btnSpeak.getId()) {
            Utils.sendRecognizerIntent(this, Const.SPEECH_REQUEST_CODE);
        }
    }

    private void receiveMessage(String text) {
        assistantHistoryAdapter.addElement(new ChatMessage(text, assistant));
    }

    private void sendMessage(String text) {
        assistantHistoryAdapter.addElement(new ChatMessage(text, user));
        AssistantService.startActionProcessQuery(getApplicationContext(), text);
    }

    /**
     * This callback is invoked when the Speech Recognizer returns.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == Const.SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            etMessage.setText(results.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
