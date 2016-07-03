package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.CacheManager;

/**
 * Activity to fetch and display the curricula of different study programs.
 */
public class CurriculaActivity extends ActivityForLoadingInBackground<Void, Optional<JSONArray>> implements OnItemClickListener {
    public static final String NAME = "name";
    public static final String URL = "url";

    public static final String CURRICULA_URL = "https://tumcabe.in.tum.de/Api/curricula";

    private Map<String, String> options;
    private ArrayAdapter<String> arrayAdapter;
    private NetUtils net;

    public CurriculaActivity() {
        super(R.layout.activity_curricula);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        net = new NetUtils(this);
        // Sets the adapter
        ListView list = (ListView) this.findViewById(R.id.activity_curricula_list_view);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(this);

        // Fetch all curricula from webservice via parent async class
        this.startLoading();
    }

    @Override
    protected Optional<JSONArray> onLoadInBackground(Void... arg) {
        return net.downloadJsonArray(CURRICULA_URL, CacheManager.VALIDITY_ONE_MONTH, false);
    }

    @Override
    protected void onLoadFinished(Optional<JSONArray> jsonData) {
        if (!jsonData.isPresent()) {
            if (NetUtils.isConnected(this)) {
                showErrorLayout();
            } else {
                showNoInternetLayout();
            }
            return;
        }
        JSONArray arr = jsonData.get();
        try {
            options = new HashMap<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);

                arrayAdapter.add(item.getString("name"));
                options.put(item.getString("name"), item.getString("url"));
            }
        } catch (JSONException e) {
            Utils.log(e);
        }
        showLoadingEnded();
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
