package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.exception.RequestLimitReachedException
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CreateEventResponse
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.DeleteEventResponse
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.activity_create_event.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.UnknownHostException
import java.util.*

/**
 * Allows the user to create (and edit) a private event in TUMonline.
 */
class CreateEventActivity : ActivityForAccessingTumOnline<CreateEventResponse>(R.layout.activity_create_event) {

    private lateinit var start: DateTime
    private lateinit var end: DateTime

    private var isEditing: Boolean = false
    private var event: CalendarItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val closeIcon = ContextCompat.getDrawable(this, R.drawable.ic_clear)
        val color = ContextCompat.getColor(this, R.color.color_primary)
        if (closeIcon != null) {
            closeIcon.setTint(color)
            supportActionBar?.setHomeAsUpIndicator(closeIcon)
        }

        // We only use the SwipeRefreshLayout to indicate progress, not to allow
        // the user to pull to refresh.
        swipeRefreshLayout?.isEnabled = false

        eventTitleView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val isEmpty = s.toString().isEmpty()
                val alpha = if (isEmpty) 0.5f else 1.0f
                createEventButton.isEnabled = !isEmpty
                createEventButton.alpha = alpha
            }

            override fun afterTextChanged(s: Editable) {}
        })

        val extras = intent.extras
        if (extras != null) {
            // an event with extras can either be editing an existing event
            // or adding a new event from Tickets & Events
            isEditing = extras.getBoolean(Const.EVENT_EDIT)
            if (isEditing) {
                createEventButton.setText(R.string.event_save_edit_button)
            }
            eventTitleView.setText(extras.getString(Const.EVENT_TITLE))
            eventDescriptionView.setText(extras.getString(Const.EVENT_COMMENT))
        } else {
            eventTitleView.requestFocus()
            showKeyboard()
        }
        initStartEndDates(extras)
        setDateAndTimeListeners()

        createEventButton.setOnClickListener {
            if (end.isBefore(start)) {
                showErrorDialog(getString(R.string.create_event_time_error))
                return@setOnClickListener
            }
            if (isEditing) {
                editEvent()
            } else {
                createEvent()
            }
        }
    }

    private fun initStartEndDates(extras: Bundle?) {
        val initialDate = extras?.getSerializable(Const.DATE) as LocalDate?
        val startTime = extras?.getSerializable(Const.EVENT_START) as DateTime?
        val endTime = extras?.getSerializable(Const.EVENT_END) as DateTime?

        if (startTime == null || endTime == null) {
            if (initialDate == null) {
                throw IllegalStateException("No date provided for CreateEventActivity")
            }

            // We’re creating a new event, so we set the start and end time to the next full hour
            start = initialDate.toDateTimeAtCurrentTime()
                    .plusHours(1)
                    .withMinuteOfHour(0)
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(0)
            end = start.plusHours(1)
        } else {
            start = startTime
            end = endTime
        }

        updateDateViews()
        updateTimeViews()
    }

    private fun setDateAndTimeListeners() {
        // DATE

        // Month +/- 1 is needed because the date picker uses zero-based month values while DateTime
        // starts counting months at 1.
        eventStartDateView.setOnClickListener {
            hideKeyboard()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                start = start.withDate(year, month + 1, dayOfMonth)
                if (end.isBefore(start)) {
                    end = end.withDate(year, month + 1, dayOfMonth)
                }
                updateDateViews()
            }, start.year, start.monthOfYear - 1, start.dayOfMonth).show()
        }
        eventEndDateView.setOnClickListener {
            hideKeyboard()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                end = end.withDate(year, month + 1, dayOfMonth)
                updateDateViews()
            }, start.year, start.monthOfYear - 1, start.dayOfMonth).show()
        }

        // TIME
        eventStartTimeView.setOnClickListener { view ->
            hideKeyboard()
            TimePickerDialog(this, { timePicker, hour, minute ->
                val eventLength = end.millis - start.millis
                start = start.withHourOfDay(hour)
                        .withMinuteOfHour(minute)
                end = end.withMillis(start.millis + eventLength)
                updateTimeViews()
            }, start.hourOfDay, start.minuteOfHour, true).show()
        }

        eventEndTimeView.setOnClickListener { view ->
            hideKeyboard()
            TimePickerDialog(this, { timePicker, hour, minute ->
                end = end.withHourOfDay(hour)
                        .withMinuteOfHour(minute)
                updateTimeViews()
            }, end.hourOfDay, end.minuteOfHour, true).show()
        }
    }

    private fun updateTimeViews() {
        val format = DateTimeFormat.forPattern("HH:mm")
                .withLocale(Locale.getDefault())
        eventStartTimeView.text = format.print(start)
        eventEndTimeView.text = format.print(end)
    }

    private fun updateDateViews() {
        val format = DateTimeFormat.forPattern("EEE, dd.MM.yyyy")
                .withLocale(Locale.getDefault())
        eventStartDateView.text = format.print(start)
        eventEndDateView.text = format.print(end)
    }

    private fun editEvent() {
        val eventId = intent.getStringExtra(Const.EVENT_NR) ?: return

        // Because we don't show a loading screen for the delete request (only for the create
        // request), we use a short Toast to let the user know that something is happening.
        Toast.makeText(this, R.string.updating_event, Toast.LENGTH_SHORT).show()

        apiClient
                .deleteEvent(eventId)
                .enqueue(object : Callback<DeleteEventResponse> {
                    override fun onResponse(
                        call: Call<DeleteEventResponse>,
                        response: Response<DeleteEventResponse>
                    ) {
                        if (response.isSuccessful) {
                            Utils.log("Event successfully deleted (now creating the edited version)")
                            TcaDb.getInstance(this@CreateEventActivity).calendarDao().delete(eventId)
                            createEvent()
                        } else {
                            Utils.showToast(this@CreateEventActivity, R.string.error_unknown)
                        }
                    }

                    override fun onFailure(
                        call: Call<DeleteEventResponse>,
                        t: Throwable
                    ) {
                        Utils.log(t)
                        displayErrorMessage(t)
                    }
                })
    }

    private fun displayErrorMessage(throwable: Throwable) {
        val messageResId: Int = when (throwable) {
            is UnknownHostException -> R.string.error_no_internet_connection
            is RequestLimitReachedException -> R.string.error_request_limit_reached
            else -> R.string.error_unknown
        }
        Utils.showToast(this, messageResId)
    }

    private fun createEvent() {
        val event = CalendarItem()
        event.dtstart = start
        event.dtend = end

        var title = eventTitleView.text.toString()
        if (title.length > 255) {
            title = title.substring(0, 255)
        }
        event.title = title

        var description = eventDescriptionView.text.toString()
        if (description.length > 4000) {
            description = description.substring(0, 4000)
        }
        event.description = description
        this.event = event

        val apiCall = apiClient.createEvent(event, null)
        fetch(apiCall)
    }

    override fun onDownloadSuccessful(response: CreateEventResponse) {
        event?.let {
            it.nr = response.eventId
            TcaDb.getInstance(this).calendarDao().insert(it)
        }
        finish()
    }

    override fun onBackPressed() {
        hideKeyboard()

        val handled = handleOnBackPressed()
        if (handled) {
            finish()
        } else {
            displayCloseDialog()
        }
    }

    private fun showKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(eventTitleView.windowToken, 0)
    }

    private fun handleOnBackPressed(): Boolean {
        val title = eventTitleView.text.toString()
        val description = eventDescriptionView.text.toString()

        // TODO: If the user is in edit mode, check whether any data was changed.
        return title.isEmpty() && description.isEmpty()
    }

    private fun displayCloseDialog() {
        val dialog = AlertDialog.Builder(this)
                .setMessage(R.string.discard_changes_question)
                .setNegativeButton(R.string.discard) { dialogInterface, which -> finish() }
                .setPositiveButton(R.string.keep_editing, null)
                .create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showErrorDialog(message: String) {
        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(message)
                .setIcon(R.drawable.ic_error_outline)
                .setPositiveButton(R.string.ok, null)
                .create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
    }
}
