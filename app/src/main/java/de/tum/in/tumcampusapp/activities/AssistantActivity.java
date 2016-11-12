package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
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

    private static final int READ_REQUEST_CODE = 42;

    private Callback<Void> stupidCB = new Callback<Void>() {
        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {
            if (response.isSuccessful()) {
                Log.d("LOOOOOOL", "SUCCESSFULLY PRINTED");
            } else {
                Log.d("LOOOOOOL", "NOT PRINTED");
            }
        }

        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            Log.d("FAILED", ":(");
            Log.d("FAILED", t.getMessage());
        }
    };

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

        if (false) {
            FileUtils.performFileSearch(this, READ_REQUEST_CODE);
            UCentralClient.getInstance(this).login("UNAME", "PASS", new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("LOGGED IN", "SUCCESS");
                    for (File f : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles()) {
                        if (f.getAbsolutePath().contains("tester")) {
                            UCentralClient.getInstance(getApplicationContext()).printFile(f, stupidCB);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.d("LOGGED IN", "FAIL");
                }
            });
        }

        String name = Utils.getSetting(this, Const.CHAT_ROOM_DISPLAY_NAME, getString(R.string.token_not_enabled));

        if (name.contains(" ")){
            name = name.substring(0, name.indexOf(" "));
        }

        String introductoryMessage = "Hi " + name + ", how can I help you?";

        assistantHistoryAdapter.addElement(new ChatMessage(introductoryMessage, assistant));

        // handle the intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sendMessage(extras.getString(Const.ASSISTANT_QUERY));
        }

        // Text 2 speech feature for answers
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });
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
        assistantHistoryAdapter.addElement(new ChatMessage(text, assistant));
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        rvMessageHistory.smoothScrollToPosition(rvMessageHistory.getAdapter().getItemCount());
    }

    private void sendMessage(String text) {
        assistantHistoryAdapter.addElement(new ChatMessage(text, user));
        AssistantService.startActionProcessQuery(getApplicationContext(), text);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == Const.SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            etMessage.setText(results.get(0));
        } else if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                Log.d("FILE NAME", uri.getPath());
                Log.d("FILE NAME", uri.getEncodedPath());
//                UCentralClient.getInstance(this).printFile(uri, stupidCB);
            }
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
}
