package de.tum.`in`.tumcampusapp

import android.annotation.SuppressLint
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.plugins.RxJavaPlugins

@SuppressLint("Registered")
class TestApp : App() {

    override fun setupPicasso() {
        // nothing to do
    }

    override fun initRxJavaErrorHandler(){
        FirebaseApp.initializeApp(applicationContext)
        RxJavaPlugins.setErrorHandler(FirebaseCrashlytics.getInstance()::recordException)
    }
}
