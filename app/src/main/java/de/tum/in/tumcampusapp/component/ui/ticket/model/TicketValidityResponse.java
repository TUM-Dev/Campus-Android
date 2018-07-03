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

    @SerializedName("tumid")
    public String tumID;

    @SerializedName("purchasedate")
    public String purchaseDate;

    @SerializedName("redeemdate")
    public String redeemDate;

    @SerializedName("status")
    public String status;

    public String getFullName() {
        return firstName + " " + lastName + " " + tumID + " " + purchaseDate + " " + redeemDate + " " + status;

    }

}
