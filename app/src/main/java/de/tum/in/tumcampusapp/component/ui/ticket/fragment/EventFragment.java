package de.tum.in.tumcampusapp.component.ui.ticket.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration;
import de.tum.in.tumcampusapp.component.ui.ticket.EventsController;
import de.tum.in.tumcampusapp.component.ui.ticket.adapter.EventsAdapter;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private View view;
    private String title;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeLayout;

    private EventsController eventsController;

    private Callback<List<Event>> eventCallback;
    private Callback<List<Ticket>> ticketCallback;

    public EventFragment() {
        init("No title");
    }

    @SuppressLint("ValidFragment")
    public EventFragment(String title) {
        init(title);
    }

    private void init(String title){
        this.title = title;

        eventCallback = new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                List<Event> events = response.body();
                if (events == null){
                    events = new ArrayList<>();
                }
                eventsController.addEvents(events);
                loadEventsFromDatabase();
                mSwipeLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Utils.log(t);
                Utils.showToast(getContext(), R.string.no_internet_connection);
                mSwipeLayout.setRefreshing(false);
            }
        };

        ticketCallback = new Callback<List<Ticket>>() {
            @Override
            public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {
                List<Ticket> tickets = response.body();
                if (tickets == null){
                    tickets = new ArrayList<>();
                }
                eventsController.addTickets(tickets);
                loadEventsFromDatabase();
                mSwipeLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
                Utils.log(t);
                Utils.showToast(getContext(), R.string.no_internet_connection);
                mSwipeLayout.setRefreshing(false);
            }
        };
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

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        // create the events controller here as the context is only created when the
        // fragment is attached to an activity
        eventsController = new EventsController(this.getContext());
    }

    private void setRecyclerView() {
        recyclerView = view
                .findViewById(R.id.activity_events_list_view);

        recyclerView.setHasFixedSize(true);
        recyclerView
                .setLayoutManager(new LinearLayoutManager(getActivity()));
        int spacing = Math.round(getResources().getDimension(R.dimen.material_card_view_padding));
        recyclerView.addItemDecoration(new EqualSpacingItemDecoration(spacing));
        loadEventsFromDatabase();

    }

    private void loadEventsFromDatabase(){
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
        eventsController.getEventsAndTicketsFromServer(eventCallback, ticketCallback);
    }
}