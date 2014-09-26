package de.tum.in.tumcampus.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.internal.js;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.TUMOnlineCacheManager;

/**
 * Activity to fetch and display the curricula of different study programs.
 */
public class CurriculaActivity extends ActionBarActivity implements OnItemClickListener {
    public static final String NAME = "name";
    public static final String URL = "url";

    private Hashtable<String, String> options;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);
        setContentView(R.layout.activity_curricula);

        // Sets the adapter
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        ListView list = (ListView) this.findViewById(R.id.activity_curricula_list_view);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(this);

        // Fetch all curricula from webservice
        // TODO create a nicer access class with caching for api access
        this.options = new Hashtable<String, String>();
        final TUMOnlineCacheManager t = new TUMOnlineCacheManager(this.getApplicationContext());
        final Context c = this.getApplicationContext();
        new AsyncTask<Void, Void, JSONArray>() {

            @Override
            protected JSONArray doInBackground(Void... v) {

                try {
                    return Utils.downloadJsonArray(c, "https://tumcabe.in.tum.de/Api/curricula");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(JSONArray jsonData) {

                try {
                    for (int i = 0; i < jsonData.length(); i++) {
                        JSONObject item = jsonData.getJSONObject(i);

                        arrayAdapter.add(item.getString("name"));
                        options.put(item.getString("name"),item.getString("url"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }.execute();

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
        intent.putExtra(URL, this.options.get(curriculumName));
        intent.putExtra(NAME, curriculumName);
        this.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
