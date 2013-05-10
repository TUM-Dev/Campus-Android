package de.tum.in.tumcampusapp.activities;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.ExamListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.ExamList;
import de.tum.in.tumcampusapp.preferences.UserPreferencesActivity;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequestFetchListener;

/**
 * Activity to show the user's grades/exams passed.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
public class GradesActivity extends Activity implements TUMOnlineRequestFetchListener {
	public final static int MENU_REFRESH = 0;
	private String accessToken;

	private RelativeLayout errorLayout;

	/**
	 * List with all exams passed (including grades)
	 */
	private ExamList examList;
	private RelativeLayout failedLayout;
	/**
	 * List view to display all exams/grades
	 */
	private ListView lvGrades;
	private RelativeLayout noTokenLayout;
	private RelativeLayout progressLayout;
	/**
	 * HTTP request handler to handle requests to TUMOnline
	 */
	private TUMOnlineRequest requestHandler;

	/** Fetches all grades from TUMOnline. */
	public void fetchGrades() {
		requestHandler.fetchInteractive(this, this);
	}

	public void onClick(View view) {
		int viewId = view.getId();
		switch (viewId) {
		case R.id.activity_grades_failed_layout:
			failedLayout.setVisibility(View.GONE);
			requestFetchGrades();
			break;
		case R.id.activity_grades_no_token_layout:
			Intent intent = new Intent(this, UserPreferencesActivity.class);
			startActivity(intent);
			break;
		}
	}

	@Override
	public void onCommonError(String errorReason) {
		Toast.makeText(this, errorReason, Toast.LENGTH_SHORT).show();
		progressLayout.setVisibility(View.GONE);
		errorLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grades);

		lvGrades = (ListView) findViewById(R.id.lstGrades);
		progressLayout = (RelativeLayout) findViewById(R.id.activity_grades_progress_layout);
		failedLayout = (RelativeLayout) findViewById(R.id.activity_grades_failed_layout);
		noTokenLayout = (RelativeLayout) findViewById(R.id.activity_grades_no_token_layout);
		errorLayout = (RelativeLayout) findViewById(R.id.activity_grades_error_layout);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_REFRESH, 0, getString(R.string.update));
		return true;
	}

	/**
	 * Handle the response by deserializing it into model entities.
	 * 
	 * @param rawResp
	 */
	@Override
	public void onFetch(String rawResp) {

		Serializer serializer = new Persister();
		examList = null;

		try {
			// Deserializes XML response
			examList = serializer.read(ExamList.class, rawResp);

			// Displays results in view
			lvGrades.setAdapter(new ExamListAdapter(GradesActivity.this, examList.getExams()));
			progressLayout.setVisibility(View.GONE);

		} catch (Exception e) {
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			progressLayout.setVisibility(View.GONE);
			failedLayout.setVisibility(View.VISIBLE);
			e.printStackTrace();
		}
	}

	@Override
	public void onFetchCancelled() {
		finish();
	}

	@Override
	public void onFetchError(String errorReason) {
		Log.e(getClass().getSimpleName(), errorReason);
		progressLayout.setVisibility(View.GONE);
		failedLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			// Downloads latest news
			requestFetchGrades();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		requestFetchGrades();
	}

	public void requestFetchGrades() {
		requestHandler = new TUMOnlineRequest(Const.NOTEN, this);
		accessToken = PreferenceManager.getDefaultSharedPreferences(this).getString(Const.ACCESS_TOKEN, null);
		if (accessToken != null) {
			Log.i(getClass().getSimpleName(), "TUMOnline token is <" + accessToken + ">");
			noTokenLayout.setVisibility(View.GONE);
			progressLayout.setVisibility(View.VISIBLE);
			fetchGrades();
		} else {
			Log.i(getClass().getSimpleName(), "No token was set");
			noTokenLayout.setVisibility(View.VISIBLE);
		}
	}
}
