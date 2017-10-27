package de.tum.in.tumcampusapp.activities.wizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;

import java.io.IOException;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.StartupActivity;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.AccessTokenManager;
import de.tum.in.tumcampusapp.auxiliary.AuthenticationManager;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.exceptions.NoPublicKey;
import de.tum.in.tumcampusapp.managers.ChatRoomManager;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatRoom;
import de.tum.in.tumcampusapp.models.tumcabe.ChatVerification;
import de.tum.in.tumcampusapp.services.SilenceService;

public class WizNavExtrasActivity extends ActivityForLoadingInBackground<Void, ChatMember> {

    private SharedPreferences preferences;
    private CheckBox checkSilentMode;
    private CheckBox bugReport;
    private CheckBox groupChatMode;
    private boolean tokenSetup;

    public WizNavExtrasActivity() {
        super(R.layout.activity_wiznav_extras);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // If called because app version changed remove "Step 4" and close on back pressed
        Intent i = getIntent();
        if (i != null && i.hasExtra(Const.TOKEN_IS_SETUP)) {
            //If coming from a 6X version we need to upload the public key to TUMO
            AuthenticationManager am = new AuthenticationManager(this);
            try {
                am.uploadPublicKey();
            } catch (NoPublicKey noPublicKey) {
                Utils.log(noPublicKey);
            }

            //Remember that we are only running through a limited setup
            tokenSetup = i.getBooleanExtra(Const.TOKEN_IS_SETUP, false);
        }

        // Get handles to all UI elements
        checkSilentMode = findViewById(R.id.chk_silent_mode);
        bugReport = findViewById(R.id.chk_bug_reports);
        bugReport.setChecked(preferences.getBoolean(Const.BUG_REPORTS, true));

        // Only make silent service selectable if access token exists
        // Otherwise the app cannot load lectures so silence service makes no sense
        if (new AccessTokenManager(this).hasValidAccessToken()) {
            checkSilentMode.setChecked(preferences.getBoolean(Const.SILENCE_SERVICE, true));
            checkSilentMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (checkSilentMode.isChecked() &&
                    !SilenceService.hasPermissions(WizNavExtrasActivity.this)) {
                    SilenceService.requestPermissions(WizNavExtrasActivity.this);
                    checkSilentMode.setChecked(false);
                }
            });
        } else {
            checkSilentMode.setChecked(false);
            checkSilentMode.setEnabled(false);
        }

        // Get handles to all UI elements
        groupChatMode = findViewById(R.id.chk_group_chat);
        if (new AccessTokenManager(this).hasValidAccessToken()) {
            groupChatMode.setChecked(preferences.getBoolean(Const.GROUP_CHAT_ENABLED, true));
        } else {
            groupChatMode.setChecked(false);
            groupChatMode.setEnabled(false);
        }
    }

    public void onClickTerms(View view) {
        Uri uri = Uri.parse(Const.CHATTERMS_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    protected ChatMember onLoadInBackground(Void... arg) {
        if (!NetUtils.isConnected(this)) {
            showNoInternetLayout();
            return null;
        }

        // Get the users lrzId and initialise chat member
        ChatMember currentChatMember = new ChatMember(Utils.getSetting(this, Const.LRZ_ID, ""));
        currentChatMember.setDisplayName(Utils.getSetting(this, Const.CHAT_ROOM_DISPLAY_NAME, ""));

        if (currentChatMember.getLrzId()
                             .equals("")) {
            return currentChatMember;
        }

        // Tell the server the new member
        ChatMember member;
        try {
            // After the user has entered their display name, send a request to the server to create the new member
            member = TUMCabeClient.getInstance(this)
                                  .createMember(currentChatMember);
        } catch (IOException e) {
            Utils.log(e);
            Utils.showToastOnUIThread(this, R.string.error_setup_chat_member);
            return null;
        }

        //Catch a possible error, when we didn't get something returned
        if (member == null || member.getLrzId() == null) {
            Utils.showToastOnUIThread(this, R.string.error_setup_chat_member);
            return null;
        }

        // Generate the private key and upload the public key to the server
        AuthenticationManager am = new AuthenticationManager(this);
        if (!am.generatePrivateKey(member)) {
            Utils.showToastOnUIThread(this, getString(R.string.failure_uploading_public_key)); //We cannot continue if the upload of the Public Key does not work
            return null;
        }

        // Try to restore already joined chat rooms from server
        try {
            List<ChatRoom> rooms = TUMCabeClient.getInstance(this)
                                                .getMemberRooms(member.getId(), new ChatVerification(this, member));
            new ChatRoomManager(this).replaceIntoRooms(rooms);

            //Store that this key was activated
            Utils.setInternalSetting(this, Const.PRIVATE_KEY_ACTIVE, true);

            return member;
        } catch (IOException | NoPrivateKey e) {
            Utils.log(e);
        }

        return null;
    }

    @Override
    protected void onLoadFinished(ChatMember member) {
        if (member == null) {
            showLoadingEnded();
            return;
        }

        // Gets the editor for editing preferences and updates the preference values with the chosen state
        Editor editor = preferences.edit();
        editor.putBoolean(Const.SILENCE_SERVICE, checkSilentMode.isChecked());
        editor.putBoolean(Const.BACKGROUND_MODE, true); // Enable by default
        editor.putBoolean(Const.BUG_REPORTS, bugReport.isChecked());
        editor.putBoolean(Const.HIDE_WIZARD_ON_STARTUP, true);

        if (!member.getLrzId()
                   .equals("")) {
            Utils.setSetting(this, Const.GROUP_CHAT_ENABLED, groupChatMode.isChecked());
            Utils.setSetting(this, Const.AUTO_JOIN_NEW_ROOMS, groupChatMode.isChecked());
            Utils.setSetting(this, Const.CHAT_MEMBER, member);
        }
        editor.apply();

        finish();
        startActivity(new Intent(this, StartupActivity.class));
    }

    /**
     * Set preference values and open {@link StartupActivity}
     *
     * @param done Done button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickDone(View done) {
        startLoading();
    }

    /**
     * If back key is pressed, open previous activity
     */
    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(this, WizNavStartActivity.class);
        intent.putExtra(Const.TOKEN_IS_SETUP, tokenSetup);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

    }
}
