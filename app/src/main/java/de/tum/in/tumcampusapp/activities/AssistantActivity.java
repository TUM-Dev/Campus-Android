package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.AssistantHistoryAdapter;
import de.tum.in.tumcampusapp.api.UCentralClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.FileUtils;
import de.tum.in.tumcampusapp.auxiliary.ImplicitCounter;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;
import de.tum.in.tumcampusapp.services.assistantServices.AssistantService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.actions.SearchIntents.ACTION_SEARCH;
import static com.google.android.gms.actions.SearchIntents.EXTRA_QUERY;

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

    private ProgressDialog progressDialog;

    private static final int READ_REQUEST_CODE = 42;

    private TextToSpeech textToSpeech;

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

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Asking Luis...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.assistant_name);
        }

        user = Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class);
        assistant = new ChatMember(0, "", getResources().getString(R.string.assistant_name));

        bindUIElements();

        // LocalBroadcastManager to handle the result from Azure
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleIntent(intent);
            }
        };
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter(Const.ASSISTANT_BROADCAST_INTENT));

        initAssistant();
        handleIntent(getIntent());

        // Text2speech feature for answers
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });
    }

    private void initAssistant() {
        String fullName = user.getDisplayName();
        String firstName = fullName.substring(0, fullName.indexOf(" "));

        String introductoryMessage = String.format(
                getResources().getString(R.string.assistant_intro_msg),
                firstName
        );

        assistantHistoryAdapter.addElement(new ChatMessage(introductoryMessage, assistant));
    }

    private void handleIntent(Intent intent) {
        if (intent == null)
            return;

        // Handle the result from AssistantService
        if (intent.hasExtra(AssistantService.EXTRA_RESULT)) {
            receiveMessage(intent.getStringExtra(AssistantService.EXTRA_RESULT));

        // Ok Google
        } else if (ACTION_SEARCH.equals(intent.getAction()) && intent.hasExtra(EXTRA_QUERY)) {
            sendMessage(intent.getStringExtra(EXTRA_QUERY));

        // FAB Button
        } else if (intent.hasExtra(Const.ASSISTANT_QUERY)) {
            sendMessage(intent.getStringExtra(Const.ASSISTANT_QUERY));
        }
    }

    private void bindUIElements() {
        setUpAssistantHistory();

        etMessage = (EditText) findViewById(R.id.etMessage);

        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(this);
    }

    private void setUpAssistantHistory() {
        rvMessageHistory = (RecyclerView) findViewById(R.id.rvMessageHistory);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvMessageHistory.setLayoutManager(mLayoutManager);

        assistantHistoryAdapter = new AssistantHistoryAdapter(new ArrayList<ChatMessage>());
        rvMessageHistory.setAdapter(assistantHistoryAdapter);
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

    @SuppressWarnings("deprecation")
    private void receiveMessage(String text) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        assistantHistoryAdapter.addElement(new ChatMessage(text, assistant));
        int countWords = text.length() - text.replace(" ", "").length() - text.replace("\n", "").length();
        if (countWords < 20) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }

        rvMessageHistory.smoothScrollToPosition(rvMessageHistory.getAdapter().getItemCount() - 1);
    }

    private void sendMessage(String text) {
        progressDialog.show();
        assistantHistoryAdapter.addElement(new ChatMessage(text, user));
        AssistantService.startActionProcessQuery(getApplicationContext(), text);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        // Speech to Text
        if (requestCode == Const.SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            etMessage.setText(results.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
