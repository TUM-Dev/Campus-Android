package de.tum.in.tumcampusapp.component.ui.ticket.model;

import com.google.gson.annotations.SerializedName;

public class TicketValidityResponse {

    @SerializedName("valid")
    public boolean valid;

    @SerializedName("ticket_history")
    public int ticketHistory;

    @SerializedName("firstname")
    public String firstName;

    @SerializedName("name")
    public String lastName;

    public String getFullName() {
        return firstName + " " + lastName;
    }

}
