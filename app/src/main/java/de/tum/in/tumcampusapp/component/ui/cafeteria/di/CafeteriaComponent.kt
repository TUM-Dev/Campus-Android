package de.tum.`in`.tumcampusapp.component.ui.cafeteria.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaDetailsSectionFragment

@Subcomponent(modules = [CafeteriaModule::class])
interface CafeteriaComponent {

    fun inject(cafeteriaActivity: CafeteriaActivity)

    fun inject(cafeteriaDetailsSectionFragment: CafeteriaDetailsSectionFragment)

    @Subcomponent.Builder
    interface Builder {

        fun cafeteriaModule(cafeteriaModule: CafeteriaModule): CafeteriaComponent.Builder

        fun build(): CafeteriaComponent

    }

}
