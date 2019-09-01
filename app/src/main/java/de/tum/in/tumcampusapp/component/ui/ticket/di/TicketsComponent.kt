package de.tum.`in`.tumcampusapp.component.ui.ticket.di

import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.BuyTicketActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.EventsActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.ShowTicketActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.StripePaymentActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.fragment.EventDetailsFragment
import de.tum.`in`.tumcampusapp.service.DownloadWorker

@Subcomponent(modules = [TicketsModule::class])
interface TicketsComponent {

    fun inject(eventDetailsFragment: EventDetailsFragment)

    fun inject(eventsActivity: EventsActivity)

    fun inject(buyTicketActivity: BuyTicketActivity)

    fun inject(stripePaymentActivity: StripePaymentActivity)

    fun inject(showTicketActivity: ShowTicketActivity)

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun eventId(@EventId eventId: Int): Builder

        fun build(): TicketsComponent
    }
}

@Module
interface TicketsModule {

    @Binds
    fun bindEventsDownloadAction(impl: EventsDownloadAction): DownloadWorker.Action
}
