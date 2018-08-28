package de.tum.`in`.tumcampusapp.component.other.generic.viewstates

import de.tum.`in`.tumcampusapp.R

sealed class ErrorViewState(
        val iconResId: Int? = null,
        val headerResId: Int? = null,
        val messageResId: Int? = null,
        val buttonTextResId: Int = R.string.retry
)

class CustomViewState(messageResId: Int) : ErrorViewState(messageResId = messageResId)

class NoMoviesViewState : ErrorViewState(
        iconResId = R.drawable.no_movies,
        messageResId = R.string.no_internet_connection
)

class NoInternetViewState : ErrorViewState(
        iconResId = R.drawable.ic_no_wifi,
        messageResId = R.string.no_internet_connection
)

class NoTokenViewState : ErrorViewState(
        headerResId = R.string.error_no_access_token_setup_header,
        messageResId = R.string.error_no_access_token_setup_body,
        buttonTextResId = R.string.open_settings
)

class FailedTokenViewState(messageResId: Int) : ErrorViewState(
        headerResId = R.string.error_accessing_tumonline_header,
        messageResId = messageResId
)

