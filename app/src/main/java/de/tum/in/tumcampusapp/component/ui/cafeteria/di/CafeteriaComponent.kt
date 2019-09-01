package de.tum.`in`.tumcampusapp.component.ui.cafeteria.di

import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaDetailsSectionFragment
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.fragment.CafeteriaFragment
import de.tum.`in`.tumcampusapp.service.DownloadWorker

@Subcomponent(modules = [CafeteriaModule::class])
interface CafeteriaComponent {
    fun inject(cafeteriaFragment: CafeteriaFragment)
    fun inject(cafeteriaDetailsSectionFragment: CafeteriaDetailsSectionFragment)
}

@Module
interface CafeteriaModule {

    @Binds
    fun bindCafeteriaDownloadAction(impl: CafeteriaDownloadAction): DownloadWorker.Action
}
