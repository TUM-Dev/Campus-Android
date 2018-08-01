package de.tum.in.tumcampusapp.component.ui.ticket.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.ticket.EventCard;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private static final Pattern COMPILE = Pattern.compile("^[0-9]+\\. [0-9]+\\. [0-9]+:[ ]*");

    private Context mContext;
    private final List<Event> mEvents;

    public EventsAdapter(Context context, List<Event> events) {
        mContext = context;
        mEvents = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_events_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = mEvents.get(position);

        EventCard eventCard = new EventCard(mContext);
        eventCard.setEvent(event);
        holder.setCurrentCard(eventCard);

        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public static class EventViewHolder extends CardViewHolder {

        CardView cardView;
        ImageView imageView;
        TextView titleTextView;
        TextView localityTextView;
        TextView dateTextView;

        public EventViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            titleTextView = view.findViewById(R.id.events_title);
            imageView = view.findViewById(R.id.events_img);
            localityTextView = view.findViewById(R.id.events_src_locality);
            dateTextView = view.findViewById(R.id.events_src_date);
        }

        public void bind(Event event) {
            String imageUrl = event.getImageUrl();
            boolean showImage = imageUrl != null && !imageUrl.isEmpty();
            imageView.setVisibility(showImage ? View.VISIBLE: View.GONE);

            if (showImage) {
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.chat_background)
                        .into(imageView);
            }

            String title = event.getTitle();
            title = COMPILE.matcher(title).replaceAll("");
            titleTextView.setText(title);

            String locality = event.getLocality();
            localityTextView.setText(locality);

            String startTime = event.getFormattedStartDateTime(itemView.getContext());
            dateTextView.setText(startTime);
        }

    }

}
