package de.tum.`in`.tumcampusapp.di

import dagger.Component
import de.tum.`in`.tumcampusapp.component.notifications.receivers.NotificationAlarmReceiver
import de.tum.`in`.tumcampusapp.component.tumui.calendar.di.CalendarComponent
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackActivity
import de.tum.`in`.tumcampusapp.component.tumui.person.PersonSearchActivity
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderActivity
import de.tum.`in`.tumcampusapp.component.ui.barrierfree.BarrierFreeContactActivity
import de.tum.`in`.tumcampusapp.component.ui.barrierfree.BarrierFreeFacilitiesActivity
import de.tum.`in`.tumcampusapp.component.ui.barrierfree.BarrierFreeMoreInfoActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaDetailsSectionFragment
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.di.CafeteriaComponent
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.widget.MensaRemoteViewFactory
import de.tum.`in`.tumcampusapp.component.ui.chat.AddChatMemberActivity
import de.tum.`in`.tumcampusapp.component.ui.chat.di.ChatComponent
import de.tum.`in`.tumcampusapp.component.ui.eduroam.SetupEduroamActivity
import de.tum.`in`.tumcampusapp.component.ui.news.NewsActivity
import de.tum.`in`.tumcampusapp.component.ui.news.di.NewsComponent
import de.tum.`in`.tumcampusapp.component.ui.onboarding.WizNavCheckTokenActivity
import de.tum.`in`.tumcampusapp.component.ui.onboarding.WizNavExtrasActivity
import de.tum.`in`.tumcampusapp.component.ui.onboarding.WizNavStartActivity
import de.tum.`in`.tumcampusapp.component.ui.openinghour.OpeningHoursDetailFragment
import de.tum.`in`.tumcampusapp.component.ui.overview.MainActivity
import de.tum.`in`.tumcampusapp.component.ui.studyroom.di.StudyRoomsComponent
import de.tum.`in`.tumcampusapp.component.ui.ticket.di.TicketsComponent
import de.tum.`in`.tumcampusapp.component.ui.transportation.di.TransportComponent
import de.tum.`in`.tumcampusapp.component.ui.transportation.widget.MVVWidgetService
import de.tum.`in`.tumcampusapp.component.ui.tufilm.di.KinoComponent
import de.tum.`in`.tumcampusapp.service.DownloadService
import de.tum.`in`.tumcampusapp.service.FillCacheService
import de.tum.`in`.tumcampusapp.service.QueryLocationsService
import de.tum.`in`.tumcampusapp.service.ScanResultsAvailableReceiver
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun cafeteriaComponent(): CafeteriaComponent.Builder

    fun calendarComponent(): CalendarComponent.Builder

    fun chatComponent(): ChatComponent.Builder

    fun kinoComponent(): KinoComponent.Builder

    fun newsComponent(): NewsComponent.Builder

    fun studyRoomsComponent(): StudyRoomsComponent.Builder

    fun ticketsComponent(): TicketsComponent.Builder

    fun transportComponent(): TransportComponent.Builder

    fun inject(mainActivity: MainActivity)

    fun inject(newsActivity: NewsActivity)

    fun inject(feedbackActivity: FeedbackActivity)

    fun inject(wizNavStartActivity: WizNavStartActivity)

    fun inject(wizNavCheckTokenActivity: WizNavCheckTokenActivity)

    fun inject(wizNavExtrasActivity: WizNavExtrasActivity)

    fun inject(roomFinderActivity: RoomFinderActivity)

    fun inject(addChatMemberActivity: AddChatMemberActivity)

    fun inject(barrierFreeFacilitiesActivity: BarrierFreeFacilitiesActivity)

    fun inject(barrierFreeContactActivity: BarrierFreeContactActivity)

    fun inject(barrierFreeMoreInfoActivity: BarrierFreeMoreInfoActivity)

    fun inject(cafeteriaActivity: CafeteriaActivity)

    fun inject(cafeteriaDetailsSectionFragment: CafeteriaDetailsSectionFragment)

    fun inject(receiver: NotificationAlarmReceiver)

    fun inject(downloadService: DownloadService)

    fun inject(fillCacheService: FillCacheService)

    fun inject(mensaRemoteViewFactory: MensaRemoteViewFactory)

    fun inject(mvvRemoteViewFactory: MVVWidgetService)

    fun inject(setupEduroamActivity: SetupEduroamActivity)

    fun inject(scanResultsAvailableReceiver: ScanResultsAvailableReceiver)

    fun inject(openingHoursDetailFragment: OpeningHoursDetailFragment)

    fun inject(personSearchActivity: PersonSearchActivity)

    fun inject(queryLocationsService: QueryLocationsService)

}
