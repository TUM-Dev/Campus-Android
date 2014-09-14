package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

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
public class RoomFinderActivity extends ActivityForSearching implements TUMRoomFinderRequestFetchListener, OnItemClickListener {

    // HTTP client for sending requests to MyTUM roomfinder
    TUMRoomFinderRequest roomFinderRequest;

    ListView list;
    RoomFinderListAdapter adapter;

    String currentlySelectedBuildingId;
    private String currentlySelectedRoomId;

    public RoomFinderActivity() {
        super(R.layout.activity_roomfinder, RoomFinderSuggestionProvider.AUTHORITY,3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomFinderRequest = new TUMRoomFinderRequest();

        openSearch();

        onNewIntent(getIntent());
    }

    @Override
    protected void onStartSearch() {
        list.setAdapter(null);
    }

    @Override
    public void onStartSearch(String query) {
        roomFinderRequest.fetchSearchInteractive(this, this, query);
    }

    @Override
    public void onFetch(ArrayList<HashMap<String, String>> result) {
        list = (ListView) findViewById(R.id.list);

        // Getting adapter by passing xml data ArrayList
        adapter = new RoomFinderListAdapter(this, result);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        if (result.size() == 0) {
            showError(R.string.no_rooms_found);
        } else {
            showLoadingEnded();
        }
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
        showError(errorReason);
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
    public void onCommonError(String errorReason) {
        showError(errorReason);
    }
}