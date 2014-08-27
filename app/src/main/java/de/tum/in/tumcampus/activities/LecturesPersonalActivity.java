package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.util.Collections;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForSearching;
import de.tum.in.tumcampus.adapters.LecturesListAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.models.LecturesSearchRow;
import de.tum.in.tumcampus.models.LecturesSearchRowSet;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequestFetchListener;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * This activity presents the users' lectures using the TUMOnline web service
 * the results can be filtered by the semester or all shown.
 * 
 * This activity uses the same models as FindLectures.
 * 
 * HINT: a TUMOnline access token is needed
 * 
 * 
 * needed/linked files:
 * 
 * res.layout.mylectures (Layout XML), models.FindLecturesRowSet,
 * models.FindLecturesListAdapter
 *
 * @author Daniel G. Mayr
 */
public class LecturesPersonalActivity extends ActivityForSearching implements TUMOnlineRequestFetchListener {
    private final static String P_SUCHE = "pSuche";

	/** filtered list which will be shown */
	LecturesSearchRowSet lecturesList = null;

	/** UI elements */
	private StickyListHeadersListView lvMyLecturesList;
    private RelativeLayout failedTokenLayout;
    private RelativeLayout noTokenLayout;

    public LecturesPersonalActivity() {
		super(R.layout.activity_lectures);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// bind UI elements
		lvMyLecturesList = (StickyListHeadersListView) findViewById(R.id.lvMyLecturesList);
        failedTokenLayout = (RelativeLayout) findViewById(R.id.failed_layout);
        noTokenLayout = (RelativeLayout) findViewById(R.id.no_token_layout);

        // handle on click events by showing its LectureDetails
        lvMyLecturesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {
                Object o = lvMyLecturesList.getItemAtPosition(position);
                LecturesSearchRow item = (LecturesSearchRow) o;

                // set bundle for LectureDetails and show it
                Bundle bundle = new Bundle();
                // we need the stp_sp_nr
                bundle.putString(LecturesSearchRow.STP_SP_NR, item.getStp_sp_nr());
                Intent intent = new Intent(LecturesPersonalActivity.this, LecturesDetailsActivity.class);
                intent.putExtras(bundle);
                // start LectureDetails for given stp_sp_nr
                startActivity(intent);
            }
        });

        performSearchAlgorithm("");

        //Counting the number of times that the user used this activity for intelligent reordering
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPrefs.getBoolean("implicitly_id", true))
        {
            ImplicitCounter.Counter("my_lectures_id", getApplicationContext());
        }
	}

    @Override
    public void performSearchAlgorithm(String query) {
        TUMOnlineRequest requestHandler;
        if(query.length()<ActivityForSearching.MIN_SEARCH_LENGTH) {
            // If query is empty show my lectures
            requestHandler = new TUMOnlineRequest(Const.LECTURES_PERSONAL, LecturesPersonalActivity.this);
        } else {
            // otherwise search for the lecture
            requestHandler = new TUMOnlineRequest(Const.LECTURES_SEARCH, LecturesPersonalActivity.this);
            requestHandler.setParameter(P_SUCHE, query);
        }

        String accessToken = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(Const.ACCESS_TOKEN, null);
        if (accessToken != null) {
            Log.i(getClass().getSimpleName(), "TUMOnline token is <"+ accessToken + ">");
            noTokenLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
            errorLayout.setVisibility(View.GONE);
            requestHandler.fetchInteractive(this, this);
        } else {
            Log.i(getClass().getSimpleName(), "No token was set");
            noTokenLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCommonError(String errorReason) {
        Toast.makeText(this, errorReason, Toast.LENGTH_SHORT).show();
        progressLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFetch(String rawResponse) {
        // deserialize the XML
        Serializer serializer = new Persister();
        try {
            lecturesList = serializer.read(LecturesSearchRowSet.class, rawResponse);
        } catch (Exception e) {
            Log.d("SIMPLEXML", "wont work: " + e.getMessage());
            progressLayout.setVisibility(View.GONE);
            failedTokenLayout.setVisibility(View.VISIBLE);
            e.printStackTrace();
            return;
        }

        if (lecturesList == null) {
            // no results found
            //TODO view no results
            lvMyLecturesList.setAdapter(null);
            return;
        }

        // Sort lectures by semester id
        List<LecturesSearchRow> lectures = lecturesList.getLehrveranstaltungen();
        Collections.sort(lectures);

        // set ListView to data via the LecturesListAdapter
        lvMyLecturesList.setAdapter(new LecturesListAdapter(LecturesPersonalActivity.this, lectures));
        progressLayout.setVisibility(View.GONE);
    }

    @Override
    public void onFetchCancelled() {
        finish();
    }

    @Override
    public void onFetchError(String errorReason) {
        Log.e(getClass().getSimpleName(), errorReason);
        progressLayout.setVisibility(View.GONE);
        Toast.makeText(this, errorReason, Toast.LENGTH_SHORT).show();

        // TODO Change errors to Exceptions
        // If there is a failed token layout show this
        if (failedTokenLayout != null) {
            failedTokenLayout.setVisibility(View.VISIBLE);
        } else {
            // Else just use the common error layout
            errorLayout.setVisibility(View.VISIBLE);
        }
    }
}
