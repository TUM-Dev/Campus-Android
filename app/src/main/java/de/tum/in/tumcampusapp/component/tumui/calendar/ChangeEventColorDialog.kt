package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Window
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.database.TcaDb
import kotlinx.android.synthetic.main.change_event_color_dialog.*

class ChangeEventColorDialog(
        context: Context,
        private val calendarItem: CalendarItem
) : Dialog(context) {

    private val calendarController: CalendarController by lazy {
        CalendarController(context)
    }

    private val eventColorProvider: EventColorProvider =
            EventColorProvider(context, TcaDb.getInstance(context).classColorDao())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(R.layout.change_event_color_dialog)

        // set default color
        val standardColor = EventColorProvider.getStandardColor(calendarItem)
        this.checkBoxDefault.buttonTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, standardColor))

        // check correct checkbox with the current color
        val currentColor = eventColorProvider.getResourceColor(calendarItem)
        val currentColorCheckboxId = getColorCheckboxIdByByColor(currentColor)
        this.findViewById<RadioButton>(currentColorCheckboxId).isChecked = true

        this.changeColorButton.setOnClickListener { changeEventColor() }
    }

    private fun changeEventColor() {
        val selectedColorBtnId = radioColor.checkedRadioButtonId
        val selectedColorBtn: RadioButton = findViewById(selectedColorBtnId)
        val selectedColor = getCustomColorByText(selectedColorBtn.text, calendarItem)
        eventColorProvider.changeEventColor(calendarItem, selectedColor)
        dismiss()
    }

    private fun getCustomColorByText(text: CharSequence, calendarItem: CalendarItem): Int {
        return when(text) {
            context.getString(R.string.custom_color_red) -> R.color.calendar_red
            context.getString(R.string.custom_color_pink) -> R.color.calendar_pink
            else -> EventColorProvider.getStandardColor(calendarItem)
        }
    }

    private fun getColorCheckboxIdByByColor(color: Int): Int {
        return when(color) {
            R.color.calendar_red -> R.id.checkBoxRed
            R.color.calendar_pink -> R.id.checkBoxPink
            else -> R.id.checkBoxDefault
        }
    }
}