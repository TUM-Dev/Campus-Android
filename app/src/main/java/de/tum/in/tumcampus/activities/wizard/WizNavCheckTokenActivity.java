package de.tum.in.tumcampus.activities.wizard;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.IdentitySet;
import de.tum.in.tumcampus.models.TokenConfirmation;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;

/**
 *
 */
public class WizNavCheckTokenActivity extends ActivityForLoadingInBackground<Void, Integer> {

	public WizNavCheckTokenActivity() {
		super(R.layout.activity_wiznav_checktoken);
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disableRefresh();
    }

    /**
     * If back key is pressed start previous activity
     */
	@Override
	public void onBackPressed() {
        finish();
        startActivity(new Intent(this, WizNavStartActivity.class));
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	}

    /**
     * Open next activity on skip
     * @param skip Skip button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickSkip(View skip) {
        finish();
        startActivity(new Intent(this, WizNavExtrasActivity.class));
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * If next is pressed, check if token has been activated
     * @param next Next button handle
     */
	@SuppressWarnings("UnusedParameters")
    public void onClickNext(View next) {
        if (!NetUtils.isConnected(this)) {
            showNoInternetLayout();
            return;
        }
		startLoading();
	}

    /**
     * Check in background if token has been enabled and get identity for enabling chat
     * */
    @Override
    protected Integer onLoadInBackground(Void... arg) {
        // Check if token has been enabled
        TUMOnlineRequest<TokenConfirmation> request = new TUMOnlineRequest<TokenConfirmation>(TUMOnlineConst.TOKEN_CONFIRMED, this, true);
        TokenConfirmation confirmation = request.fetch();
        if (confirmation!=null && confirmation.isConfirmed()) {

            // Get users full name
            TUMOnlineRequest<IdentitySet> request2 = new TUMOnlineRequest<IdentitySet>(TUMOnlineConst.IDENTITY, this, true);
            IdentitySet id = request2.fetch();
            if (id == null) {
                return R.string.no_rights_to_access_id;
            }

            // Save the name to preferences
            Utils.setSetting(this, Const.CHAT_ROOM_DISPLAY_NAME, id.toString());
            return null;
        } else {
            if(!NetUtils.isConnected(this))
                return R.string.no_internet_connection;
            else
                return R.string.token_not_enabled;
        }
    }

    /**
     * If everything worked, start the next activity page
     * otherwise give the user the possibility to retry
     * */
    @Override
    protected void onLoadFinished(Integer errorMessageStrResId) {
        if(errorMessageStrResId==null) {
            startNextActivity();
        } else {
            Utils.showToast(this, errorMessageStrResId);
            showLoadingEnded();
        }
    }

    /**
     * Adds clickable link to activity
     */
	@Override
	protected void onStart() {
		super.onStart();
		TextView textView = (TextView) findViewById(R.id.tvBrowse);
		textView.setClickable(true);
		textView.setMovementMethod(LinkMovementMethod.getInstance());
		String url = "<a href='http://campus.tum.de'>TUMOnline</a>";
		textView.setText(Html.fromHtml(url));
	}

    /**
     * Opens next wizard page
     */
	private void startNextActivity() {
		finish();
		startActivity(new Intent(this, WizNavChatActivity.class));
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	}
}
