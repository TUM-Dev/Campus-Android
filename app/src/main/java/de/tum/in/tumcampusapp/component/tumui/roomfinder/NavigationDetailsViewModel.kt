package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.api.navigatum.NavigaTumAPIClient
import de.tum.`in`.tumcampusapp.api.navigatum.domain.toNavigationDetails
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class NavigationDetailsViewModel @Inject constructor(
    private val navigaTumAPIClient: NavigaTumAPIClient
) : ViewModel() {

    val state: MutableStateFlow<NavigationDetailsState> = MutableStateFlow(NavigationDetailsState())

    private var disposable: Disposable? = null

    fun loadNavigationDetails(navigationEntityId: String) {
        state.value = state.value.copy(isLoading = true)
        disposable = navigaTumAPIClient
            .getNavigationDetails(navigationEntityId)
            .subscribeOn(Schedulers.io())
            .map { it.toNavigationDetails() }
            .doOnError(Utils::log)
            .onErrorReturn { null }
            .subscribe { details ->
                state.value = state.value.copy(
                    isLoading = false,
                    navigationDetails = details
                )
            }
    }
}
