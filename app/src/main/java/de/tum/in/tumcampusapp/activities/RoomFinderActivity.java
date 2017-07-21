package de.tum.in.tumcampusapp.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForSearching;
import de.tum.in.tumcampusapp.adapters.NoResultsAdapter;
import de.tum.in.tumcampusapp.adapters.RoomFinderListAdapter;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.RoomFinderSuggestionProvider;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.RecentsManager;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderRoom;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Activity to show a convenience interface for using the MyTUM room finder.
 */
public class RoomFinderActivity extends ActivityForSearching
        implements OnItemClickListener {

    private RecentsManager recentsManager;
    private StickyListHeadersListView list;
    private RoomFinderListAdapter adapter;

    private AsyncTask<String, Void, List<RoomFinderRoom>> searchRoomAsyncTask;

    public RoomFinderActivity() {
        super(R.layout.activity_roomfinder, RoomFinderSuggestionProvider.AUTHORITY, 3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        list = (StickyListHeadersListView) findViewById(R.id.list);
        list.setOnItemClickListener(this);
        recentsManager = new RecentsManager(this, RecentsManager.ROOMS);
        adapter = new RoomFinderListAdapter(this, getRecents());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SearchManager.QUERY)) {
            requestSearch(intent.getStringExtra(SearchManager.QUERY));
            return;
        }

        if (adapter.getCount() == 0) {
            openSearch();
        } else {
            list.setAdapter(adapter);
        }
    }

    @Override
    protected void onStartSearch() {
        List<RoomFinderRoom> recents = getRecents();
        if (recents.isEmpty()) {
            finish();
            return;
        }
        adapter = new RoomFinderListAdapter(this, recents);
        list.setAdapter(adapter);
    }

    @Override
    public void onStartSearch(String query) {
        startLoadSearchResult(query);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TreeMap<String, String> room = new TreeMap<>((HashMap<String, String>) list.getAdapter().getItem(position));

        openRoomDetails(room);
    }

    /**
     * Opens a {@link RoomFinderDetailsActivity} that displays details (e.g. location on a map) for
     * a given room. Also adds this room to the recent queries.
     */
    // TODO: 7/21/2017 Rewrite open details
    private void openRoomDetails(Map<String, String> room) {
        // Add to recents
        StringBuilder val = new StringBuilder();
        Bundle b = new Bundle();
        for (Map.Entry<String, String> entry : room.entrySet()) {
            val.append(entry.getKey()).append('=').append(entry.getValue()).append(';');
            b.putString(entry.getKey(), entry.getValue());
        }
        recentsManager.replaceIntoDb(val.toString());

        // Start detail activity
        Intent intent = new Intent(this, RoomFinderDetailsActivity.class);
        intent.putExtra(RoomFinderDetailsActivity.EXTRA_ROOM_INFO, b);
        startActivity(intent);
    }

    /**
     * Reconstruct recents from String
     */
    // TODO: 7/21/2017 Rewrite getRecents
    private List<RoomFinderRoom> getRecents() {
        Cursor recentStations = recentsManager.getAllFromDb();
        List<RoomFinderRoom> roomList = new ArrayList<>(recentStations.getCount());
        if (recentStations.moveToFirst()) {
            do {
                String[] values = recentStations.getString(0).split(";");
                HashMap<String, String> e = new HashMap<>();
                for (String entry : values) {
                    if (entry.isEmpty()) {
                        continue;
                    }
                    String[] kv = entry.split("=");
                    e.put(kv[0], kv[1]);
                }
//                roomList.add(e);
            } while (recentStations.moveToNext());
        }
        recentStations.close();
        return roomList;
    }

    private List<RoomFinderRoom> onLoadSearchResultInBackground(String query){
        try {
            return TUMCabeClient.getInstance(this).fetchRooms(query);
        } catch (IOException e) {
            Utils.log(e);
        }

        return null;
    }

    private void onSearchResultLoadFinished(List<RoomFinderRoom> result){
        if (result.isEmpty()) {
            list.setAdapter(new NoResultsAdapter(this));
        } else {
            adapter = new RoomFinderListAdapter(this, result);
            list.setAdapter(adapter);
        }
    }

    // TODO: 7/21/2017 Rewrite in optional 
    private final void startLoadSearchResult(String... params){
        if (searchRoomAsyncTask != null) {
            searchRoomAsyncTask.cancel(true);
        }

        searchRoomAsyncTask = new AsyncTask<String, Void, List<RoomFinderRoom>>() {
            @Override
            protected void onPreExecute() {
                showLoadingStart();
            }

            @Override
            protected List<RoomFinderRoom> doInBackground(String... params) {
                return onLoadSearchResultInBackground(params[0]);
            }

            @Override
            protected void onPostExecute(List<RoomFinderRoom> result) {
                showLoadingEnded();
                onSearchResultLoadFinished(result);
                searchRoomAsyncTask = null;
            }
        };
        searchRoomAsyncTask.execute(params);
    }
}
