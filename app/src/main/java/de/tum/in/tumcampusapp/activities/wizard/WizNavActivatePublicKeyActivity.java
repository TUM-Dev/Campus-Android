package de.tum.in.tumcampusapp.activities.wizard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.models.TUMCabeClient;
import de.tum.in.tumcampusapp.models.ChatMember;
import de.tum.in.tumcampusapp.models.ChatRoom;
import de.tum.in.tumcampusapp.models.ChatVerification;
import de.tum.in.tumcampusapp.models.managers.ChatRoomManager;
import retrofit.RetrofitError;

/**
 *
 */
public class WizNavActivatePublicKeyActivity extends ActivityForLoadingInBackground<Void, Boolean> {

    private boolean tokenSetup = false;

    public WizNavActivatePublicKeyActivity() {
        super(R.layout.activity_wiznav_activate_key);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        disableRefresh();

        TextView status = (TextView) findViewById(R.id.tvSucc);
        ChatMember member = Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class);
        status.setText(String.format(getString(R.string.public_key_mail), member.getLrzId()));

        // If called because app version changed remove "Step 4" and close on back pressed
        Intent i = getIntent();
        if (i != null && i.hasExtra(Const.TOKEN_IS_SETUP)) {
            tokenSetup = i.getBooleanExtra(Const.TOKEN_IS_SETUP, false);
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
        startActivity(new Intent(this, WizNavChatActivity.class));
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
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
        ChatRoomManager manager = new ChatRoomManager(this);

        // Get member and private key from settings
        ChatMember member = Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class);
        if (member == null)
            return false;

        // Try to restore already joined chat rooms from server
        try {
            List<ChatRoom> rooms = TUMCabeClient.getInstance(this).getMemberRooms(member.getId(), new ChatVerification(this, member));
            manager.replaceIntoRooms(rooms);

            //Store that this key was activated
            Utils.setInternalSetting(this, Const.PRIVATE_KEY_ACTIVE, true);

            return true;
        } catch (RetrofitError e) {
            Utils.log(e);
        } catch (NoPrivateKey e){
            Utils.log(e);
        }
        return false;
    }

    @Override
    protected void onLoadFinished(Boolean result) {
        if (result) {
            startNextActivity();
        } else {
            showLoadingEnded();
            Utils.showToast(this, R.string.key_not_activated);
        }
    }
}
