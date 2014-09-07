package de.tum.in.tumcampus.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForSearching;
import de.tum.in.tumcampus.adapters.PersonListAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.PersonSearchSuggestionProvider;
import de.tum.in.tumcampus.models.Person;
import de.tum.in.tumcampus.models.PersonList;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequestFetchListener;

/**
 * Activity to search for employees.
 */
public class PersonsSearchActivity extends ActivityForSearching implements TUMOnlineRequestFetchListener {
    private static final String PERSONEN_SUCHE = "personenSuche";
    private static final String P_SUCHE = "pSuche";

    /**
     * List to display the results
     */
    private ListView lvPersons;
    private RelativeLayout failedTokenLayout;
    private RelativeLayout noTokenLayout;

    public PersonsSearchActivity() {
        super(R.layout.activity_persons, PersonSearchSuggestionProvider.AUTHORITY,3);
    }

    /**
     * Displays the employees searched for.
     *
     * @param persons The search results enriched with some additional information.
     */
    private void displayResults(List<Person> persons) {
        final ListView lvStaff = (ListView) findViewById(R.id.lstPersons);

        lvStaff.setAdapter(new PersonListAdapter(this, persons));

        lvStaff.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {
                Object listViewItem = lvStaff.getItemAtPosition(position);
                Person person = (Person) listViewItem;

                // store selected person ID in bundle to get in in StaffDetails
                Bundle bundle = new Bundle();
                bundle.putSerializable("personObject", person);

                // show detailed information in new activity
                Intent intent = new Intent(PersonsSearchActivity.this, PersonsDetailsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lvPersons = (ListView) findViewById(R.id.lstPersons);
        failedTokenLayout = (RelativeLayout) findViewById(R.id.failed_layout);
        noTokenLayout = (RelativeLayout) findViewById(R.id.no_token_layout);

        //Counting the number of times that the user used this activity for intelligent reordering
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPrefs.getBoolean("implicitly_id", true)) {
            ImplicitCounter.Counter("person_search_id", getApplicationContext());
        }

        onNewIntent(getIntent());
    }

    @Override
    public void performEmptyQuery() {
        lvPersons.setAdapter(null);
    }

    @Override
    public void performSearchAlgorithm(String query) {
        TUMOnlineRequest requestHandler = new TUMOnlineRequest(PERSONEN_SUCHE, this);
        requestHandler.setParameter(P_SUCHE, query);
        String accessToken = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(Const.ACCESS_TOKEN, null);
        if (accessToken != null) {
            Log.i(getClass().getSimpleName(), "TUMOnline token is <"
                    + accessToken + ">");
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

    /**
     * Handles the XML response from TUMOnline by deserializing the information
     * to model entities.
     *
     * @param rawResponse The XML data from TUMOnline.
     */
    @Override
    public void onFetch(String rawResponse) {
        Log.d(getClass().getSimpleName(), rawResponse);

        // test by sample element "familienname" (required field)
        if (!rawResponse.contains("familienname")) {
            lvPersons.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{"keine Ergebnisse"}));
        }

        Serializer serializer = new Persister();

        // Lists of employees
        PersonList personList;

        // deserialize the XML to model entities
        try {
            personList = serializer.read(PersonList.class, rawResponse);
        } catch (Exception e) {
            Log.d("SIMPLEXML", "wont work: " + e.getMessage());
            progressLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            Toast.makeText(this, R.string.no_search_result, Toast.LENGTH_SHORT).show();
            return;
        }
        displayResults(personList.getPersons());
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
