package de.tum.in.tumcampusapp.activities;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.LecturesSearchListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.LecturesSearchRow;
import de.tum.in.tumcampusapp.models.LecturesSearchRowSet;

/**
 * This activity represents a small find box to query through the TUMOnline web
 * service to find lectures identified by the acquired query string.
 * 
 * A list of all found lectures will be displayed and by clicking on each it
 * will send the lecture number to the LectureDetails Activity.
 * 
 * HINT: a TUMOnline access token is needed
 * 
 * 
 * needed/linked files:
 * 
 * res.layout.findLectures (Layout XML), models.FindLecturesRowSet,
 * models.FindLecturesListAdapter
 * 
 * 
 * @solves [M4] Lehrveranstaltungen suchen
 * @author Daniel Mayr
 * @review Thomas Behrens
 */
public class LecturesSearchActivity extends ActivityForAccessingTumOnline implements OnEditorActionListener {

	private static String P_SUCHE = "pSuche";

	/** UI Elements */
	EditText etFindQuery;
	ListView lvFound;

	public LecturesSearchActivity() {
		super(Const.LECTURES_SEARCH, R.layout.activity_lecturessearch);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int viewId = view.getId();
		switch (viewId) {
		case R.id.activity_lecturesearch_clear:
			etFindQuery.setText("");
			break;
		case R.id.activity_lecturesearch_dosearch:
			searchForLectures();
			break;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// bind GUI elements
		etFindQuery = (EditText) findViewById(R.id.etFindQuery);
		etFindQuery.setOnEditorActionListener(this);
		lvFound = (ListView) findViewById(R.id.lvFound);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		return searchForLectures();
	}

	@Override
	public void onFetch(String rawResponse) {
		Log.d("RESPONSE", rawResponse);

		// deserialize the xml output
		// we use simpleXML for this by providing a
		// class which represents the xml-schema
		Serializer serializer = new Persister();
		LecturesSearchRowSet lecturesList = null;

		try {
			lecturesList = serializer.read(LecturesSearchRowSet.class, rawResponse);

		} catch (Exception e) {
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
			Toast.makeText(this, R.string.no_search_result, Toast.LENGTH_SHORT).show();
		}

		if (lecturesList == null) {
			// no results found
			return;
		}

		// make some customizations to the ListView
		// provide data via the FindLecturesListAdapter
		lvFound.setAdapter(new LecturesSearchListAdapter(this, lecturesList.getLehrveranstaltungen()));

		// deal with clicks on items in the ListView
		lvFound.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("static-access")
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				// each item represents the current FindLecturesRow
				// item
				Object o = lvFound.getItemAtPosition(position);
				LecturesSearchRow item = (LecturesSearchRow) o;

				// bundle data for the LectureDetails Activity
				Bundle bundle = new Bundle();
				bundle.putString(item.STP_SP_NR, item.getStp_sp_nr());
				Intent i = new Intent(LecturesSearchActivity.this, LecturesDetailsActivity.class);
				i.putExtras(bundle);
				// load LectureDetails
				startActivity(i);
			}
		});
		progressLayout.setVisibility(View.GONE);
	};

	private boolean searchForLectures() {
		if (etFindQuery.getText().length() < 3) {
			Toast.makeText(this, R.string.please_insert_at_least_three_chars, Toast.LENGTH_SHORT).show();
			return false;
		}
		// set the query string as parameter for the TUMOnline request
		requestHandler.setParameter(P_SUCHE, etFindQuery.getText().toString());
		Utils.hideKeyboard(this, etFindQuery);

		// do the TUMOnline request (implement listener here)
		super.requestFetch();
		return true;
	}
}
