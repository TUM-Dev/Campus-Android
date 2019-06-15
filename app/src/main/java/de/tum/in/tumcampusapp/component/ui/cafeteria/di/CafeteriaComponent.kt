package de.tum.`in`.tumcampusapp.component.ui.cafeteria.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaDetailsSectionFragment
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.fragment.CafeteriaFragment

@Subcomponent(modules = [CafeteriaModule::class])
interface CafeteriaComponent {

    fun inject(cafeteriaFragment: CafeteriaFragment)
    fun inject(cafeteriaDetailsSectionFragment: CafeteriaDetailsSectionFragment)

    @Subcomponent.Builder
    interface Builder {

        fun cafeteriaModule(cafeteriaModule: CafeteriaModule): Builder

        fun build(): CafeteriaComponent

    }

}
