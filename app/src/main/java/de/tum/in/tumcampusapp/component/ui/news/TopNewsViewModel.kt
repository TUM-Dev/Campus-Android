package de.tum.`in`.tumcampusapp.component.ui.news;

import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.component.ui.news.repository.TopNewsRemoteRepository
import javax.inject.Inject

/**
 * ViewModel for TopNews/NewsAlert.
 */
class TopNewsViewModel @Inject constructor(
        private val remoteRepository: TopNewsRemoteRepository
) : ViewModel() {

    /**
     * Downloads the NewsAlert and stores it in the sharedPreferences
     */
    fun fetchNewsAlert() = remoteRepository.fetchNewsAlert()

    override fun onCleared() {
        super.onCleared()
        remoteRepository.cancel()
    }

}
