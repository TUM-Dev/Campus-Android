package de.tum.in.tumcampus.activities.wizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.widget.CheckBox;

import com.google.gson.Gson;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.AuthenticationManager;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.TUMCabeClient;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatPublicKey;
import de.tum.in.tumcampus.models.LecturesSearchRow;
import de.tum.in.tumcampus.models.LecturesSearchRowSet;
import de.tum.in.tumcampus.models.managers.ChatRoomManager;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;
import retrofit.RetrofitError;

/**
 *
 */
public class WizNavChatActivity extends ActivityForLoadingInBackground<Void, ChatMember> {

    private boolean tokenSetup = false;
    private CheckBox groupChatMode, autoJoin, acceptedTerms;

    public WizNavChatActivity() {
        super(R.layout.activity_wiznav_chat);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        disableRefresh();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // If called because app version changed remove "Step 3" and close on back pressed
        Intent i = getIntent();
        if (i != null && i.hasExtra(Const.TOKEN_IS_SETUP)) {
            tokenSetup = i.getBooleanExtra(Const.TOKEN_IS_SETUP, false);
        }

        // Get handles to all UI elements
        groupChatMode = (CheckBox) findViewById(R.id.chk_group_chat);
        autoJoin = (CheckBox) findViewById(R.id.chk_auto_join_chat);
        acceptedTerms = (CheckBox) findViewById(R.id.chk_group_chat_terms);


        // Only make silent service selectable if access token exists
        // Otherwise the app cannot load lectures so silence service makes no sense
        if (new AccessTokenManager(this).hasValidAccessToken()) {
            groupChatMode.setChecked(preferences.getBoolean(Const.GROUP_CHAT_ENABLED, true));
            autoJoin.setChecked(preferences.getBoolean(Const.AUTO_JOIN_NEW_ROOMS, true));
            acceptedTerms.setChecked(preferences.getBoolean(Const.GROUP_CHAT_ENABLED, false));
        } else {
            groupChatMode.setChecked(false);
            groupChatMode.setEnabled(false);
            autoJoin.setEnabled(false);
            acceptedTerms.setEnabled(false);
        }
    }

    public void onClickTerms(View view) {
        Uri uri = Uri.parse("https://tumcabe.in.tum.de/landing/chatterms/");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
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

    /**
     * Open next activity on skip
     *
     * @param skip Skip button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickSkip(View skip) {
        startNextActivity();
    }

    /**
     * If next is pressed, check if token has been activated
     *
     * @param next Next button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickNext(View next) {
        if (groupChatMode.isChecked()) {
            if (!acceptedTerms.isChecked()) {
                Utils.showToast(this, R.string.have_to_accept_terms);
                return;
            }
        }

        startLoading();
    }

    /**
     * Opens next wizard page
     */
    private void startNextActivity() {
        finish();
        Intent intent;
        if (!Utils.getInternalSettingBool(this, Const.PRIVATE_KEY_ACTIVE, false)) {
            intent = new Intent(this, WizNavActivatePublicKeyActivity.class);
        } else {
            intent = new Intent(this, WizNavExtrasActivity.class);
        }
        intent.putExtra(Const.TOKEN_IS_SETUP, tokenSetup);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    @Override
    protected ChatMember onLoadInBackground(Void... arg) {
        if (groupChatMode.isChecked()) {
            if (!NetUtils.isConnected(this)) {
                showNoInternetLayout();
                return null;
            }

            // Get all of the users lectures and save them as possible chat rooms
            ChatRoomManager manager = new ChatRoomManager(this);
            TUMOnlineRequest<LecturesSearchRowSet> requestHandler = new TUMOnlineRequest<>(TUMOnlineConst.LECTURES_PERSONAL, this, true);
            LecturesSearchRowSet lecturesList = requestHandler.fetch();
            if (lecturesList != null && lecturesList.getLehrveranstaltungen() != null) {
                List<LecturesSearchRow> lectures = lecturesList.getLehrveranstaltungen();
                manager.replaceInto(lectures);
            } else {
                Utils.showToastOnUIThread(this, R.string.no_rights_to_access_lectures);
                return null;
            }

            // Get the users full name
            String id = Utils.getSetting(this, Const.CHAT_ROOM_DISPLAY_NAME, "");

            // Get the users lrzId and initialise chat member
            String lrzId = Utils.getSetting(this, Const.LRZ_ID, "");
            ChatMember currentChatMember = new ChatMember(lrzId);
            currentChatMember.setDisplayName(id);

            // Tell the server the new member
            ChatMember member;
            try {
                // After the user has entered their display name, send a request to the server to create the new member
                member = TUMCabeClient.getInstance(this).createMember(currentChatMember);
            } catch (RetrofitError e) {
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
            if (am.generatePrivateKey(member)) {
                return member;
            } else {
                Utils.showToastOnUIThread(this, getString(R.string.failure_uploading_public_key));
            }
        }
        return null;
    }

    @Override
    protected void onLoadFinished(ChatMember member) {
        if (member != null) {
            Utils.setSetting(this, Const.GROUP_CHAT_ENABLED, groupChatMode.isChecked());
            Utils.setSetting(this, Const.AUTO_JOIN_NEW_ROOMS, groupChatMode.isChecked() && autoJoin.isChecked());
            Utils.setSetting(this, Const.CHAT_MEMBER, member);
            Utils.log("Set member to settings: " + new Gson().toJson(member));
            startNextActivity();
        } else {
            showLoadingEnded();
        }
    }
}
