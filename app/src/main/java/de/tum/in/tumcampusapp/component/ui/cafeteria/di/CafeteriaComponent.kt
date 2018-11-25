package de.tum.`in`.tumcampusapp.component.ui.cafeteria.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaNotificationSettingsActivity

@Subcomponent(modules = [CafeteriaModule::class])
interface CafeteriaComponent {

    fun inject(cafeteriaNotificationSettingsActivity: CafeteriaNotificationSettingsActivity)

    @Subcomponent.Builder
    interface Builder {

        fun cafeteriaModule(cafeteriaModule: CafeteriaModule): CafeteriaComponent.Builder

        fun build(): CafeteriaComponent

    }

}
