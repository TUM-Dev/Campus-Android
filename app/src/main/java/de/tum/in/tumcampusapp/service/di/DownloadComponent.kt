package de.tum.`in`.tumcampusapp.service.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.tumui.grades.GradeNotificationDeleteReceiver
import de.tum.`in`.tumcampusapp.component.ui.onboarding.StartupActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.EventsActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.EventsFragment
import de.tum.`in`.tumcampusapp.service.DownloadWorker

@Subcomponent(modules = [DownloadModule::class])
interface DownloadComponent {

    fun inject(downloadWorker: DownloadWorker)
    fun inject(eventsFragment: EventsFragment)
    fun inject(startupActivity: StartupActivity)
    fun inject(gradeNotificationDeleteReceiver: GradeNotificationDeleteReceiver)

    @Subcomponent.Builder
    interface Builder {
        fun downloadModule(downloadModule: DownloadModule): Builder
        fun build(): DownloadComponent
    }

}
