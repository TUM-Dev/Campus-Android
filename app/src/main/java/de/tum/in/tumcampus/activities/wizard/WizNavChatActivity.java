package de.tum.in.tumcampus.activities.wizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.widget.CheckBox;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatClient;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatPublicKey;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.ChatVerification;
import de.tum.in.tumcampus.models.LecturesSearchRow;
import de.tum.in.tumcampus.models.LecturesSearchRowSet;
import de.tum.in.tumcampus.models.managers.ChatRoomManager;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;
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

            // Get all of the users lectures and save them as possible chat rooms
            ChatRoomManager manager = new ChatRoomManager(this);
            TUMOnlineRequest<LecturesSearchRowSet> requestHandler = new TUMOnlineRequest<LecturesSearchRowSet>(TUMOnlineConst.LECTURES_PERSONAL, this, true);
            LecturesSearchRowSet lecturesList = requestHandler.fetch();
            if (lecturesList != null) {
                List<LecturesSearchRow> lectures = lecturesList.getLehrveranstaltungen();
                manager.replaceInto(lectures);
            } else {
                Utils.showToastOnUIThread(this, R.string.no_rights_to_access_lectures);
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
                member = ChatClient.getInstance(this).createMember(currentChatMember);

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
            PrivateKey privateKey = generatePrivateKey(member);
            if(privateKey==null)
                return false;

            // Try to restore already joined chat rooms from server
            try {
                List<ChatRoom> rooms = ChatClient.getInstance(this).getMemberRooms(currentChatMember.getId(), new ChatVerification(privateKey, currentChatMember));
                manager.replaceIntoRooms(rooms);
            } catch (RetrofitError e) {
                Utils.log(e);
                return false;
            }
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
    private PrivateKey generatePrivateKey(ChatMember member) {
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
                    return keyPair.getPrivate();
                } catch (RetrofitError e) {
                    Utils.showToastOnUIThread(this, getString(R.string.failure_uploading_public_key));
                    Utils.log(e, "Failure uploading public key");
                }
            } catch (NoSuchAlgorithmException e) {
                Utils.log(e);
            }
        } else {
            // If the key is already generated, retrieve it from shared preferences
            byte[] privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT);
            KeyFactory keyFactory;
            try {
                keyFactory = KeyFactory.getInstance("RSA");
                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                return keyFactory.generatePrivate(privateKeySpec);
            } catch (NoSuchAlgorithmException e) {
                Utils.log(e);
            } catch (InvalidKeySpecException e) {
                Utils.log(e);
            }
        }
        return null;
    }
}
