package de.tum.`in`.tumcampusapp.component.ui.ticket.model

data class Payment(
    var stripePublicKey: String = "",
    var termsLink: String = "",
    var minTickets: Int = 1,
    var maxTickets: Int = 1
)
