package de.tum.in.tumcampusapp.activities;

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

public class WizNavCheckTokenActivity extends ActivityForAccessingTumOnline {

	public WizNavCheckTokenActivity() {
		super("isTokenConfirmed", R.layout.activity_wiznav_checktoken);
	}

	public void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		super.onCreate(savedInstanceState);
	}

	protected void onStart() {
		super.onStart();
		TextView textView = (TextView) findViewById(R.id.tvBrowse);
		textView.setClickable(true);
		textView.setMovementMethod(LinkMovementMethod.getInstance());
		String text = "<a href='http://campus.tum.de'> Enable Token through TUM campus portal using Token-Management </a>";
		textView.setText(Html.fromHtml(text));
	}

	public void onClickNext(View view) {
		super.requestFetch();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_activity_wizzard, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_exit:
			finish();
			Intent intent = new Intent(this, StartActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onFetch(String rawResponse) {
		if (rawResponse.contains("true")) {
			finish();
			Intent intent = new Intent(this, WizNavColorActivity.class);
			startActivity(intent);

		} else if (rawResponse.contains("false")) {
			Toast.makeText(this, R.string.token_not_enabled, Toast.LENGTH_SHORT)
					.show();
		}
		progressLayout.setVisibility(View.GONE);
	}

	@Override
	public void onBackPressed() {
		finish();
		Intent intent = new Intent(this, WizNavStartActivity.class);
		startActivity(intent);
	}
}
