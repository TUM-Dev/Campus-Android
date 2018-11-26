package de.tum.`in`.tumcampusapp.component.ui.cafeteria.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaNotificationSettingsActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.widget.MensaWidget

@Subcomponent(modules = [CafeteriaModule::class])
interface CafeteriaComponent {

    fun inject(cafeteriaNotificationSettingsActivity: CafeteriaNotificationSettingsActivity)

    fun inject(mensaWidget: MensaWidget)

    @Subcomponent.Builder
    interface Builder {

        fun cafeteriaModule(cafeteriaModule: CafeteriaModule): CafeteriaComponent.Builder

        fun build(): CafeteriaComponent

    }

}
