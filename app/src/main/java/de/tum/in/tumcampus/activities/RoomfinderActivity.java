package de.tum.in.tumcampus.activities;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForSearching;
import de.tum.in.tumcampus.adapters.RoomFinderListAdapter;
import de.tum.in.tumcampus.auxiliary.RoomFinderSuggestionProvider;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequest;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequestFetchListener;

/**
 * Activity to show a convenience interface for using the MyTUM room finder.
 *
 * @author Vincenz Doelle, Anas Chackfeh
 */
public class RoomfinderActivity extends ActivityForSearching implements OnEditorActionListener, TUMRoomFinderRequestFetchListener, OnItemClickListener {

    // HTTP client for sending requests to MyTUM roomfinder
    TUMRoomFinderRequest roomFinderRequest;

    ListView list;
    RoomFinderListAdapter adapter;

    String currentlySelectedBuildingId;
    private String currentlySelectedRoomId;

    public RoomfinderActivity() {
        super(R.layout.activity_roomfinder);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomFinderRequest = new TUMRoomFinderRequest();

        //Counting the number of times that the user used this activity for intelligent reordering
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPrefs.getBoolean("implicitly_id", true)) {
            ImplicitCounter.Counter("roomfinder_id", getApplicationContext());
        }

        //Handle intents
        handleIntent(getIntent());
    }

    @Override
    public boolean performSearchAlgorithm() {
        EditText searchString = (EditText) this.findViewById(R.id.search_field);
        String query = searchString.getText().toString();

        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, RoomFinderSuggestionProvider.AUTHORITY, RoomFinderSuggestionProvider.MODE);
        suggestions.saveRecentQuery(query.toString(), null);

        roomFinderRequest.fetchSearchInteractive(this, this, query);
        return true;
    }

    @Override
    public void onFetch(ArrayList<HashMap<String, String>> result) {
        list = (ListView) findViewById(R.id.list);

        // Getting adapter by passing xml data ArrayList
        adapter = new RoomFinderListAdapter(this, result);
        list.setAdapter(adapter);

        // Click event for single list row
        list.setOnItemClickListener(this);

        progressLayout.setVisibility(View.GONE);
        if (result.size() == 0) {
            Toast.makeText(this, R.string.no_rooms_found, Toast.LENGTH_SHORT).show();
            errorLayout.setVisibility(View.VISIBLE);
            return;
        }
        errorLayout.setVisibility(View.GONE);
    }

    @Override
    public void onFetchCancelled() {
        onFetchError("");
    }

    @Override
    public void onFetchDefaultMapId(String mapId) {
        Intent intent = new Intent(this, RoomFinderDetailsActivity.class);
        intent.putExtra("buildingId", currentlySelectedBuildingId);
        intent.putExtra("roomId", currentlySelectedRoomId);
        intent.putExtra("mapId", mapId);

        startActivity(intent);
    }

    @Override
    public void onFetchError(String errorReason) {
        roomFinderRequest.cancelRequest(true);
        errorLayout.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        @SuppressWarnings("unchecked")
        HashMap<String, String> room = (HashMap<String, String>) list.getAdapter().getItem(position);

        currentlySelectedBuildingId = room.get(TUMRoomFinderRequest.KEY_Building + TUMRoomFinderRequest.KEY_ID);
        currentlySelectedRoomId = room.get(TUMRoomFinderRequest.KEY_ARCHITECT_NUMBER);
        roomFinderRequest.fetchDefaultMapIdJob(this, this, currentlySelectedBuildingId);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
    }

    @Override
    public void onCommonError(String errorReason) {
        Toast.makeText(this, errorReason, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        this.handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getAction().equals(Intent.ACTION_SEARCH)) {
            //Get the requested room
            String query = intent.getStringExtra(SearchManager.QUERY);

            //Update search field
            EditText searchView = (EditText) this.findViewById(R.id.search_field);
            searchView.setText(query);

            //Execute search
            performSearchAlgorithm();
        }
    }


}