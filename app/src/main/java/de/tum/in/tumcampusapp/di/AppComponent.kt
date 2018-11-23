package de.tum.`in`.tumcampusapp.di

import dagger.Component
import de.tum.`in`.tumcampusapp.component.notifications.receivers.NotificationAlarmReceiver
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaDetailsSectionFragment
import de.tum.`in`.tumcampusapp.component.ui.overview.MainActivity
import de.tum.`in`.tumcampusapp.service.DownloadService
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(mainActivity: MainActivity)

    fun inject(cafeteriaActivity: CafeteriaActivity)

    fun inject(cafeteriaDetailsSectionFragment: CafeteriaDetailsSectionFragment)

    fun inject(receiver: NotificationAlarmReceiver)

    fun inject(downloadService: DownloadService)

}
