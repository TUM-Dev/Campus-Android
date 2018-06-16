package de.tum.in.tumcampusapp.component.ui.ticket;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;

/**
 * Created by SONU on 16/09/15.
 */
public class EventFragment extends Fragment {
    private View view;

    private String title;//String for tab title

    private static RecyclerView recyclerView;

    public EventFragment() {
    }

    @SuppressLint("ValidFragment")
    public EventFragment(String title) {
        this.title = title;//Setting tab title
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.event_fragment, container, false);

        setRecyclerView();
        return view;

    }

    //Setting recycler view
    private void setRecyclerView() {

        recyclerView = (RecyclerView) view
                .findViewById(R.id.activity_events_list_view);
        recyclerView.setHasFixedSize(true);
        recyclerView
                .setLayoutManager(new LinearLayoutManager(getActivity()));//Linear Items
        List<Event> events;
        if (title.equals(this.getString(R.string.booked_events))) {
            events = EventsController.getBookedEvents();
        } else {
            events = EventsController.getEvents();
        }
        EventsAdapter adapter = new EventsAdapter(events);
        recyclerView.setAdapter(adapter);// set adapter on recyclerview

    }
}