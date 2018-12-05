package de.tum.`in`.tumcampusapp.service.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.service.DownloadService

@Subcomponent(modules = [DownloadModule::class])
interface DownloadComponent {

    fun inject(downloadService: DownloadService)

    @Subcomponent.Builder
    interface Builder {

        fun downloadModule(downloadModule: DownloadModule): Builder

        fun build(): DownloadComponent

    }

}
