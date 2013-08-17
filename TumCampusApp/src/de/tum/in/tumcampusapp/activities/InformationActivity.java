package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Dialogs;
import de.tum.in.tumcampusapp.auxiliary.Utils;

public class InformationActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu_activity_information, menu);
		MenuItem pieMenuItem = menu.findItem(R.id.pieChart);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/* Create the Intent */
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		
		/* Fill it with Data */
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getString(R.string.feedbackAddr)});
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.feedbackSubj));

		/* Send it off to the Activity-Chooser */
		startActivity(Intent.createChooser(emailIntent, "Send mail..."));
		return true;
	}
}
