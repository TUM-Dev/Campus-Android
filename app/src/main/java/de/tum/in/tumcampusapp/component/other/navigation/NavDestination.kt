package de.tum.`in`.tumcampusapp.component.other.navigation

import android.os.Bundle
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity

sealed class NavDestination {

    data class Activity @JvmOverloads constructor(
        val clazz: Class<out BaseActivity>,
        val args: Bundle = Bundle.EMPTY
    ) : NavDestination()

    data class Fragment @JvmOverloads constructor(
        val clazz: Class<out androidx.fragment.app.Fragment>,
        val args: Bundle = Bundle.EMPTY
    ) : NavDestination()

    data class Link(
        val url: String
    ) : NavDestination()

}