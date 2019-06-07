package de.tum.`in`.tumcampusapp.component.other.generic.drawer

import androidx.fragment.app.Fragment
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity

sealed class NavItem {

    data class FragmentDestination(
        val titleRes: Int,
        val iconRes: Int,
        val fragment: Class<out Fragment>,
        val needsTUMOAccess: Boolean = false,
        val needsChatAccess: Boolean = false,
        val hideForEmployees: Boolean = false
    ) : NavItem()

    data class ActivityDestination(
        val titleRes: Int,
        val iconRes: Int,
        val activity: Class<out BaseActivity>,
        val needsTUMOAccess: Boolean = false,
        val needsChatAccess: Boolean = false,
        val hideForEmployees: Boolean = false
    ) : NavItem()

}
