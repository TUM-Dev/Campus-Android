package de.tum.`in`.tumcampusapp.api.navigatum.domain

import de.tum.`in`.tumcampusapp.api.navigatum.model.details.NavigationPropertyDto

data class NavigationProperty(
    val title: String = "",
    val value: String = ""
)

fun NavigationPropertyDto.toNavigationProperty(): NavigationProperty {
    return NavigationProperty(
        title = this.name,
        value = this.text
    )
}
