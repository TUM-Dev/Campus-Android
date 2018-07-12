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

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.ticket.EventCard;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.utils.Utils;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private static final Pattern COMPILE = Pattern.compile("^[0-9]+\\. [0-9]+\\. [0-9]+:[ ]*");
    private final List<Event> mEventList;
    private Context mContext;

    static class EventViewHolder extends CardViewHolder {
        CardView cardView;
        ImageView imgView;
        TextView titleView;
        TextView localityView;
        TextView srcDateView;

        public EventViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            titleView = view.findViewById(R.id.events_title);
            imgView = view.findViewById(R.id.events_img);
            localityView = view.findViewById(R.id.events_src_locality);
            srcDateView = view.findViewById(R.id.events_src_date);
        }
    }

    public EventsAdapter(List<Event> events) {
        mEventList = events;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.card_events_item, parent, false);
        return new EventViewHolder(view);
    }

    public static void bindNewsView(RecyclerView.ViewHolder newsViewHolder, Event event, Context context) {
        EventViewHolder holder = (EventViewHolder) newsViewHolder;

        holder.imgView.setVisibility(View.VISIBLE);
        holder.titleView.setVisibility(View.VISIBLE);

        // Set image
        String imgUrl = event.getImage();
        if (imgUrl == null || imgUrl.isEmpty()) {
            if (event.getLink().endsWith(".png") || event.getLink().endsWith(".jpeg")) {
                Utils.log("try link as image");
                // the link points to an image (eventspread)
                Picasso.get()
                        .load(event.getLink())
                        .placeholder(R.drawable.chat_background)
                        .into(holder.imgView, new Callback() {
                            @Override
                            public void onSuccess() {
                                holder.titleView.setVisibility(View.GONE); // titleView is included in eventspread slide
                                holder.imgView.setOnClickListener(null); // link doesn't lead to more infos
                            }

                            @Override
                            public void onError(Exception e) {
                                holder.imgView.setVisibility(View.GONE); // we can't display the image after all
                            }
                        });
            } else {
                holder.imgView.setVisibility(View.GONE);
            }
        } else {
            Picasso.get()
                    .load(imgUrl)
                    .placeholder(R.drawable.chat_background)
                    .into(holder.imgView);
        }

        String title = event.getTitle();
        title = COMPILE.matcher(title)
                .replaceAll("");
        holder.titleView.setText(title);

        //Adds localityView
        String locality = event.getLocality();
        holder.localityView.setText(locality);

        // Adds date
        holder.srcDateView.setText(Event.Companion.getFormattedDate(event.getStart()));
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = mEventList.get(position);

        EventCard eventCard = new EventCard(mContext);

        eventCard.setEvent(event);
        holder.setCurrentCard(eventCard);

        bindNewsView(holder, event, mContext);
    }

    @Override
    public int getItemCount() {
        return mEventList.size();
    }
}
