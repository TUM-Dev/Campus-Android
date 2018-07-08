package de.tum.in.tumcampusapp.component.ui.ticket.payload;

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

    public String getTicketInfo() {
        return firstName + " " + lastName + "\n" + tumID + "\n" + purchaseDate + "\n" + redeemDate + "\n" + status;

    }

}
