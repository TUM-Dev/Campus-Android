package de.tum.`in`.tumcampusapp.component.ui.ticket

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

sealed class Action {
    data class Refresh(val isLoggedIn: Boolean) : Action()
}

sealed class Result {
    object ShowLoading : Result()
    data class EventsLoaded(val events: List<Event>) : Result()
    object ShowError : Result()
    object HideError : Result()
    object None : Result()
}

class EventsViewModel @Inject constructor() : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _viewState = MutableLiveData<EventsViewState>()
    val viewState: LiveData<EventsViewState> = _viewState

    fun refreshEventsAndTickets() {
    }

    private fun render(viewState: EventsViewState) {
        _viewState.postValue(viewState)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    companion object {
        private const val ERROR_DURATION: Long = 4
    }
}
