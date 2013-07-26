package de.tum.in.tumcampusapp.activities.wizzard;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;

public class WizNavCheckTokenActivity extends ActivityForAccessingTumOnline {

	public WizNavCheckTokenActivity() {
		super("isTokenConfirmed", R.layout.activity_wiznav_checktoken);
	}

	@Override
	public void onBackPressed() {
		startPreviousActivity();
	}

	public void onClickNext(View view) {
		super.requestFetch();
	}

	public void onClickSkip(View view) {
		startNextActivity();
	}

	public void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_activity_wizzard, menu);
		return true;
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_exit:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.pager_title_strip);
	}

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
		Intent intent = new Intent(this, WizNavExtrasActivity.class);
		startActivity(intent);
	}

	private void startPreviousActivity() {
		finish();
		Intent intent = new Intent(this, WizNavStartActivity.class);
		startActivity(intent);
	}
}
