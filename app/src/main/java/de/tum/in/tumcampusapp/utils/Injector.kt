package de.tum.`in`.tumcampusapp.utils

import android.app.Activity
import android.app.Service
import androidx.fragment.app.Fragment
import de.tum.`in`.tumcampusapp.App
import de.tum.`in`.tumcampusapp.di.AppComponent

val Service.injector: AppComponent
    get() = (applicationContext as App).appComponent

val Fragment.injector: AppComponent
    get() = (requireContext().applicationContext as App).appComponent

val Activity.injector: AppComponent
    get() = (applicationContext as App).appComponent
