package de.tum.in.tumcampusapp.component.ui.ticket;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

public class EventsActivity extends ActivityForDownloadingExternal  {

    private RecyclerView lv;
    private int state = -1;
    private EventsController nm;
    public EventsActivity() {
        super(Const.EVENTS,R.layout.activity_events);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Gets all news from mockup
        List<Event> events = EventsController.getEvents();

        if (events.size() > 0) {
            EventsAdapter adapter = new EventsAdapter(this, events);
            lv = findViewById(R.id.activity_events_list_view);
            lv.setLayoutManager(new LinearLayoutManager(this));
            lv.setAdapter(adapter);
        }
    }
    /**
     * Save ListView state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LinearLayoutManager layoutManager = (LinearLayoutManager) lv.getLayoutManager();
        state = layoutManager.findFirstVisibleItemPosition();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_activity_events, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_booked_events) {
            //TODO:fliter booked events
        }
        return true;
    }
}

