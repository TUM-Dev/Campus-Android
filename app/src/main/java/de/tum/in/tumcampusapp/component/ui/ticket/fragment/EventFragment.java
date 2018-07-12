package de.tum.in.tumcampusapp.component.ui.ticket.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration;
import de.tum.in.tumcampusapp.component.ui.ticket.EventsController;
import de.tum.in.tumcampusapp.component.ui.ticket.adapter.EventsAdapter;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;

public class EventFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private View view;
    private String title;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeLayout;

    private EventsController eventsController;

    public EventFragment() {
    }

    @SuppressLint("ValidFragment")
    public EventFragment(String title) {
        this.title = title;
        eventsController = new EventsController(this.getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.event_fragment, container, false);
        setRecyclerView();
        mSwipeLayout = view.findViewById(R.id.event_refresh);
        mSwipeLayout.setOnRefreshListener(this);
        return view;

    }

    private void setRecyclerView() {
        recyclerView = view
                .findViewById(R.id.activity_events_list_view);

        recyclerView.setHasFixedSize(true);
        recyclerView
                .setLayoutManager(new LinearLayoutManager(getActivity()));
        int spacing = Math.round(getResources().getDimension(R.dimen.material_card_view_padding));
        recyclerView.addItemDecoration(new EqualSpacingItemDecoration(spacing));
        loadEvents();

    }

    private void loadEvents(){
        List<Event> events;
        if (title.equals(this.getString(R.string.booked_events))) {
            events = eventsController.getBookedEvents();
        } else {
            events = eventsController.getEvents();
        }
        EventsAdapter adapter = new EventsAdapter(events);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        loadEvents();
        mSwipeLayout.setRefreshing(false);
    }
}