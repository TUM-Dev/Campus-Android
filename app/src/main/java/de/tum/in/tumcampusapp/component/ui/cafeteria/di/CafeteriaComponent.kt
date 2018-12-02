package de.tum.`in`.tumcampusapp.component.ui.cafeteria.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity

@Subcomponent(modules = [CafeteriaModule::class])
interface CafeteriaComponent {

    fun inject(cafeteriaActivity: CafeteriaActivity)

    @Subcomponent.Builder
    interface Builder {

        fun cafeteriaModule(cafeteriaModule: CafeteriaModule): CafeteriaComponent.Builder

        fun build(): CafeteriaComponent

    }

}
