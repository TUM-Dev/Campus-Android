package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import kotlinx.android.synthetic.main.change_event_color_dialog.*

class ChangeEventColorDialog(
        context: Context,
        private val calendarItem: CalendarItem,
        private val onColorChanged: (OnColorChangedData?) -> Unit,
        private val fromCreateEventActivity: Boolean
) : Dialog(context) {

    private val eventColorController: EventColorController = EventColorController(context)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(R.layout.change_event_color_dialog)

        if (fromCreateEventActivity) {
            this.repeatingSwitch.visibility = View.GONE
        }

        // set default color
        val standardColor = EventColorController.getStandardColor(calendarItem)
        this.checkBoxDefault.buttonTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, standardColor))

        // check correct checkbox with the current color
        val currentColor = eventColorController.getResourceColor(calendarItem)
        val currentColorCheckboxId = getColorCheckboxIdByByColor(currentColor)
        this.findViewById<RadioButton>(currentColorCheckboxId).isChecked = true

        this.changeColorButton.setOnClickListener { changeEventColor() }
    }

    private fun changeEventColor() {
        val selectedColorBtnId = radioColor.checkedRadioButtonId
        val selectedColorBtn: RadioButton = findViewById(selectedColorBtnId)
        val selectedColor = getCustomColorByText(selectedColorBtn.text, calendarItem)

        if (fromCreateEventActivity) {
            onColorChanged(OnColorChangedData(selectedColorBtn.text, selectedColor))
        } else {
            eventColorController.changeEventColor(calendarItem, selectedColor, !repeatingSwitch.isChecked)
            onColorChanged(null)
        }
        dismiss()
    }

    private fun getCustomColorByText(text: CharSequence, calendarItem: CalendarItem): Int {
        return when(text) {
            context.getString(R.string.custom_color_red) -> R.color.calendar_red
            context.getString(R.string.custom_color_pink) -> R.color.calendar_pink
            context.getString(R.string.custom_color_purple) -> R.color.calendar_purple
            context.getString(R.string.custom_color_indigo) -> R.color.calendar_indigo
            context.getString(R.string.custom_color_blue) -> R.color.calendar_blue
            context.getString(R.string.custom_color_teal) -> R.color.calendar_teal
            context.getString(R.string.custom_color_green) -> R.color.calendar_green
            context.getString(R.string.custom_color_lime) -> R.color.calendar_lime
            context.getString(R.string.custom_color_yellow) -> R.color.calendar_yellow
            context.getString(R.string.custom_color_amber) -> R.color.calendar_amber
            context.getString(R.string.custom_color_orange) -> R.color.calendar_orange
            else -> EventColorController.getStandardColor(calendarItem)
        }
    }

    private fun getColorCheckboxIdByByColor(color: Int): Int {
        return when(color) {
            R.color.calendar_red -> R.id.checkBoxRed
            R.color.calendar_pink -> R.id.checkBoxPink
            R.color.calendar_purple -> R.id.checkBoxPurple
            R.color.calendar_indigo -> R.id.checkBoxIndigo
            R.color.calendar_blue -> R.id.checkBoxBlue
            R.color.calendar_teal -> R.id.checkBoxTeal
            R.color.calendar_green -> R.id.checkBoxGreen
            R.color.calendar_lime -> R.id.checkBoxLime
            R.color.calendar_yellow -> R.id.checkBoxYellow
            R.color.calendar_amber -> R.id.checkBoxAmber
            R.color.calendar_orange -> R.id.checkBoxOrange
            else -> R.id.checkBoxDefault
        }
    }

    data class OnColorChangedData(
            val text: CharSequence,
            val color: Int
    )
}