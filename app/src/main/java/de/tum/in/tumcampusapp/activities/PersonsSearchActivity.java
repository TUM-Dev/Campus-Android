package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForSearchingTumOnline;
import de.tum.in.tumcampusapp.adapters.NoResultsAdapter;
import de.tum.in.tumcampusapp.auxiliary.PersonSearchSuggestionProvider;
import de.tum.in.tumcampusapp.managers.RecentsManager;
import de.tum.in.tumcampusapp.models.tumo.Person;
import de.tum.in.tumcampusapp.models.tumo.PersonList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;

/**
 * Activity to search for employees.
 */
public class PersonsSearchActivity extends ActivityForSearchingTumOnline<PersonList> implements OnItemClickListener {
    private static final String P_SUCHE = "pSuche";

    /**
     * List to display the results
     */
    private ListView lvPersons;
    private RecentsManager recentsManager;

    public PersonsSearchActivity() {
        super(TUMOnlineConst.Companion.getPERSON_SEARCH(), R.layout.activity_persons, PersonSearchSuggestionProvider.AUTHORITY, 3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lvPersons = findViewById(R.id.lstPersons);
        lvPersons.setOnItemClickListener(this);

        // get all stations from db
        recentsManager = new RecentsManager(this, RecentsManager.PERSONS);

        // Initialize persons adapter
        ArrayList<Person> list;
        ArrayAdapter<Person> adapter;
        try (Cursor personsCursor = recentsManager.getAllFromDb()) {
            list = new ArrayList<>(personsCursor.getCount());
            if (personsCursor.moveToFirst()) {
                do {
                    String recent = personsCursor.getString(0);
                    String[] t = recent.split("\\$");
                    Person p = new Person();
                    p.setId(t[0]);
                    p.setName(t[1]);
                    list.add(p);
                } while (personsCursor.moveToNext());
            }
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, list);

        if (adapter.getCount() == 0) {
            openSearch();
        } else {
            lvPersons.setAdapter(adapter);
            lvPersons.setOnItemClickListener(this);
            lvPersons.requestFocus();
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

        recentsManager.replaceIntoDb(person.getId() + "$" + person.toString()
                                                                  .trim());
    }

    @Override
    protected void onStartSearch() {
        ArrayList<Person> list;
        try (Cursor personsCursor = recentsManager.getAllFromDb()) {
            if (personsCursor.getCount() == 0) {
                finish();
                return;
            }

            list = new ArrayList<>(personsCursor.getCount());
            if (personsCursor.moveToFirst()) {
                do {
                    String recent = personsCursor.getString(0);
                    String[] t = recent.split("\\$");
                    Person p = new Person();
                    p.setId(t[0]);
                    p.setName(t[1]);
                    list.add(p);
                } while (personsCursor.moveToNext());
            }
        }
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, list);
        lvPersons.setAdapter(adapter);
    }

    @Override
    public void onStartSearch(String query) {
        requestHandler.setParameter(P_SUCHE, query);
        requestFetch();
    }

    private void proceedToPersonDetails(PersonList response )    {
        lvPersons.setAdapter(null);
        Bundle bundle = new Bundle();
        bundle.putSerializable("personObject", response.getPersons().get(0));
        Intent intent = new Intent(this, PersonsDetailsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * Handles the XML response from TUMOnline by de-serializing the information
     * to model entities.
     *
     * @param response The de-serialized data from TUMOnline.
     */
    @Override
    public void onLoadFinished(PersonList response) {
        if (response.getPersons() == null || response.getPersons().isEmpty()) {
            lvPersons.setAdapter(new NoResultsAdapter(this));
        } else if (response.getPersons().size() == 1){
            proceedToPersonDetails(response);
        } else {
            ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, response.getPersons());
            lvPersons.setAdapter(adapter);
        }
    }
}
