package de.tum.in.tumcampusapp.component.ui.ticket;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

public class EventsActivity extends ActivityForDownloadingExternal {

    private RecyclerView lv;
    private boolean mBookedShowMode;
    private MenuItem menuItemSwitchView;

    private EventsController eventsController;

    public EventsActivity() {
        super(Const.EVENTS, R.layout.activity_events);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lv = findViewById(R.id.activity_events_list_view);
        lv.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        eventsController = new EventsController(this);

        this.mBookedShowMode = Utils.getSettingBool(this, Const.EVENT_BOOKED_MODE, false);
        refreshEventView();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_activity_events, menu);
        menuItemSwitchView = menu.findItem(R.id.action_booked_events);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_booked_events) {
            mBookedShowMode = !mBookedShowMode;
            Utils.setSetting(this, Const.EVENT_BOOKED_MODE, mBookedShowMode);
            this.refreshEventView();
        }
        return true;
    }

    private void refreshEventView() {
        int icon;

        List<Event> events;
        if (mBookedShowMode) {
            icon = R.drawable.ic_action_booked;
            // TODO: integrate server call for getTickets to extract booked events
            events = eventsController.getBookedEvents();
        } else {
            icon = R.drawable.ic_all_event;
            events = eventsController.getEvents();
        }

        EventsAdapter adapter = new EventsAdapter(events);
        lv.setAdapter(adapter);

        if (menuItemSwitchView != null) {
            menuItemSwitchView.setIcon(icon);
        }
    }
}

