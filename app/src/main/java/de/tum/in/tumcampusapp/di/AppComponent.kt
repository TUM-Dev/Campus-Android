package de.tum.`in`.tumcampusapp.di

import android.content.Context
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DatabaseModule::class, InMemoryDatabaseModule::class])
interface AppComponent {

    fun inject(context: Context)

}