package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Context;
import android.content.Intent;

import de.tum.in.tumcampusapp.component.ui.news.NewsCard;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.ticket.activity.EventDetailsActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;

public class EventCard extends NewsCard {

    private Event event;

    public void setEvent(Event event) {
        this.event = event;
    }

    public EventCard(Context context) {
        super(CardManager.CARD_EVENTS, context);
    }

    @Override
    public Intent getIntent() {
        Intent intent = new Intent(getContext(), EventDetailsActivity.class);
        intent.putExtra("event_id", event.getId());
        return intent;
    }
}
