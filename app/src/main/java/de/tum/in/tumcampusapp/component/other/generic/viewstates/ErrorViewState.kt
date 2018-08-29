package de.tum.`in`.tumcampusapp.component.other.generic.viewstates

import de.tum.`in`.tumcampusapp.R

sealed class ErrorViewState(
        val iconResId: Int? = null,
        val headerResId: Int? = null,
        val messageResId: Int? = null,
        val buttonTextResId: Int = R.string.retry
)

class EmptyViewState(iconResId: Int? = null, messageResId: Int) : ErrorViewState(
        iconResId = iconResId,
        messageResId = messageResId
)

class NoInternetViewState : ErrorViewState(
        iconResId = R.drawable.ic_no_wifi,
        messageResId = R.string.no_internet_connection
)

class FailedTokenViewState(messageResId: Int) : ErrorViewState(
        headerResId = R.string.error_accessing_tumonline_header,
        messageResId = messageResId
)

class UnknownErrorViewState(messageResId: Int) : ErrorViewState(messageResId = messageResId)

