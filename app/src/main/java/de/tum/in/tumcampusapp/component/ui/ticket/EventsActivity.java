package de.tum.in.tumcampusapp.component.ui.ticket;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.repository.EventLocalRepository;
import de.tum.in.tumcampusapp.component.ui.ticket.repository.EventRemoteRepository;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;

public class EventsActivity extends ActivityForDownloadingExternal {

    private RecyclerView lv;
    private boolean mBookedShowMode;
    private MenuItem menuItemSwitchView;

    private EventViewModel eventViewModel;

    private final CompositeDisposable disposable = new CompositeDisposable();

    public EventsActivity() {
        super(Const.EVENTS, R.layout.activity_events);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventLocalRepository.db = TcaDb.getInstance(this);
        eventViewModel = new EventViewModel(EventLocalRepository.INSTANCE,
                EventRemoteRepository.INSTANCE, disposable);

        lv = findViewById(R.id.activity_events_list_view);
        lv.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

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

        Flowable<List<Event>> eventsFlowable;
        if (mBookedShowMode) {
            icon = R.drawable.ic_action_booked;
            // TODO: integrate server call for getTickets to extract booked events
            //eventsFlowable = eventViewModel.getBookdedEvents();
            eventsFlowable = eventViewModel.getAllEvents();
        } else {
            icon = R.drawable.ic_all_event;
            eventsFlowable = eventViewModel.getAllEvents();
        }

        if (menuItemSwitchView != null) {
            menuItemSwitchView.setIcon(icon);
        }

        View noMovies = findViewById(R.id.no_movies_layout);

        // TODO: comment this
        eventsFlowable
                .doOnError(throwable -> setContentView(R.layout.layout_error))
                .subscribe(events -> {
                    if (events.isEmpty()) {
                        noMovies.setVisibility(View.VISIBLE);
                    } else {
                        noMovies.setVisibility(View.GONE);

                        EventsAdapter adapter = new EventsAdapter(events);
                        lv.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}

