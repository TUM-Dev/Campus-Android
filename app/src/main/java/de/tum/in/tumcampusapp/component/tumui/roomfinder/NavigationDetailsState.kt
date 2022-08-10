package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import de.tum.`in`.tumcampusapp.api.navigatum.domain.NavigationDetails

data class NavigationDetailsState(
    val isLoading: Boolean = false,
    val navigationDetails: NavigationDetails? = null
)
