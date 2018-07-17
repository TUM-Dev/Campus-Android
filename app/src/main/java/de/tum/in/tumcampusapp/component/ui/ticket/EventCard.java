package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.ticket.activity.EventDetailsActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.adapter.EventsAdapter;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;

public class EventCard extends Card {

    private Event event;

    public void setEvent(Event event) {
        this.event = event;
    }

    public EventCard(Context context) {
        super(CardManager.CARD_EVENT, context, "card_event", false);
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        View card = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_events_item, parent, false);

        EventsAdapter.EventViewHolder holder = new EventsAdapter.EventViewHolder(card);

        return holder;
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);

        if (viewHolder instanceof EventsAdapter.EventViewHolder) {
            EventsAdapter.EventViewHolder holder = (EventsAdapter.EventViewHolder) viewHolder;
            EventsAdapter.bindEventView(holder, event);
        }
    }


    @Override
    public Intent getIntent() {
        Intent intent = new Intent(getContext(), EventDetailsActivity.class);
        intent.putExtra("event_id", event.getId());
        return intent;
    }

    @Override
    protected void discard(@NotNull SharedPreferences.Editor editor) {
        // TODO: add a dismissed attribute to the event table later
        //       see NewsCard for reference
        //       Do nothing for now...
    }
}
