package de.tum.in.tumcampusapp.component.ui.ticket;

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

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.utils.Utils;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    private static final Pattern COMPILE = Pattern.compile("^[0-9]+\\. [0-9]+\\. [0-9]+:[ ]*");
    private final List<Event> meventlist;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView img;
        TextView title;
        TextView locality;
        TextView srcDate;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            title = (TextView) view.findViewById(R.id.events_title);
            img = (ImageView) view.findViewById(R.id.events_img);
            locality = (TextView) view.findViewById(R.id.events_src_locality);
            srcDate = (TextView) view.findViewById(R.id.events_src_date);
        }
    }

    public EventsAdapter(List<Event> events) {
        meventlist = events;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if (mContext==null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.card_events_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = meventlist.get(position);
        holder.img.setVisibility(View.VISIBLE);
        holder.title.setVisibility(View.VISIBLE);

        // Set image
        String imgUrl = event.getImage();
        if (imgUrl.isEmpty() || imgUrl.equals("null")) {
            if (event.getLink().endsWith(".png") || event.getLink().endsWith(".jpeg")) {
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
        //Adds locality
        String locality = event.getLocality();
        holder.locality.setText(locality);

        // Adds date
        Date date = event.getDate();
        DateFormat sdf = DateFormat.getDateInstance();
        holder.srcDate.setText(sdf.format(date));
    }

    @Override
    public int getItemCount() {
        return meventlist.size();
    }


}
