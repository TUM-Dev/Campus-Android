package de.tum.`in`.tumcampusapp

import android.annotation.SuppressLint

@SuppressLint("Registered")
class TestApp : App() {

    /**
     * When a Roboelectric test is run with the annotation @Config(application = TestApp.class)
     * The methods overriden in this class get used instead of the ones specified in App.kt
     *
     * So this method just disabled Picasso setup for Roboelectric testing
     */
    override fun setupPicasso() {
        // nothing to do
    }
}
