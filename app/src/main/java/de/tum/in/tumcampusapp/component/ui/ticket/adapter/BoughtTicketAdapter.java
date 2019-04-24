package de.tum.in.tumcampusapp.component.ui.ticket.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.ticket.BoughtTicketViewHolder;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketInfo;

public class BoughtTicketAdapter extends RecyclerView.Adapter<BoughtTicketViewHolder> {
    private List<TicketInfo> ticketInfos;

    public BoughtTicketAdapter(List<TicketInfo> ticketInfos) {
        this.ticketInfos = ticketInfos;
    }

    @NonNull
    @Override
    public BoughtTicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bought_ticket_row, parent, false);
        return new BoughtTicketViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BoughtTicketViewHolder viewHolder, int i) {
        viewHolder.bind(ticketInfos.get(i));
    }

    @Override
    public int getItemCount() {
        return ticketInfos.size();
    }
}
