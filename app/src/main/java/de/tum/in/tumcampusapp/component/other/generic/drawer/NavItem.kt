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
        val fragment: Class<out Fragment>,
        titleRes: Int,
        iconRes: Int,
        needsTUMOAccess: Boolean = false,
        needsChatAccess: Boolean = false,
        hideForEmployees: Boolean = false
    ) : NavItem(titleRes, iconRes, needsTUMOAccess, needsChatAccess, hideForEmployees)

    class ActivityDestination(
        val activity: Class<out BaseActivity>,
        titleRes: Int,
        iconRes: Int,
        needsTUMOAccess: Boolean = false,
        needsChatAccess: Boolean = false,
        hideForEmployees: Boolean = false
    ) : NavItem(titleRes, iconRes, needsTUMOAccess, needsChatAccess, hideForEmployees)

}
