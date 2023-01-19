package de.tum.`in`.tumcampusapp.utils.camera

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

object PermissionHelper {
    /**
     * @return true if user has given permission before
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun checkPermission(permission: String, context: Context, view: View): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(context, permission)

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            //view?.showPermissionRequestDialog(permission)
            return false
        }
        return true
    }
}