package de.tum.`in`.tumcampusapp.utils

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer

class NonNullLiveData<T> : MutableLiveData<T>() {

    fun observe(owner: LifecycleOwner, callback: (T) -> Unit) {
        observe(owner, Observer<T> { value ->
            value?.let {
                callback(it)
            }
        })
    }

}