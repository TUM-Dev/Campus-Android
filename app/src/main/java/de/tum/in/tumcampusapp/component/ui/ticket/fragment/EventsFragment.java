package de.tum.in.tumcampusapp.component.ui.ticket.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration;
import de.tum.in.tumcampusapp.component.ui.ticket.EventsController;
import de.tum.in.tumcampusapp.component.ui.ticket.adapter.EventsAdapter;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.EventType;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String KEY_EVENT_TYPE = "type";

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeLayout;
    private TextView mPlaceholderTextView;

    private EventType mEventType;
    private EventsController mEventsController;

    private Callback<List<Event>> mEventsCallback = new Callback<List<Event>>() {
        @Override
        public void onResponse(@NonNull Call<List<Event>> call,
                               @NonNull Response<List<Event>> response) {
            List<Event> events = response.body();
            if (events == null) {
                return;
            }

            mEventsController.addEvents(events);
            //loadEventsFromDatabase(); // TODO
            if (mEventType == EventType.ALL) {
                showEvents(events);
            }

            mSwipeLayout.setRefreshing(false);
        }

        @Override
        public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable t) {
            Utils.log(t);
            Utils.showToast(getContext(), R.string.error_something_wrong);
            mSwipeLayout.setRefreshing(false);
        }
    };

    private Callback<List<Ticket>> mTicketsCallback = new Callback<List<Ticket>>() {
        @Override
        public void onResponse(@NonNull Call<List<Ticket>> call, Response<List<Ticket>> response) {
            List<Ticket> tickets = response.body();
            if (tickets == null) {
                return;
            }
            mEventsController.addTickets(tickets);
            // TODO
            //loadEventsFromDatabase();
            //showEvents(events);

            if (mEventType == EventType.BOOKED) {
                List<Event> bookedEvents = mEventsController.getBookedEvents();
                showEvents(bookedEvents);
            }

            mSwipeLayout.setRefreshing(false);
        }

        @Override
        public void onFailure(@NonNull Call<List<Ticket>> call, @NonNull Throwable t) {
            Utils.log(t);
            Utils.showToast(getContext(), R.string.error_something_wrong);
            mSwipeLayout.setRefreshing(false);
        }
    };

    public static EventsFragment newInstance(Serializable eventType) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_EVENT_TYPE, eventType);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, 
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.events_fragment, container, false);

        mRecyclerView = view.findViewById(R.id.activity_events_list_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        int spacing = Math.round(getResources().getDimension(R.dimen.material_card_view_padding));
        mRecyclerView.addItemDecoration(new EqualSpacingItemDecoration(spacing));
        
        mSwipeLayout = view.findViewById(R.id.event_refresh);
        mSwipeLayout.setOnRefreshListener(this);

        mPlaceholderTextView = view.findViewById(R.id.placeholder_text_view);
        
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Create the events controller here as the context is only created when the
        // fragment is attached to an activity
        mEventsController = new EventsController(getContext());

        if (getArguments() != null) {
            mEventType = (EventType) getArguments().getSerializable(KEY_EVENT_TYPE);

            if (mEventType != null) {
                List<Event> events = loadEventsFromDatabase(mEventType);
                showEvents(events);
            }
        }
    }

    private List<Event> loadEventsFromDatabase(EventType type) {
        List<Event> events;

        if (type == EventType.ALL) {
            events = mEventsController.getEvents();
        } else {
            events = mEventsController.getBookedEvents();
        }

        return events;
    }

    private void showEvents(List<Event> events)  {
        boolean isEmpty = events.isEmpty();
        mRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        mPlaceholderTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        if (!events.isEmpty()) {
            EventsAdapter adapter = new EventsAdapter(getContext(), events);
            mRecyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            mPlaceholderTextView.setText(mEventType.getPlaceholderResId());
        }
    }

    @Override
    public void onRefresh() {
        mEventsController.getEventsAndTicketsFromServer(mEventsCallback, mTicketsCallback);
    }

}