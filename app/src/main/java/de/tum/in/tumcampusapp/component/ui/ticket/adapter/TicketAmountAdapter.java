package de.tum.in.tumcampusapp.component.ui.ticket.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.ticket.TicketAmountViewHolder;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;

public class TicketAmountAdapter extends RecyclerView.Adapter<TicketAmountViewHolder> {
    private List<TicketType> ticketTypes;

    public TicketAmountAdapter(List<TicketType> typeList) {
        ticketTypes = typeList;
    }

    @NonNull
    @Override
    public TicketAmountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_amount_row, parent, false);
        return new TicketAmountViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketAmountViewHolder holder, int position) {
        holder.bindToTicketType(ticketTypes.get(position), position);
    }

    @Override
    public int getItemCount() {
        return ticketTypes.size();
    }
}
