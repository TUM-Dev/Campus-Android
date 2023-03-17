package de.tum.`in`.tumcampusapp.utils

import android.app.TimePickerDialog
import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R

class ThemedTimePickerDialog(context: Context, listener: OnTimeSetListener, hourOfDay: Int, minute: Int) :
        TimePickerDialog(context, listener, hourOfDay, minute, true) {
    override fun show() {
        window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        setOnShowListener {
            getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
        }
        return super.show()
    }
}