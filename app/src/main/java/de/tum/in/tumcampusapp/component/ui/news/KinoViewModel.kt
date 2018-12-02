package de.tum.`in`.tumcampusapp.component.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tum.`in`.tumcampusapp.component.ui.news.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * ViewModel for kinos.
 */
class KinoViewModel(
        private val localRepository: KinoLocalRepository
) : ViewModel() {

    /**
     * Get all kinos from database
     */
    fun getAllKinos(): Flowable<List<Kino>> = // TODO: LiveData
            localRepository.getAllKinos()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .defaultIfEmpty(emptyList())

    /**
     * Get a kino by its position (id)
     */
    fun getKinoByPosition(position: Int): Flowable<Kino> =
            localRepository.getKinoByPosition(position)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    fun getEventByMovieId(movieId: String): Flowable<Event> =
            localRepository.getEventByMovieId(movieId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    fun getPositionByDate(date: String) = localRepository.getPositionByDate(date)

    fun getPositionById(id: String) = localRepository.getPositionById(id)

    class Factory(
            private val localRepository: KinoLocalRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return KinoViewModel(localRepository) as T
        }

    }

}
