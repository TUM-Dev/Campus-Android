package de.tum.in.tumcampus.activities.wizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.widget.CheckBox;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatClient;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatPublicKey;
import retrofit.RetrofitError;

/**
 *
 */
public class WizNavChatActivity extends ActivityForLoadingInBackground<Void, Boolean> {

    private SharedPreferences preferences;
    private boolean tokenSetup = false;
    private CheckBox groupChatMode;
    private CheckBox acceptedTerms;

    public WizNavChatActivity() {
		super(R.layout.activity_wiznav_chat);
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        disableRefresh();

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // If called because app version changed remove "Step 3" and close on back pressed
        Intent i = getIntent();
        if (i != null && i.hasExtra(Const.TOKEN_IS_SETUP)) {
            tokenSetup = i.getBooleanExtra(Const.TOKEN_IS_SETUP, false);
            findViewById(R.id.step_3).setVisibility(View.GONE);
        }

        // Get handles to all UI elements
        groupChatMode = (CheckBox) findViewById(R.id.chk_group_chat);
        acceptedTerms = (CheckBox) findViewById(R.id.chk_group_chat_terms);

        // Only make silent service selectable if access token exists
        // Otherwise the app cannot load lectures so silence service makes no sense
        if (new AccessTokenManager(this).hasValidAccessToken()) {
            groupChatMode.setChecked(preferences.getBoolean(Const.GROUP_CHAT_ENABLED, true));
        } else {
            groupChatMode.setChecked(false);
            groupChatMode.setEnabled(false);
            acceptedTerms.setEnabled(false);
        }
    }

    public void onClickTerms(View view) {
        /*new AlertDialog.Builder(this).setTitle(R.string.chat_terms_title)
                .setMessage(getResources().getString(R.string.chat_terms_body))
                .setPositiveButton(android.R.string.ok, null).create().show();*/
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_chat_terms)));
        startActivity(myIntent);
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
     * @param skip Skip button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickSkip(View skip) {
        startNextActivity();
    }

    /**
     * If next is pressed, check if token has been activated
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
        Intent intent = new Intent(this, WizNavExtrasActivity.class);
        intent.putExtra(Const.TOKEN_IS_SETUP, tokenSetup);
		startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	}

    @Override
    protected Boolean onLoadInBackground(Void... arg) {
        if (groupChatMode.isChecked()) {
            if (!NetUtils.isConnected(this)) {
                showNoInternetLayout();
                return false;
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
                member = ChatClient.getInstance(this.getApplicationContext()).createMember(currentChatMember);

                //Catch a possible error, when we didn't get something returned
                if (member == null || member.getLrzId() == null) {
                    Utils.showToastOnUIThread(this, R.string.error_setup_chat_member);
                    return false;
                }
            } catch (RetrofitError e) {
                Utils.log(e);
                Utils.showToastOnUIThread(this, R.string.error_setup_chat_member);
                return false;
            }

            // Generate the private key and upload the public key to the server
            return generatePrivateKey(member);
        }
        return true;
    }

    @Override
    protected void onLoadFinished(Boolean result) {
        if (result) {
            Utils.setSetting(this, Const.GROUP_CHAT_ENABLED, groupChatMode.isChecked());
            startNextActivity();
        } else {
            showLoadingEnded();
        }
    }
    /**
     * Gets private key from preferences or generates one.
     *
     * @return Private key instance
     */
    private boolean generatePrivateKey(ChatMember member) {
        // Retrieve private key
        String privateKeyString = Utils.getInternalSettingString(this, Const.PRIVATE_KEY, "");

        if (privateKeyString.isEmpty()) {
            // If the key is not in shared preferences, generate key-pair
            KeyPairGenerator keyGen;
            try {
                keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(1024);
                KeyPair keyPair = keyGen.generateKeyPair();

                String publicKeyString = Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT);
                privateKeyString = Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.DEFAULT);

                // Upload public key to the server
                try {
                    ChatClient.getInstance(this).uploadPublicKey(member.getId(), new ChatPublicKey(publicKeyString));

                    // Save private key in shared preferences
                    Utils.setInternalSetting(this, Const.PRIVATE_KEY, privateKeyString);

                    Utils.logv("Success uploading public key: " + publicKeyString);
                    Utils.showToastOnUIThread(this, String.format(getString(R.string.public_key_mail), member.getLrzId()));
                    return true;
                } catch (RetrofitError e) {
                    Utils.showToastOnUIThread(this, getString(R.string.failure_uploading_public_key));
                    Utils.log(e, "Failure uploading public key");
                }
            } catch (NoSuchAlgorithmException e) {
                Utils.log(e);
            }
            return false;
        }
        return true;
    }
}
