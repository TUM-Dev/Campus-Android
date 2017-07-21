package de.tum.in.tumcampusapp.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.common.base.Optional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForSearchingInBackground;
import de.tum.in.tumcampusapp.adapters.NoResultsAdapter;
import de.tum.in.tumcampusapp.adapters.RoomFinderListAdapter;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.RoomFinderSuggestionProvider;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.RecentsManager;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderRoom;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Activity to show a convenience interface for using the MyTUM room finder.
 */
public class RoomFinderActivity extends ActivityForSearchingInBackground<List<RoomFinderRoom>>
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
    protected Optional<List<RoomFinderRoom>> onSearchInBackground() {
        // TODO: 7/21/2017 Return recent information
        return Optional.of(getRecents());
    }

    @Override
    protected Optional<List<RoomFinderRoom>> onSearchInBackground(String query) {
        try {
            List<RoomFinderRoom> rooms = TUMCabeClient.getInstance(this).fetchRooms(query);
            return Optional.of(rooms);
        } catch (IOException e) {
            Utils.log(e);
        }
        return null;
    }

    @Override
    protected void onSearchFinished(Optional<List<RoomFinderRoom>> result) {
        if(!result.isPresent()){
            if (NetUtils.isConnected(this)) {
                showErrorLayout();
            } else {
                showNoInternetLayout();
            }
            return;
        }

        List<RoomFinderRoom> searchResult = result.get();
        if (searchResult.isEmpty()) {
            list.setAdapter(new NoResultsAdapter(this));
        } else {
            adapter = new RoomFinderListAdapter(this, searchResult);
            list.setAdapter(adapter);
        }
        showLoadingEnded();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RoomFinderRoom room = (RoomFinderRoom) list.getAdapter().getItem(position);
        // TODO: 7/21/2017 Rewrite open details
//        openRoomDetails(room);
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
}
