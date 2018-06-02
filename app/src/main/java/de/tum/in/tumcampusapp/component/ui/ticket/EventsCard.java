package de.tum.in.tumcampusapp.component.ui.ticket;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Date;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.overview.card.NotificationAwareCard;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.database.TcaDb;
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

/*    public String getSource() {
        return mEvents.getSrc();
    }*/

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
     * @param n   Events object
     */
    public void setEvents(Event n) {
        mEvents = n;
    }

    @Override
    protected void discard(SharedPreferences.Editor editor) {
        EventsController eventsController = new EventsController(getContext());
        eventsController.setDismissed(mEvents.getId(), mEvents.getDismissed() | 1);
    }

    @Override
    protected void discardNotification(SharedPreferences.Editor editor) {
        EventsController eventsController = new EventsController(getContext());
        eventsController.setDismissed(mEvents.getId(), mEvents.getDismissed() | 2);
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        return (mEvents.getDismissed() & 1) == 0;
    }

    @Override
    protected boolean shouldShowNotification(SharedPreferences prefs) {
        return (mEvents.getDismissed() & 2) == 0;
    }

    @Override
/*    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        NewsSourcesDao newsSourcesDao = TcaDb.getInstance(getContext()).newsSourcesDao();
        NewsSources newsSource = newsSourcesDao.getNewsSource(Integer.parseInt(mNews.getSrc()));
        notificationBuilder.setContentTitle(getContext().getString(R.string.news));
        notificationBuilder.setContentText(mNews.getTitle());
        notificationBuilder.setContentInfo(newsSource.getTitle());
        notificationBuilder.setTicker(mNews.getTitle());
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        try {
            if(!mNews.getImage().isEmpty()){
                Bitmap bgImg = Picasso.get().load(mNews.getImage()).get();
                notificationBuilder.extend(new NotificationCompat.WearableExtender().setBackground(bgImg));
            }
        } catch (IOException e) {
            // ignore it if download fails
        }
        return notificationBuilder.build();
    }*///NewsDao 是query，mockup的话还需不需要

    @Override
    public Intent getIntent() {
        // Show regular Events in browser
        String url = mEvents.getLink();
        if (url.isEmpty()) {
            Utils.showToast(getContext(), R.string.no_link_existing);
            return null;
        }

        // Opens url in browser
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
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
