package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.DateUtils;
import de.tum.in.tumcampus.models.MoodleEvent;

/**
 * Created by a2k on 6/8/2015.
 *
 * This class handles the view output of the results for finding events via
 * Moodle used in {@link de.tum.in.tumcampus.activities.MoodleEventsActivity}
 */
public class MoodleEventAdapter extends RecyclerView.Adapter<MoodleEventAdapter.MoodleEventViewHolder> {

    private ArrayList<MoodleEvent> events;
    private AdapterView.OnItemClickListener listener;

    public MoodleEventAdapter(ArrayList<MoodleEvent> events, AdapterView.OnItemClickListener listener){
        this.events = events;
        this.listener = listener;
    }


    @Override
    public MoodleEventAdapter.MoodleEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.card_events_item, parent, false);
        return new MoodleEventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MoodleEventAdapter.MoodleEventViewHolder holder, final int position) {
        MoodleEvent e = events.get(position);
        holder.title.setText(e.getName());
        holder.description.setText(e.getDescription());
        GregorianCalendar gc = DateUtils.epochToDate(e.getTimestart().longValue());
        holder.date.setText(gc.getTime().toString());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(null, v, position, v.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class MoodleEventViewHolder extends RecyclerView.ViewHolder{
        /**
         * ViewHolder for card_events_item
         */

        protected TextView title;
        protected TextView description;
        protected TextView date;

        public MoodleEventViewHolder(View v){
            super(v);
            title = (TextView) v.findViewById(R.id.event_title);
            description = (TextView) v.findViewById(R.id.event_description);
            date = (TextView) v.findViewById(R.id.event_date);
        }


    }
}
