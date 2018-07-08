package de.tum.`in`.tumcampusapp.api.tumonline

interface TUMOnlineResponseListener<in T> {

    fun onDownloadSuccessful(response: T)

}