package de.tum.in.tumcampusapp.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;

/**
 * Created by dmitriipetukhov on 11/12/16.
 */

public class AssistantHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ChatMessage> mMessages;

    static class BaseVH extends RecyclerView.ViewHolder {
        TextView tvMessage;

        BaseVH(View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.tvMessage);
        }
    }

    static class IncomingVH extends BaseVH {
        IncomingVH(View itemView) {
            super(itemView);
        }
    }

    static class OutgoingVH extends BaseVH {
        OutgoingVH(View itemView) {
            super(itemView);
        }
    }

    public AssistantHistoryAdapter(ArrayList<ChatMessage> messages) {
        mMessages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessages.get(position).getMember().getDisplayName().equals("TUM Assistant")) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;
        switch (viewType) {
            case 0: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.activity_assistant_history_row_outgoing, parent, false);
                vh = new OutgoingVH(v);
                break;
            }
            case 1: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.activity_assistant_history_row_incoming, parent, false);
                vh = new IncomingVH(v);
                break;
            }
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        BaseVH vh = ((BaseVH) holder);
        vh.tvMessage.setText(mMessages.get(position).getText());

        if (getItemViewType(position) == 0) {
            OutgoingVH ovh = (OutgoingVH) vh;
        } else {
            IncomingVH ivh = (IncomingVH) vh;
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public void addElement(ChatMessage msg) {
        mMessages.add(msg);
        notifyDataSetChanged();
    }

    public ArrayList<ChatMessage> getElements() {
        return mMessages;
    }
}
