package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import de.tum.`in`.tumcampusapp.R

object Dialogs {

    // TODO: Theming

    fun showMultiChoiceDialog(
            context: Context,
            items: List<String>,
            checkedItems: List<Boolean>,
            listener: DialogInterface.OnMultiChoiceClickListener
    ) {
        val dialog = AlertDialog.Builder(context)
                .setMultiChoiceItems(items.toTypedArray(), checkedItems.toBooleanArray(), listener)
                .create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
    }

}
