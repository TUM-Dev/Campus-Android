package de.tum.`in`.tumcampusapp.di

import dagger.Component
import de.tum.`in`.tumcampusapp.component.notifications.receivers.NotificationAlarmReceiver
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarActivity
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackActivity
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderActivity
import de.tum.`in`.tumcampusapp.component.ui.barrierfree.BarrierFreeContactActivity
import de.tum.`in`.tumcampusapp.component.ui.barrierfree.BarrierFreeFacilitiesActivity
import de.tum.`in`.tumcampusapp.component.ui.barrierfree.BarrierFreeMoreInfoActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaDetailsSectionFragment
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.widget.MensaRemoteViewFactory
import de.tum.`in`.tumcampusapp.component.ui.chat.AddChatMemberActivity
import de.tum.`in`.tumcampusapp.component.ui.news.KinoDetailsFragment
import de.tum.`in`.tumcampusapp.component.ui.news.NewsActivity
import de.tum.`in`.tumcampusapp.component.ui.onboarding.WizNavCheckTokenActivity
import de.tum.`in`.tumcampusapp.component.ui.onboarding.WizNavExtrasActivity
import de.tum.`in`.tumcampusapp.component.ui.onboarding.WizNavStartActivity
import de.tum.`in`.tumcampusapp.component.ui.overview.MainActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.BuyTicketActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.ShowTicketActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.fragment.EventDetailsFragment
import de.tum.`in`.tumcampusapp.component.ui.ticket.fragment.EventsFragment
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportationActivity
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportationDetailsActivity
import de.tum.`in`.tumcampusapp.component.ui.transportation.widget.MVVWidgetConfigureActivity
import de.tum.`in`.tumcampusapp.service.DownloadService
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(mainActivity: MainActivity)

    fun inject(calendarActivity: CalendarActivity)

    fun inject(newsActivity: NewsActivity)

    fun inject(mvvWidgetConfigureActivity: MVVWidgetConfigureActivity)

    fun inject(feedbackActivity: FeedbackActivity)

    fun inject(wizNavStartActivity: WizNavStartActivity)

    fun inject(wizNavCheckTokenActivity: WizNavCheckTokenActivity)

    fun inject(wizNavExtrasActivity: WizNavExtrasActivity)

    fun inject(roomFinderActivity: RoomFinderActivity)

    fun inject(kinoDetailsFragment: KinoDetailsFragment)

    fun inject(addChatMemberActivity: AddChatMemberActivity)

    fun inject(barrierFreeFacilitiesActivity: BarrierFreeFacilitiesActivity)

    fun inject(barrierFreeContactActivity: BarrierFreeContactActivity)

    fun inject(barrierFreeMoreInfoActivity: BarrierFreeMoreInfoActivity)

    fun inject(buyTicketActivity: BuyTicketActivity)

    fun inject(showTicketActivity: ShowTicketActivity)

    fun inject(eventDetailsFragment: EventDetailsFragment)

    fun inject(eventsFragment: EventsFragment)

    fun inject(transportationActivity: TransportationActivity)

    fun inject(transportationDetailsActivity: TransportationDetailsActivity)

    fun inject(cafeteriaActivity: CafeteriaActivity)

    fun inject(cafeteriaDetailsSectionFragment: CafeteriaDetailsSectionFragment)

    fun inject(receiver: NotificationAlarmReceiver)

    fun inject(downloadService: DownloadService)

    fun inject(mensaRemoteViewFactory: MensaRemoteViewFactory)

}
