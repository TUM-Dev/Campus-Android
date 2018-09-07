package de.tum.in.tumcampusapp.component.ui.ticket.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.ticket.EventCard;
import de.tum.in.tumcampusapp.component.ui.ticket.EventDiffUtil;
import de.tum.in.tumcampusapp.component.ui.ticket.EventsController;
import de.tum.in.tumcampusapp.component.ui.ticket.activity.ShowTicketActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private static final Pattern COMPILE = Pattern.compile("^[0-9]+\\. [0-9]+\\. [0-9]+:[ ]*");

    private Context mContext;
    private EventsController mEventsController;

    private List<Event> mEvents = new ArrayList<>();

    public EventsAdapter(Context context) {
        mContext = context;
        mEventsController = new EventsController(context);
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

        boolean hasTicket = mEventsController.isEventBooked(event);
        holder.bind(event, hasTicket);
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public void update(List<Event> newEvents) {
        DiffUtil.Callback callback = new EventDiffUtil(mEvents, newEvents);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback);

        mEvents.clear();
        mEvents.addAll(newEvents);

        diffResult.dispatchUpdatesTo(this);
    }

    public static class EventViewHolder extends CardViewHolder {

        CardView cardView;
        ProgressBar progressBar;
        ImageView imageView;
        TextView titleTextView;
        TextView localityTextView;
        TextView dateTextView;
        AppCompatButton ticketButton;

        public EventViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            progressBar = view.findViewById(R.id.poster_progress_bar);
            imageView = view.findViewById(R.id.events_img);
            titleTextView = view.findViewById(R.id.events_title);
            localityTextView = view.findViewById(R.id.events_src_locality);
            dateTextView = view.findViewById(R.id.events_src_date);
            ticketButton = view.findViewById(R.id.ticketButton);
        }

        public void bind(Event event, boolean hasTicket) {
            String imageUrl = event.getImageUrl();
            boolean showImage = imageUrl != null && !imageUrl.isEmpty();

            if (showImage) {
                Picasso.get()
                        .load(imageUrl)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                Utils.log(e);
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            } else {
                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
            }

            String title = event.getTitle();
            title = COMPILE.matcher(title).replaceAll("");
            titleTextView.setText(title);

            String locality = event.getLocality();
            localityTextView.setText(locality);

            String startTime = event.getFormattedStartDateTime(itemView.getContext());
            dateTextView.setText(startTime);

            if (!hasTicket) {
                ticketButton.setVisibility(View.GONE);
                return;
            }

            ticketButton.setVisibility(View.VISIBLE);
            ticketButton.setOnClickListener(v -> {
                Context context = itemView.getContext();
                Intent intent = new Intent(context, ShowTicketActivity.class);
                intent.putExtra(Const.KEY_EVENT_ID, event.getId());
                context.startActivity(intent);
            });
        }

    }

}
