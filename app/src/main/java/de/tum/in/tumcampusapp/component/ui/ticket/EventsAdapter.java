package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.utils.Utils;

public class EventsAdapter extends RecyclerView.Adapter<CardViewHolder> {
    private static final Pattern COMPILE = Pattern.compile("^[0-9]+\\. [0-9]+\\. [0-9]+:[ ]*");
    private final List<Event> events;
    private final Context mContext;

    EventsAdapter(Context context, List<Event> events) {
        this.mContext = context;
        this.events = events;
    }

    public static EventsViewHolder newEventView(ViewGroup parent) {
        View card;
            card = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_events_item, parent, false);
        EventsViewHolder holder = new EventsViewHolder(card);
        holder.title = card.findViewById(R.id.events_title);
        holder.img = card.findViewById(R.id.events_img);
        holder.srcDate = card.findViewById(R.id.events_src_date);
        card.setTag(holder);
        return holder;
    }

    public static void bindEventsView(RecyclerView.ViewHolder eventsViewHolder, Event event, Context context) {
        EventsViewHolder holder = (EventsViewHolder) eventsViewHolder;
        holder.img.setVisibility(View.VISIBLE);
        holder.title.setVisibility(View.VISIBLE);

        // Set image
        String imgUrl = event.getImage();
        if (imgUrl.isEmpty() || imgUrl.equals("null")) {
            if(event.getLink().endsWith(".png") || event.getLink().endsWith(".jpeg")){
                Utils.log("try link as image");
                // the link points to an image (eventspread)
                Picasso.get()
                        .load(event.getLink())
                        .placeholder(R.drawable.chat_background)
                        .into(holder.img, new Callback() {
                            @Override
                            public void onSuccess() {
                                holder.title.setVisibility(View.GONE); // title is included in eventspread slide
                                holder.img.setOnClickListener(null); // link doesn't lead to more infos
                            }
                            @Override
                            public void onError(Exception e) {
                                holder.img.setVisibility(View.GONE); // we can't display the image after all
                            }
                        });
            } else {
                holder.img.setVisibility(View.GONE);
            }
        } else {
            Picasso.get()
                    .load(imgUrl)
                    .placeholder(R.drawable.chat_background)
                    .into(holder.img);
        }

        String title = event.getTitle();
            title = COMPILE.matcher(title)
                    .replaceAll("");
        holder.title.setText(title);

        // Adds date
        Date date = event.getDate();
        DateFormat sdf = DateFormat.getDateInstance();
        holder.srcDate.setText(sdf.format(date));

    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return newEventView(parent);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        EventsViewHolder nHolder = (EventsViewHolder) holder;
        EventsCard card;
            card = new EventsCard(mContext);

        card.setEvents(events.get(position));
        nHolder.setCurrentCard(card);

        bindEventsView(holder, events.get(position), mContext);
    }

/*    @Override
    public int getItemViewType(int position) {
        return news.get(position).isFilm() ? 0 : 1;
    }*/

    @Override
    public int getItemCount() {
        return events.size();
    }

    private static class EventsViewHolder extends CardViewHolder {
        ImageView img;
        TextView title;
        TextView srcDate;

        EventsViewHolder(View itemView) {
            super(itemView);
        }
    }
}
