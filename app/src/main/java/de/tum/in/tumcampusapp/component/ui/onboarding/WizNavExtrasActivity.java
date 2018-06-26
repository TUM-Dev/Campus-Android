package de.tum.in.tumcampusapp.component.ui.onboarding;

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
import de.tum.in.tumcampusapp.api.app.AuthenticationManager;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.exception.NoPrivateKey;
import de.tum.in.tumcampusapp.api.app.exception.NoPublicKey;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeStatus;
import de.tum.in.tumcampusapp.api.app.model.UploadStatus;
import de.tum.in.tumcampusapp.api.tumonline.AccessTokenManager;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.component.ui.chat.ChatRoomController;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoom;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatVerification;
import de.tum.in.tumcampusapp.service.SilenceService;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

public class WizNavExtrasActivity extends ActivityForLoadingInBackground<Void, ChatMember> {

    private SharedPreferences preferences;
    private CheckBox checkSilentMode;
    private CheckBox bugReport;
    private CheckBox groupChatMode;

    public WizNavExtrasActivity() {
        super(R.layout.activity_wiznav_extras);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // set up bug report option
        bugReport = findViewById(R.id.chk_bug_reports);
        bugReport.setChecked(preferences.getBoolean(Const.BUG_REPORTS, true));

        // Only make silent service selectable if access token exists
        // Otherwise the app cannot load lectures so silence service makes no sense
        checkSilentMode = findViewById(R.id.chk_silent_mode);
        if (new AccessTokenManager(this).hasValidAccessToken()) {
            checkSilentMode.setChecked(preferences.getBoolean(Const.SILENCE_SERVICE, false));
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

        // set up groupChat option
        groupChatMode = findViewById(R.id.chk_group_chat);
        if (new AccessTokenManager(this).hasValidAccessToken()) {
            groupChatMode.setChecked(preferences.getBoolean(Const.GROUP_CHAT_ENABLED, true));
        } else {
            groupChatMode.setChecked(false);
            groupChatMode.setEnabled(false);
        }
    }

    public void onClickTerms(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_chat_terms)));
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
                                                .getMemberRooms(member.getId(), ChatVerification.Companion.getChatVerification(this, member));
            new ChatRoomController(this).replaceIntoRooms(rooms);

            //Store that this key was activated
            Utils.setSetting(this, Const.PRIVATE_KEY_ACTIVE, true);

            // upload obfuscated ids now that we have a member
            UploadStatus uploadStatus = TUMCabeClient.getInstance(this)
                    .getUploadStatus(Utils.getSetting(this, Const.LRZ_ID, ""))
                    .blockingFirst();
            new AuthenticationManager(this).uploadObfuscatedIds(uploadStatus);

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
        startActivity(new Intent(this, WizNavStartActivity.class));
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}
