package de.tum.`in`.tumcampusapp.service.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.service.DownloadWorker

@Subcomponent(modules = [DownloadModule::class])
interface DownloadComponent {

    fun inject(downloadWorker: DownloadWorker)

    @Subcomponent.Builder
    interface Builder {

        fun downloadModule(downloadModule: DownloadModule): Builder

        fun build(): DownloadComponent

    }

}
