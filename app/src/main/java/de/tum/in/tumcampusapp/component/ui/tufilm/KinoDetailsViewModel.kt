package de.tum.`in`.tumcampusapp.component.ui.tufilm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class KinoDetailsViewModel @Inject constructor() : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _kino = MutableLiveData<Kino>()
    val kino: LiveData<Kino> = _kino

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event> = _event

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
