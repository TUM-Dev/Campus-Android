package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForSearchingTumOnline;
import de.tum.in.tumcampus.adapters.NoResultsAdapter;
import de.tum.in.tumcampus.adapters.PersonListAdapter;
import de.tum.in.tumcampus.auxiliary.PersonSearchSuggestionProvider;
import de.tum.in.tumcampus.models.Person;
import de.tum.in.tumcampus.models.PersonList;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;

/**
 * Activity to search for employees.
 */
public class PersonsSearchActivity extends ActivityForSearchingTumOnline<PersonList> {
    private static final String P_SUCHE = "pSuche";

    /** List to display the results */
    private ListView lvPersons;

    public PersonsSearchActivity() {
        super(TUMOnlineConst.PERSONEN_SUCHE, PersonList.class, R.layout.activity_persons, PersonSearchSuggestionProvider.AUTHORITY, 3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lvPersons = (ListView) findViewById(R.id.lstPersons);

        openSearch();
    }

    @Override
    protected void onStartSearch() {
        lvPersons.setAdapter(null);
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
        if(response==null) {
            lvPersons.setAdapter(new NoResultsAdapter(this));
        } else {
            displayResults(response.getPersons());
        }
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
}
