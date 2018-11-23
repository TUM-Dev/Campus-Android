package de.tum.`in`.tumcampusapp.component.ui.cafeteria.interactors

import androidx.lifecycle.LiveData

interface LiveDataInteractor<T> {

    fun execute(): LiveData<T>

}
