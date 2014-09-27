package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Activity to fetch and display the curricula of different study programs.
 */
public class CurriculaActivity extends ActivityForLoadingInBackground<Void,JSONArray> implements OnItemClickListener {
    public static final String NAME = "name";
    public static final String URL = "url";

    private Hashtable<String, String> options;
    private ArrayAdapter<String> arrayAdapter;

    public CurriculaActivity() {
        super(R.layout.activity_curricula);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets the adapter
        ListView list = (ListView) this.findViewById(R.id.activity_curricula_list_view);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(this);

        // Fetch all curricula from webservice
        startLoading();
    }

    @Override
    protected JSONArray onLoadInBackground(Void... arg) {
        return Utils.downloadJsonArray(this, "https://tumcabe.in.tum.de/Api/curricula", false);
    }

    @Override
    protected void onLoadFinished(JSONArray jsonData) {
        try {
            options = new Hashtable<String, String>();
            for (int i = 0; i < jsonData.length(); i++) {
                JSONObject item = jsonData.getJSONObject(i);

                arrayAdapter.add(item.getString("name"));
                options.put(item.getString("name"),item.getString("url"));
            }
        } catch (JSONException e) {
            Utils.log(e);
        }
    }

    /**
     * Handle click on curricula item
     *
     * @param parent Containing listView
     * @param view   Item view
     * @param pos    Index of item
     * @param id     Id of item
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        String curriculumName = ((TextView) view).getText().toString();

        // Puts URL and name into an intent and starts the detail view
        Intent intent = new Intent(this, CurriculaDetailsActivity.class);
        intent.putExtra(URL, options.get(curriculumName));
        intent.putExtra(NAME, curriculumName);
        this.startActivity(intent);
    }
}
