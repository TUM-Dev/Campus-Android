package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

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

    public PersonsSearchActivity() {
        super(TUMOnlineConst.PERSON_SEARCH, R.layout.activity_persons, PersonSearchSuggestionProvider.AUTHORITY, 3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lvPersons = (ListView) findViewById(R.id.lstPersons);
        lvPersons.setOnItemClickListener(this);

        // get all stations from db
        recentsManager = new RecentsManager(this, RecentsManager.PERSONS);

        // Initialize persons adapter
        Cursor personsCursor = recentsManager.getAllFromDb();
        ArrayList<Person> list = new ArrayList<Person>(personsCursor.getCount());
        if(personsCursor.moveToFirst()) {
            do {
                String recent = personsCursor.getString(0);
                String[] t = recent.split("\\$");
                Person p = new Person();
                p.setId(t[0]);
                p.setName(t[1]);
                list.add(p);
            } while(personsCursor.moveToNext());
        }
        personsCursor.close();

        ArrayAdapter<Person> adapter = new ArrayAdapter<Person>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, list);

        if(mQuery==null) {
            if(adapter.getCount()==0) {
                openSearch();
            } else {
                lvPersons.setAdapter(adapter);
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

        recentsManager.replaceIntoDb(person.getId()+"$"+person.toString().trim());
    }

    @Override
    protected void onStartSearch() {
        Cursor personsCursor = recentsManager.getAllFromDb();
        ArrayList<Person> list = new ArrayList<Person>(personsCursor.getCount());
        if(personsCursor.moveToFirst()) {
            do {
                String recent = personsCursor.getString(0);
                String[] t = recent.split("$");
                Person p = new Person();
                p.setId(t[0]);
                p.setName(t[1]);
                list.add(p);
            } while(personsCursor.moveToNext());
        }
        personsCursor.close();

        ArrayAdapter<Person> adapter = new ArrayAdapter<Person>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, list);
        lvPersons.setAdapter(adapter);
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
        } else {
            ArrayAdapter<Person> adapter = new ArrayAdapter<Person>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, response.getPersons());
            lvPersons.setAdapter(adapter);
        }
    }
}
