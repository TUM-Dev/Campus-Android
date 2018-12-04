package de.tum.`in`.tumcampusapp.component.ui.news.model

import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime

data class NewsAlert(
        var url: String = "",
        var link: String = "",
        var displayUntil: String = ""
) {

    private val displayUntilDate: DateTime?
        get() = DateTimeUtils.parseIsoDateWithMillis(displayUntil)

    val shouldDisplay: Boolean
        get() = displayUntilDate?.isAfterNow ?: false

}
