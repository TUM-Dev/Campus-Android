package de.tum.in.tumcampus.activities.wizzard;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;

public class WizNavCheckTokenActivity extends ActivityForAccessingTumOnline {

	public WizNavCheckTokenActivity() {
		super("isTokenConfirmed", R.layout.activity_wiznav_checktoken);
	}

	@Override
	public void onBackPressed() {
        finish();
        startActivity(new Intent(this, WizNavStartActivity.class));
	}

	public void onClickNext(View view) {
		super.requestFetch();
	}

	public void onClickSkip(View view) {
		startNextActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onFetch(String rawResponse) {
		if (rawResponse.contains("true")) {
			startNextActivity();
		} else if (rawResponse.contains("false")) {
			Toast.makeText(this, R.string.token_not_enabled, Toast.LENGTH_SHORT)
					.show();
		}
		progressLayout.setVisibility(View.GONE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		TextView textView = (TextView) findViewById(R.id.tvBrowse);
		textView.setClickable(true);
		textView.setMovementMethod(LinkMovementMethod.getInstance());
		String url = "<a href='http://campus.tum.de'>TUMOnline</a>";
		textView.setText(Html.fromHtml(url));
	}

	private void startNextActivity() {
		finish();
		startActivity(new Intent(this, WizNavExtrasActivity.class));
	}
}
