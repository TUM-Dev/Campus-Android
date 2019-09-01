package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import com.google.gson.annotations.SerializedName

data class Payment(
    @SerializedName("stripe_publishable_key")
    var stripePublicKey: String = "",
    @SerializedName("terms")
    var termsLink: String = "",
    var minTickets: Int = 1,
    var maxTickets: Int = 1
)