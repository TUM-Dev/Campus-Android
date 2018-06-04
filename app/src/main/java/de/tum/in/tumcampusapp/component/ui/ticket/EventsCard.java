package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import java.util.Date;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.overview.card.NotificationAwareCard;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Card that shows selected events
 */
public class EventsCard extends NotificationAwareCard {

    private Event mEvents;

    public EventsCard(Context context) {
        this(CardManager.CARD_EVENTS, context);
    }

    public EventsCard(int type, Context context) {
        super(type, context, "card_events", false);
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent, int type) {
        return EventsAdapter.newEventView(parent);
    }

    @Override
    public int getId() {
        return mEvents.getId();
    }

    @Override
    public String getTitle() {
        return mEvents.getTitle();
    }


    public Date getDate() {
        return mEvents.getDate();
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        EventsAdapter.bindEventsView(viewHolder, mEvents, getContext());
    }

    /**
     * Sets the information needed to show Events
     *
     * @param n Events object
     */
    public void setEvents(Event n) {
        mEvents = n;
    }

    @Override
    protected void discard(SharedPreferences.Editor editor) { }

    @Override
    protected void discardNotification(SharedPreferences.Editor editor) { }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        return false;
    }

    @Override
    protected boolean shouldShowNotification(SharedPreferences prefs) {
        return false;
    }


    @Override
    public RemoteViews getRemoteViews(Context context, int appWidgetId) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.cards_widget_card);
        remoteViews.setTextViewText(R.id.widgetCardTextView, this.getTitle());
        final String imgURL = mEvents.getImage();
        if (!imgURL.trim().isEmpty() && !"null".equals(imgURL)) {
            Utils.log(imgURL);
            Picasso.get()
                    .load(imgURL)
                    .into(remoteViews, R.id.widgetCardImageView, new int[]{appWidgetId});
        }
        return remoteViews;
    }
}
