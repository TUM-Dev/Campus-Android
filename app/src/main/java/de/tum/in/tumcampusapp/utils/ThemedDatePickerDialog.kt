package de.tum.`in`.tumcampusapp.utils

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R

class ThemedDatePickerDialog(context: Context, listener: OnDateSetListener, year: Int, month: Int, dayOfMonth: Int) :
    DatePickerDialog(context, listener, year, month, dayOfMonth) {
    override fun show() {
        window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        setOnShowListener {
            getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
        }
        return super.show()
    }
}
