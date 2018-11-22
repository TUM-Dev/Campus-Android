package de.tum.`in`.tumcampusapp.utils.ui

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.tum.`in`.tumcampusapp.R

object Dialogs {

    @JvmStatic
    fun showConfirm(context: Context, titleResId: Int, messageResId: Int) {
        showConfirm(context, context.getString(titleResId), context.getString(messageResId))
    }

    @JvmStatic
    fun showConfirm(context: Context, titleResId: Int, message: CharSequence) {
        showConfirm(context, context.getString(titleResId), message)
    }

    @JvmStatic
    fun showConfirm(context: Context, title: String, messageResId: Int) {
        showConfirm(context, title, context.getString(messageResId))
    }

    @JvmStatic
    fun showConfirm(context: Context, title: String, message: CharSequence) {
        val dialog = AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
    }

}
