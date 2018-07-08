package de.tum.in.tumcampusapp.component.ui.studycard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.studycard.model.StudyCard;

public class StudyCardListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<StudyCard> items;

    // the layout of the list
    static class ViewHolder {
        TextView title;
    }

    // constructor
    public StudyCardListAdapter(Context context, List<StudyCard> items) {
        super();
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public StudyCard getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0; //this.items.get(position).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        StudyCard card = getItem(position);

        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.activity_cards_list_item, parent, false);
            viewHolder.title = (TextView) view.findViewById(R.id.card_title);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (card != null) {
            // viewHolder.title.setText(card.getTitle());
        }

        return view;
    }
}
