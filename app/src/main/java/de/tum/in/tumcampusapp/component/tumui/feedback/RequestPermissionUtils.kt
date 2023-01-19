package de.tum.`in`.tumcampusapp.component.tumui.feedback

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class RequestPermissionUtils (private val context: Context,view: View){

    /**
     * @return true if user has given permission before
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public fun checkPermission(permission: String): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(context, permission)

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            val requestCode = when (permission) {
                Manifest.permission.READ_EXTERNAL_STORAGE -> FeedbackPresenter.PERMISSION_FILES
                else -> FeedbackPresenter.PERMISSION_CAMERA
            }

            showPermissionRequestDialog(permission, requestCode)
            return false
        }
        return true
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun showPermissionRequestDialog(permission: String, requestCode: Int) {
      //  requestPermissions(arrayOf(permission), requestCode)
    }
}