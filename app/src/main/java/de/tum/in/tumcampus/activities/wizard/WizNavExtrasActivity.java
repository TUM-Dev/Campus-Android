package de.tum.in.tumcampus.activities.wizard;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.StartupActivity;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatClient;
import de.tum.in.tumcampus.models.ChatMember;
import retrofit.RetrofitError;

public class WizNavExtrasActivity extends ActivityForLoadingInBackground<String, Boolean> {

    private SharedPreferences preferences;
    private CheckBox checkBackgroundMode;
    private CheckBox checkSilentMode;
    private CheckBox groupChatMode;
    private CheckBox acceptedTerms;
    private CheckBox bugReport;
    private EditText nickName;
    private boolean tokenSetup = false;

    public WizNavExtrasActivity() {
        super(R.layout.activity_wiznav_extras);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // If called because app version changed remove "Step 3" and close on back pressed
        Intent i = getIntent();
        if (i != null && i.hasExtra(Const.TOKEN_IS_SETUP)) {
            tokenSetup = i.getBooleanExtra(Const.TOKEN_IS_SETUP, false);
            findViewById(R.id.step_3).setVisibility(View.GONE);
        }

        // Get handles to all UI elements
        checkSilentMode = (CheckBox) findViewById(R.id.chk_silent_mode);
        checkBackgroundMode = (CheckBox) findViewById(R.id.chk_background_mode);
        groupChatMode = (CheckBox) findViewById(R.id.chk_group_chat);
        acceptedTerms = (CheckBox) findViewById(R.id.chk_group_chat_terms);
        bugReport = (CheckBox) findViewById(R.id.chk_bug_reports);
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
        String nickname = nickName.getText().toString().trim();
        if(groupChatMode.isChecked()) {
            if (!acceptedTerms.isChecked()) {
                Utils.showToast(this, R.string.have_to_accept_terms);
                return;
            } else if (nickname.isEmpty()) {
                Utils.showToast(this, R.string.nickname_empty);
                return;
            } else if (nickname.length() < 3) {
                Utils.showToast(this, R.string.nickname_too_short);
                return;
            }
        }

        startLoading(nickname);
    }

    @Override
    protected Boolean onLoadInBackground(String... arg) {
        // If the user is opening the chat for the first time, we need to display
        // a dialog where they can enter their desired display name
        String lrzId = Utils.getSetting(this, Const.LRZ_ID);
        ChatMember currentChatMember = new ChatMember(lrzId);
        currentChatMember.setDisplayName(arg[0]);

        if(!NetUtils.isConnected(this)) {
            return false;
        }

        try {
            // After the user has entered their display name, send a request to the server to create the new member
            ChatClient.getInstance(this.getApplicationContext()).createMember(currentChatMember);
        } catch(RetrofitError e) {
            showErrorLayout();
            Utils.log(e);
            return false;
        }
        return true;
    }

    @Override
    protected void onLoadFinished(Boolean result) {
        if(!NetUtils.isConnected(this)) {
            showNoInternetLayout();
            return;
        }
        if(result) {
            // Gets the editor for editing preferences and
            // updates the preference values with the chosen state
            Editor editor = preferences.edit();
            editor.putBoolean(Const.SILENCE_SERVICE, checkSilentMode.isChecked());
            editor.putBoolean(Const.BACKGROUND_MODE, checkBackgroundMode.isChecked());
            editor.putBoolean(Const.GROUP_CHAT_ENABLED, groupChatMode.isChecked());
            editor.putBoolean(Const.BUG_REPORTS, bugReport.isChecked());

            // Save display name in shared preferences
            editor.putString(Const.CHAT_ROOM_DISPLAY_NAME, nickName.getText().toString().trim());

            editor.putBoolean(Const.HIDE_WIZARD_ON_STARTUP, true);
            editor.apply();

            finish();
            startActivity(new Intent(this, StartupActivity.class));
        }
    }

    public void onClickTerms(View view) {
        new AlertDialog.Builder(this).setTitle(R.string.chat_terms_title)
                .setMessage(getResources().getString(R.string.chat_terms_body))
                .setPositiveButton(android.R.string.ok, null).create().show();
        //TODO show chat terms: update string to correct terms
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
