package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R

class ThemedAlertDialogBuilder(context: Context) : AlertDialog.Builder(context) {
    override fun show(): AlertDialog {
        val dialog = create()
        dialog.show()
        // Elements are modifed after show, because of https://issuetracker.google.com/issues/36913037
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setBackgroundColor(ContextCompat.getColor(context, R.color.text_primary_dark))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setBackgroundColor(ContextCompat.getColor(context, R.color.text_secondary_dark))
        return dialog
    }
}