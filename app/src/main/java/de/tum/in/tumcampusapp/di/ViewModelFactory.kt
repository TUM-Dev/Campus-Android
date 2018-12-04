package de.tum.`in`.tumcampusapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Provider

/**
 * This factory takes a [Provider] of a [ViewModel] and creates a [ViewModel] from it. We use this
 * so that we don't have to write factories for each individual [ViewModel] that we use.
 *
 * @param provider A [Provider] of the requested [ViewModel]
 */
class ViewModelFactory<T : ViewModel>(
        private val provider: Provider<T>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return provider.get() as T
    }

}
