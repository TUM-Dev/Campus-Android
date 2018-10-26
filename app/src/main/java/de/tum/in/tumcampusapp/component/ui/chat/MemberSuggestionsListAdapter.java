package de.tum.in.tumcampusapp.component.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;

public class MemberSuggestionsListAdapter extends BaseAdapter implements Filterable{

    private List<ChatMember> originalData;
    private List<ChatMember> members;
    private Context mContext;

    // constructor
    MemberSuggestionsListAdapter(Context context, List<ChatMember> members) {
        originalData = members;
        this.members = members;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2, parent, false);
        }
        ((TextView) view.findViewById(android.R.id.text1)).setText(members.get(position).getDisplayName());
        ((TextView) view.findViewById(android.R.id.text2)).setText(members.get(position).getLrzId());
        return view;
    }

    @Override
    public ChatMember getItem(int position) {
        if (members != null) {
            return members.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        if(members == null || members.size() < i){
            return -1;
        }
        return members.get(i).getId();
    }

    @Override
    public int getCount() {
        if(members == null){
            return 0;
        }
        return members.size();
    }

    public void updateSuggestions(List<ChatMember> members) {
        this.originalData = members;
        this.members = members;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();
                ArrayList<ChatMember> after = new ArrayList<>();
                if(charSequence != null) {
                    for (ChatMember member : originalData) {
                        if (member.getDisplayName() != null && member.getDisplayName().contains(charSequence)) {
                            after.add(member);
                        }
                    }
                }
                results.values = after;
                results.count = after.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                members = (List<ChatMember>)filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
