package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForSearching;
import de.tum.in.tumcampus.adapters.NoResultsAdapter;
import de.tum.in.tumcampus.adapters.RoomFinderListAdapter;
import de.tum.in.tumcampus.auxiliary.RoomFinderSuggestionProvider;
import de.tum.in.tumcampus.models.managers.RecentsManager;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequest;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequestFetchListener;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Activity to show a convenience interface for using the MyTUM room finder.
 */
public class RoomFinderActivity extends ActivityForSearching implements TUMRoomFinderRequestFetchListener, OnItemClickListener {

    // HTTP client for sending requests to MyTUM roomFinder
    private TUMRoomFinderRequest roomFinderRequest;

    private RecentsManager recentsManager;
    private StickyListHeadersListView list;
    private RoomFinderListAdapter adapter;

    private String currentlySelectedBuildingId;
    private String currentlySelectedRoomId;

    public RoomFinderActivity() {
        super(R.layout.activity_roomfinder, RoomFinderSuggestionProvider.AUTHORITY, 3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomFinderRequest = new TUMRoomFinderRequest();

        list = (StickyListHeadersListView) findViewById(R.id.list);

        recentsManager = new RecentsManager(this, RecentsManager.ROOMS);
        adapter = new RoomFinderListAdapter(this, getRecents());

        if (mQuery == null) {
            if (adapter.getCount() == 0) {
                openSearch();
            } else {
                list.setAdapter(adapter);
                list.setOnItemClickListener(this);
            }
        }
    }

    @Override
    protected void onStartSearch() {
        adapter = new RoomFinderListAdapter(this, getRecents());
        list.setAdapter(adapter);
    }

    @Override
    public void onStartSearch(String query) {
        roomFinderRequest.fetchSearchInteractive(this, this, query);
    }

    @Override
    public void onFetch(ArrayList<HashMap<String, String>> result) {
        if (result.size() == 0) {
            list.setAdapter(new NoResultsAdapter(this));
            list.setOnItemClickListener(null);
        } else {
            adapter = new RoomFinderListAdapter(this, result);
            list.setAdapter(adapter);
            list.setOnItemClickListener(this);
            showLoadingEnded();
        }
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

        // Add to recents
        String val = "";
        for (Map.Entry<String, String> entry : room.entrySet()) {
            val += entry.getKey() + "=" + entry.getValue() + ";";
        }
        recentsManager.replaceIntoDb(val);
    }

    @Override
    public void onNoInternetError() {
        showNoInternetLayout();
    }

    /**
     * Reconstruct recents from String
     */
    private ArrayList<HashMap<String, String>> getRecents() {
        Cursor recentStations = recentsManager.getAllFromDb();
        ArrayList<HashMap<String, String>> map = new ArrayList<HashMap<String, String>>(recentStations.getCount());
        if (recentStations.moveToFirst()) {
            do {
                String[] values = recentStations.getString(0).split(";");
                HashMap<String, String> e = new HashMap<String, String>();
                for (String entry : values) {
                    if (entry.isEmpty())
                        continue;
                    String[] kv = entry.split("=");
                    e.put(kv[0], kv[1]);
                }
                map.add(e);
            } while (recentStations.moveToNext());
        }
        recentStations.close();
        return map;
    }
}