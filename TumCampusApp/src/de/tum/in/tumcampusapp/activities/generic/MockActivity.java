package de.tum.in.tumcampusapp.activities.generic;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import de.tum.in.tumcampus.R;

/**
 * Mock Acitivy for test purpose only.
 * 
 * @author Sascha Moecker
 * 
 */
public class MockActivity extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mock);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
