package de.tum.`in`.tumcampusapp.di

import dagger.Component
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.di.CafeteriaComponent
import de.tum.`in`.tumcampusapp.component.ui.news.di.NewsComponent
import de.tum.`in`.tumcampusapp.component.ui.overview.MainActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.di.EventsComponent
import de.tum.`in`.tumcampusapp.component.ui.ticket.di.TicketsComponent
import de.tum.`in`.tumcampusapp.component.ui.tufilm.di.KinoComponent
import de.tum.`in`.tumcampusapp.service.di.DownloadComponent
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun cafeteriaComponent(): CafeteriaComponent.Builder

    fun downloadComponent(): DownloadComponent.Builder

    fun eventsComponent(): EventsComponent.Builder

    fun kinoComponent(): KinoComponent.Builder

    fun newsComponent(): NewsComponent.Builder

    fun ticketsComponent(): TicketsComponent.Builder

    fun inject(mainActivity: MainActivity)

}
