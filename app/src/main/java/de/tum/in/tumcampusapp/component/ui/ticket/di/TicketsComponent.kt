package de.tum.`in`.tumcampusapp.component.ui.ticket.di

import dagger.BindsInstance
import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.BuyTicketActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.ShowTicketActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.StripePaymentActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.fragment.EventDetailsFragment

@Subcomponent(modules = [TicketsModule::class])
interface TicketsComponent {

    fun inject(eventDetailsFragment: EventDetailsFragment)

    fun inject(buyTicketActivity: BuyTicketActivity)

    fun inject(stripePaymentActivity: StripePaymentActivity)

    fun inject(showTicketActivity: ShowTicketActivity)

    @Subcomponent.Builder
    interface Builder {

        fun ticketsModule(ticketsModule: TicketsModule): TicketsComponent.Builder

        @BindsInstance
        fun eventId(@EventId eventId: Int): TicketsComponent.Builder

        fun build(): TicketsComponent

    }

}
