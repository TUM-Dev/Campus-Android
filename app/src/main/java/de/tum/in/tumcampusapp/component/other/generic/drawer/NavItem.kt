package de.tum.`in`.tumcampusapp.component.other.generic.drawer

import androidx.fragment.app.Fragment
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity

sealed class NavItem(
    val titleRes: Int,
    val iconRes: Int,
    val needsTUMOAccess: Boolean,
    val needsChatAccess: Boolean,
    val hideForEmployees: Boolean
) {

    class FragmentDestination(
        titleRes: Int,
        iconRes: Int,
        val fragment: Class<out Fragment>,
        needsTUMOAccess: Boolean = false,
        needsChatAccess: Boolean = false,
        hideForEmployees: Boolean = false
    ) : NavItem(titleRes, iconRes, needsTUMOAccess, needsChatAccess, hideForEmployees)

    class ActivityDestination(
        titleRes: Int,
        iconRes: Int,
        val activity: Class<out BaseActivity>,
        needsTUMOAccess: Boolean = false,
        needsChatAccess: Boolean = false,
        hideForEmployees: Boolean = false
    ) : NavItem(titleRes, iconRes, needsTUMOAccess, needsChatAccess, hideForEmployees)
}
