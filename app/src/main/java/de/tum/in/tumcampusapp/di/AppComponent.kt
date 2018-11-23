package de.tum.`in`.tumcampusapp.di

import dagger.Component
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(cafeteriaActivity: CafeteriaActivity)

}
