package de.tum.`in`.tumcampusapp.di

import dagger.Component
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.di.CafeteriaComponent
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun cafeteriaComponent(): CafeteriaComponent.Builder

}
