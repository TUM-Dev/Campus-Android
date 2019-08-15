package de.tum.in.tumcampusapp.component.ui.ticket.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.CardInteractionListener;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.ticket.EventCard;
import de.tum.in.tumcampusapp.component.ui.ticket.EventDiffUtil;
import de.tum.in.tumcampusapp.component.ui.ticket.activity.ShowTicketActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.EventBetaInfo;
import de.tum.in.tumcampusapp.component.ui.ticket.model.EventItem;
import de.tum.in.tumcampusapp.component.ui.ticket.repository.TicketsLocalRepository;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class EventsAdapter extends RecyclerView.Adapter<CardViewHolder> {

    private static final Pattern TITLE_DATE = Pattern.compile("^[0-9]+\\. [0-9]+\\. [0-9]+:[ ]*");

    private static final int CARD_INFO = 0;
    private static final int CARD_HORIZONTAL = 1;
    private static final int CARD_VERTICAL = 2;

    private Context mContext;
    private TicketsLocalRepository mTicketsLocalRepo;

    private List<EventItem> mEvents = new ArrayList<>();
    private EventItem betaInfo = new EventBetaInfo();

    public EventsAdapter(Context context) {
        mContext = context;
        mTicketsLocalRepo = new TicketsLocalRepository(TcaDb.Companion.getInstance(context));
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == CARD_INFO) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_events_info, parent, false);
            return new CardViewHolder(view);
        }

        int layoutRes;
        if (viewType == CARD_HORIZONTAL) {
            layoutRes = R.layout.card_events_item;
        } else {
            layoutRes = R.layout.card_events_item_vertical;
        }
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(layoutRes, parent, false);
        return new EventViewHolder(view, null, false);
    }

    @Override
    public int getItemViewType(int position) {
        EventItem item = mEvents.get(position);
        if (item instanceof EventBetaInfo) {
            return CARD_INFO;
        }
        if (((Event) item).getKino() == -1) {
            return CARD_HORIZONTAL;
        }
        return CARD_VERTICAL;
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        EventItem eventItem = mEvents.get(position);
        if (eventItem instanceof EventBetaInfo) {
            return;
        }
        Event event = (Event) eventItem;
        EventCard eventCard = new EventCard(mContext);
        eventCard.setEvent(event);
        holder.setCurrentCard(eventCard);

        int ticketCount = mTicketsLocalRepo.getTicketCount(event);
        ((EventViewHolder) holder).bind(event, ticketCount);
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public void update(List<EventItem> newEvents) {
        if (newEvents.isEmpty() || !(newEvents.get(0) instanceof EventBetaInfo)) {
            newEvents.add(0, betaInfo);
        }
        DiffUtil.Callback callback = new EventDiffUtil(mEvents, newEvents);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback);
        mEvents = newEvents;
        diffResult.dispatchUpdatesTo(this);
    }

    public static class EventViewHolder extends CardViewHolder {

        private boolean showOptionsButton;
        Group optionsButtonGroup;

        CardView cardView;
        ProgressBar progressBar;
        ImageView imageView;
        TextView titleTextView;
        TextView dateTextView;
        MaterialButton ticketButton;

        public EventViewHolder(View view,
                               CardInteractionListener interactionListener,
                               boolean showOptionsButton) {
            super(view, interactionListener);
            this.showOptionsButton = showOptionsButton;

            cardView = (CardView) view;
            progressBar = view.findViewById(R.id.poster_progress_bar);
            optionsButtonGroup = view.findViewById(R.id.cardMoreIconGroup);
            imageView = view.findViewById(R.id.events_img);
            titleTextView = view.findViewById(R.id.events_title);
            dateTextView = view.findViewById(R.id.events_src_date);
            ticketButton = view.findViewById(R.id.ticketButton);
        }

        public void bind(Event event, int ticketCount) {
            String imageUrl = event.getImageUrl();
            boolean showImage = imageUrl != null && !imageUrl.isEmpty();

            optionsButtonGroup.setVisibility(showOptionsButton ? VISIBLE : GONE);

            if (showImage) {
                Picasso.get()
                       .load(imageUrl)
                       .into(imageView, new Callback() {
                           @Override
                           public void onSuccess() {
                               progressBar.setVisibility(GONE);
                           }

                           @Override
                           public void onError(Exception e) {
                               Utils.log(e);
                               progressBar.setVisibility(GONE);
                           }
                       });
            } else {
                progressBar.setVisibility(GONE);
                imageView.setVisibility(GONE);
            }

            String title = event.getTitle();
            title = TITLE_DATE.matcher(title).replaceAll("");
            titleTextView.setText(title);

            String startTime = event.getFormattedStartDateTime(itemView.getContext());
            dateTextView.setText(startTime);

            if (ticketCount == 0) {
                ticketButton.setVisibility(GONE);
                return;
            }

            ticketButton.setText(itemView.getContext().getResources()
                    .getQuantityString(R.plurals.tickets, ticketCount, ticketCount));
            ticketButton.setVisibility(VISIBLE);
            ticketButton.setOnClickListener(v -> {
                Context context = itemView.getContext();
                Intent intent = new Intent(context, ShowTicketActivity.class);
                intent.putExtra(Const.KEY_EVENT_ID, event.getId());
                context.startActivity(intent);
            });
        }

    }

}