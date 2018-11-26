package de.tum.`in`.tumcampusapp.component.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.component.ui.news.model.NewsSources
import de.tum.`in`.tumcampusapp.component.ui.news.repository.NewsLocalRepository
import org.jetbrains.anko.doAsync
import javax.inject.Inject

class NewsViewModel @Inject constructor(
        private val localRepository: NewsLocalRepository
) : ViewModel() {

    private val _news = MutableLiveData<List<News>>()
    val news: LiveData<List<News>> = _news

    private val _error = MutableLiveData<Unit>()
    val error: LiveData<Unit> = _error

    init {
        doAsync {
            val results = localRepository.getAll()
            if (results.isEmpty()) {
                _error.postValue(Unit)
            } else {
                _news.postValue(results)
            }
        }
    }

    fun getNewsSources(): List<NewsSources> = localRepository.getNewsSources()

    fun getTodayIndex(): Int = localRepository.getTodayIndex()

}
