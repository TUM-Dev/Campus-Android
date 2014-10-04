package de.tum.in.tumcampus.activities.wizard;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.StartupActivity;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatClient;
import de.tum.in.tumcampus.models.ChatMember;

public class WizNavExtrasActivity extends ActionBarActivity {

    private SharedPreferences preferences;
    private CheckBox checkBackgroundMode;
    private CheckBox checkSilentMode;
    private CheckBox groupChatMode;
    private CheckBox acceptedTerms;
    private EditText nickName;
    private boolean tokenSetup = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_wiznav_extras);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Intent i = getIntent();
        if (i != null && i.hasExtra(Const.TOKEN_IS_SETUP)) {
            tokenSetup = i.getBooleanExtra(Const.TOKEN_IS_SETUP, false);
            findViewById(R.id.step_3).setVisibility(View.GONE);
        }

        checkSilentMode = (CheckBox) findViewById(R.id.chk_silent_mode);
        checkBackgroundMode = (CheckBox) findViewById(R.id.chk_background_mode);
        groupChatMode = (CheckBox) findViewById(R.id.chk_group_chat);
        acceptedTerms = (CheckBox) findViewById(R.id.chk_group_chat_terms);
        nickName = (EditText) findViewById(R.id.nickname);

        // Only make silent service selectable if access token exists
        // Otherwise the app cannot load lectures so silence service makes no sense
        if (new AccessTokenManager(this).hasValidAccessToken()) {
            checkSilentMode.setChecked(preferences.getBoolean(Const.SILENCE_SERVICE, true));
            groupChatMode.setChecked(preferences.getBoolean(Const.GROUP_CHAT_ENABLED, true));
            nickName.setText(preferences.getString(Const.CHAT_ROOM_DISPLAY_NAME, ""));
        } else {
            checkSilentMode.setChecked(false);
            checkSilentMode.setEnabled(false);
            groupChatMode.setChecked(false);
            groupChatMode.setEnabled(false);
            nickName.setEnabled(false);
            acceptedTerms.setEnabled(false);
        }
        checkBackgroundMode.setChecked(preferences.getBoolean(Const.BACKGROUND_MODE, true));
    }

    /**
     * Set preference values and open {@link StartupActivity}
     *
     * @param done Done button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickDone(View done) {
        // Gets the editor for editing preferences and updates the preference
        // values with the chosen state
        Editor editor = preferences.edit();

        //Save Group Chat values
        editor.putBoolean(Const.GROUP_CHAT_ENABLED, groupChatMode.isChecked());
        if(groupChatMode.isChecked()) {
            if (!acceptedTerms.isChecked()) {
                Utils.showToast(this, R.string.have_to_accept_terms);
                return;
            } else if (nickName.getText().toString().trim().isEmpty()) {
                Utils.showToast(this, R.string.nickname_empty);
                return;
            }


            // If the user is opening the chat for the first time, we need to display
            // a dialog where they can enter their desired display name
            String nickname = nickName.getText().toString();
            String lrzId = Utils.getSetting(this, Const.LRZ_ID);
            ChatMember currentChatMember = new ChatMember(lrzId);
            currentChatMember.setDisplayName(nickname);


            // After the user has entered their display name,
            // send a request to the server to create the new member
            currentChatMember = ChatClient.getInstance().createMember(currentChatMember);

            // Save display name in shared preferences
            editor.putString(Const.CHAT_ROOM_DISPLAY_NAME, nickname);
        }

        //Save other settings
        editor.putBoolean(Const.SILENCE_SERVICE, checkSilentMode.isChecked());
        editor.putBoolean(Const.BACKGROUND_MODE, checkBackgroundMode.isChecked());
        editor.putBoolean(Const.HIDE_WIZARD_ON_STARTUP, true);
        //Apply them to the shared prefs
        editor.apply();

        //Exit and start the main activity
        finish();
        startActivity(new Intent(this, StartupActivity.class));
    }

    public void onClickTerms(View view) {
        new AlertDialog.Builder(this).setTitle(R.string.chat_terms_title)
                .setMessage(getResources().getString(R.string.chat_terms_body))
                .setPositiveButton(android.R.string.ok, null).create().show();
        //TODO show chat terms
    }

    /**
     * If back key is pressed, open previous activity
     */
    @Override
    public void onBackPressed() {
        finish();
        if (!tokenSetup) {
            if (new AccessTokenManager(this).hasValidAccessToken())
                startActivity(new Intent(this, WizNavCheckTokenActivity.class));
            else
                startActivity(new Intent(this, WizNavStartActivity.class));
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
    }
}
