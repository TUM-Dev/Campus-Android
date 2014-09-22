package de.tum.in.tumcampus.activities.wizard;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;

/**
 *
 */
public class WizNavCheckTokenActivity extends ActivityForAccessingTumOnline {

	public WizNavCheckTokenActivity() {
		super(TUMOnlineConst.TOKEN_CONFIRMED, R.layout.activity_wiznav_checktoken);
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * If back key is pressed start previous activity
     */
	@Override
	public void onBackPressed() {
        finish();
        startActivity(new Intent(this, WizNavStartActivity.class));
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
		requestFetch();
	}

    /**
     * When fetch was successful, start next activity or show error
     * @param rawResponse this will be the raw return of the fetch
     */
	@Override
	public void onFetch(String rawResponse) {
		if (rawResponse.contains("true")) {
			startNextActivity();
		} else if (rawResponse.contains("false")) {
            Utils.showToast(this, R.string.token_not_enabled);
		}
		showLoadingEnded();
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
		startActivity(new Intent(this, WizNavExtrasActivity.class));
	}
}
