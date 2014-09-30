package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForSearchingTumOnline;
import de.tum.in.tumcampus.adapters.NoResultsAdapter;
import de.tum.in.tumcampus.auxiliary.PersonSearchSuggestionProvider;
import de.tum.in.tumcampus.models.Person;
import de.tum.in.tumcampus.models.PersonList;
import de.tum.in.tumcampus.models.managers.RecentsManager;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;

/**
 * Activity to search for employees.
 */
public class PersonsSearchActivity extends ActivityForSearchingTumOnline<PersonList> implements OnItemClickListener {
    private static final String P_SUCHE = "pSuche";

    /** List to display the results */
    private ListView lvPersons;
    private RecentsManager recentsManager;
    private SimpleCursorAdapter recentsAdapter;

    public PersonsSearchActivity() {
        super(TUMOnlineConst.PERSON_SEARCH, R.layout.activity_persons, PersonSearchSuggestionProvider.AUTHORITY, 3);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lvPersons = (ListView) findViewById(R.id.lstPersons);

        // get all stations from db
        recentsManager = new RecentsManager(this, RecentsManager.PERSONS);


        // Initialize stations adapter
        Cursor personsCursor = recentsManager.getAllFromDb();
        recentsAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, personsCursor,
                personsCursor.getColumnNames(), new int[]{android.R.id.text1});

        if(mQuery==null) {
            if(recentsAdapter.getCount()==0) {
                openSearch();
            } else {
                lvPersons.setAdapter(recentsAdapter);
                lvPersons.setOnItemClickListener(this);
                lvPersons.requestFocus();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> a, View v, int position, long id) {
        Person person = (Person) lvPersons.getItemAtPosition(position);

        // store selected person ID in bundle to get in in StaffDetails
        Bundle bundle = new Bundle();
        bundle.putSerializable("personObject", person);

        // show detailed information in new activity
        Intent intent = new Intent(this, PersonsDetailsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);

        recentsManager.replaceIntoDb(person.toString());
    }

    @Override
    protected void onStartSearch() {
        Cursor personsCursor = recentsManager.getAllFromDb();
        recentsAdapter.changeCursor(personsCursor);
        lvPersons.setAdapter(recentsAdapter);
        lvPersons.setOnItemClickListener(this);
    }

    @Override
    public void onStartSearch(String query) {
        requestHandler.setParameter(P_SUCHE, query);
        requestFetch();
    }

    /**
     * Handles the XML response from TUMOnline by de-serializing the information
     * to model entities.
     *
     * @param response The de-serialized data from TUMOnline.
     */
    @Override
    public void onLoadFinished(PersonList response) {
        if(response.getPersons()==null) {
            lvPersons.setAdapter(new NoResultsAdapter(this));
            lvPersons.setOnItemClickListener(null);
        } else {
            ArrayAdapter<Person> adapter = new ArrayAdapter<Person>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, response.getPersons());
            lvPersons.setAdapter(adapter);
            lvPersons.setOnItemClickListener(this);
        }
    }
}
