package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.models.tumcabe.Curriculum;

public class CurriculumAdapter extends SimpleStickyListHeadersAdapter<Curriculum> {

    public CurriculumAdapter(Context context, List<Curriculum> infos) {
        super(context, infos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(infoList.get(position)
                                                                                  .getName());

        return convertView;
    }
}
