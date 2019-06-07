package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CalendarContract
import android.text.format.DateUtils
import android.transition.TransitionManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.checkSelfPermission
import com.alamkanak.weekview.DateTimeInterpreter
import com.alamkanak.weekview.ScrollListener
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.setMonthChangeListener
import com.alamkanak.weekview.setOnEventClickListener
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.notifications.persistence.NotificationType
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.Event
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.EventsResponse
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportController
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.service.QueryLocationsService
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.FontUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_calendar.todayButton
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale

class CalendarFragment : FragmentForAccessingTumOnline<EventsResponse>(
    R.layout.fragment_calendar,
    R.string.calendar
), CalendarDetailsFragment.OnEventInteractionListener {

    private val calendarController: CalendarController by lazy {
        CalendarController(requireContext())
    }

    private val weekView: WeekView<CalendarItem> by lazy {
        requireActivity().findViewById<WeekView<CalendarItem>>(R.id.weekView)
    }

    private val showDate: DateTime? by lazy {
        val value = arguments?.getLong(Const.EVENT_TIME, -1) ?: -1
        if (value != -1L) DateTime(value) else null
    }

    private val eventId: String by lazy {
        checkNotNull(arguments?.getString(Const.KEY_EVENT_ID))
    }

    private var isWeekMode = false

    private var isFetched: Boolean = false
    private var menuItemSwitchView: MenuItem? = null
    private var menuItemFilterCanceled: MenuItem? = null

    private val compositeDisposable = CompositeDisposable()

    private var detailsFragment: CalendarDetailsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The week view adds a horizontal bar below the Toolbar. When refreshing, the refresh
        // spinner covers it. Therefore, we adjust the spinner's end position.
        swipeRefreshLayout?.let {
            val startOffset = it.progressViewStartOffset
            val endOffset = it.progressViewEndOffset
            it.setProgressViewOffset(false, startOffset, endOffset)
        }

        weekView.setMonthChangeListener { startDate, endDate ->
            val begin = DateTime(startDate)
            val end = DateTime(endDate)
            prepareCalendarItems(begin, end)
        }

        weekView.setOnEventClickListener { data, _ ->
            openEvent(data)
        }

        weekView.scrollListener = object : ScrollListener {
            override fun onFirstVisibleDayChanged(
                    newFirstVisibleDay: Calendar,
                    oldFirstVisibleDay: Calendar?
            ) {
                val visibleDay = LocalDate(newFirstVisibleDay)
                val today = LocalDate.now()
                val isToday = visibleDay.isEqual(today)

                todayButton.visibility = if (isToday) View.GONE else View.VISIBLE
                TransitionManager.beginDelayedTransition(swipeRefreshLayout)
            }
        }

        todayButton.setOnClickListener { weekView.goToToday() }
        showDate?.let { openEvent(eventId) }

        isWeekMode = arguments?.getBoolean(Const.CALENDAR_WEEK_MODE) ?: false

        disableRefresh()
        loadEvents(CacheControl.USE_CACHE)
    }

    override fun onStart() {
        super.onStart()
        refreshWeekView()
    }

    override fun onRefresh() {
        loadEvents(CacheControl.BYPASS_CACHE)
    }

    private fun loadEvents(cacheControl: CacheControl) {
        val apiCall = apiClient.getCalendar(cacheControl)
        fetch(apiCall)
    }

    override fun onDownloadSuccessful(response: EventsResponse) {
        isFetched = true

        val events = response.events ?: return
        scheduleNotifications(events)

        compositeDisposable += Completable
                .fromAction { calendarController.importCalendar(events) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onCalendarImportedIntoDatabase)
    }

    private fun onCalendarImportedIntoDatabase() {
        // Update the action bar to display the enabled menu options
        requireActivity().invalidateOptionsMenu()
        QueryLocationsService.enqueueWork(requireContext())
    }

    private fun scheduleNotifications(events: List<Event>) {
        if (calendarController.hasNotificationsEnabled()) {
            calendarController.scheduleNotifications(events)
        }

        val transportController = TransportController(requireContext())
        if (transportController.hasNotificationsEnabled()) {
            transportController.scheduleNotifications(events)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val menuItemExportGoogle = menu?.findItem(R.id.action_export_calendar)
        val menuItemDeleteCalendar = menu?.findItem(R.id.action_delete_calendar)

        menuItemExportGoogle?.isEnabled = isFetched
        menuItemDeleteCalendar?.isEnabled = isFetched

        val autoSyncCalendar = Utils.getSettingBool(requireContext(), Const.SYNC_CALENDAR, false)
        menuItemExportGoogle?.isVisible = !autoSyncCalendar
        menuItemDeleteCalendar?.isVisible = autoSyncCalendar
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_sync_calendar, menu)

        menuItemSwitchView = menu?.findItem(R.id.action_switch_view_mode)
        menuItemFilterCanceled = menu?.findItem(R.id.action_calendar_filter_canceled)

        refreshWeekView()
        initFilterCheckboxes()

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_switch_view_mode -> {
                isWeekMode = !isWeekMode
                Utils.setSetting(requireContext(), Const.CALENDAR_WEEK_MODE, isWeekMode)
                refreshWeekView()
                return true
            }
            R.id.action_export_calendar -> {
                exportCalendarToGoogle()

                // Enable automatic calendar synchronisation
                Utils.setSetting(requireContext(), Const.SYNC_CALENDAR, true)
                requireActivity().invalidateOptionsMenu()
                return true
            }
            R.id.action_delete_calendar -> {
                deleteCalendarFromGoogle()
                return true
            }
            R.id.action_create_event -> {
                val currentDate = LocalDate(weekView.firstVisibleDay)
                val intent = Intent(requireContext(), CreateEventActivity::class.java)
                intent.putExtra(Const.DATE, currentDate)
                startActivity(intent)
                return true
            }
            R.id.action_calendar_filter_canceled -> {
                item.isChecked = !item.isChecked
                applyFilterCanceled(item.isChecked)
                return true
            }
            R.id.action_update_calendar -> {
                loadEvents(CacheControl.BYPASS_CACHE)
                refreshWeekView()
                return true
            }
            else -> {
                isFetched = false
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun initFilterCheckboxes() {
        val showCancelledEvents =
                Utils.getSettingBool(requireContext(), Const.CALENDAR_FILTER_CANCELED, true)
        Utils.log(if (showCancelledEvents) "Show cancelled events" else "Hide cancelled events")

        menuItemFilterCanceled?.isChecked = showCancelledEvents
        applyFilterCanceled(showCancelledEvents)
    }

    private fun applyFilterCanceled(value: Boolean) {
        Utils.setSetting(requireContext(), Const.CALENDAR_FILTER_CANCELED, value)
        refreshWeekView()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        val hasPermissions = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (!hasPermissions) {
            return
        }

        if (requestCode == REQUEST_SYNC) {
            exportCalendarToGoogle()
        } else if (requestCode == REQUEST_DELETE) {
            deleteCalendarFromGoogle()
        }
    }

    /**
     * Asynchronous task for exporting the calendar to a local Google calendar
     */
    private fun exportCalendarToGoogle() {
        // Check Calendar permission for Android 6.0
        if (!isPermissionGranted(REQUEST_SYNC)) {
            return
        }

        compositeDisposable += Completable
                .fromAction { CalendarController.syncCalendar(requireContext()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (isAdded) {
                        displayCalendarSyncSuccessDialog()
                    }
                }, { throwable ->
                    Utils.log(throwable)
                    Utils.showToast(requireContext(), R.string.export_to_google_error)
                })
    }

    private fun isPermissionGranted(id: Int): Boolean {
        if (checkSelfPermission(requireContext(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(requireContext(), Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            return true
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR)
                    || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALENDAR)) {
                AlertDialog.Builder(requireContext())
                        .setMessage(getString(R.string.permission_calendar_explanation))
                        .setPositiveButton(R.string.ok) { _, _ ->
                            requestPermissions(PERMISSIONS_CALENDAR, id)
                        }
                        .show()
            } else {
                requestPermissions(PERMISSIONS_CALENDAR, id)
            }
        }

        return false
    }

    private fun displayCalendarSyncSuccessDialog() {
        AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.dialog_show_calendar))
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes) { _, _ -> displayCalendarOnGoogleCalendar() }
                .show()
    }

    private fun displayCalendarOnGoogleCalendar() {
        val startMillis = DateTime.now().millis
        val builder = CalendarContract.CONTENT_URI.buildUpon()
        builder.appendPath("time")
        ContentUris.appendId(builder, startMillis)
        val intent = Intent(Intent.ACTION_VIEW).setData(builder.build())
        startActivity(intent)
    }

    private fun prepareCalendarItems(
            begin: DateTime,
            end: DateTime
    ): List<WeekViewDisplayable<CalendarItem>> {
        val showCancelledEvents =
                Utils.getSettingBool(requireContext(), Const.CALENDAR_FILTER_CANCELED, true)

        val calendarItems = if (showCancelledEvents) {
            calendarController.getFromDbBetweenDates(begin, end)
        } else {
            calendarController.getFromDbNotCancelledBetweenDates(begin, end)
        }

        return mergeSimilarCalendarItems(calendarItems)
    }

    /**
     * Creates one event out of multiple instances of the same event that have different locations.
     * List must already be sorted so that event duplicates are right after each other.
     */
    private fun mergeSimilarCalendarItems(calendarItems: List<CalendarItem>): List<WeekViewDisplayable<CalendarItem>> {
        val events = ArrayList<WeekViewDisplayable<CalendarItem>>()

        var i = 0
        while (i < calendarItems.size) {
            val calendarItem = calendarItems[i]
            val location = StringBuilder()
            location.append(calendarItem.location)

            while (i + 1 < calendarItems.size && calendarItem.isSameEventButForLocation(calendarItems[i + 1])) {
                i++
                location.append(" + ")
                location.append(calendarItems[i].location)
            }

            calendarItem.location = location.toString()
            events.add(calendarItem)
            i++
        }

        return events
    }

    private fun openEvent(eventId: String) {
        val items = checkNotNull(calendarController.getCalendarItemAndDuplicatesById(eventId))
        if (items.isEmpty()) {
            return
        }

        openEvent(items.first())
    }

    private fun openEvent(event: CalendarItem) {
        detailsFragment = CalendarDetailsFragment.newInstance(event.nr, true, this)
        detailsFragment?.show(requireFragmentManager(), null)
    }

    override fun onEditEvent(calendarItem: CalendarItem) {
        val bundle = Bundle().apply {
            putBoolean(Const.EVENT_EDIT, true)
            putString(Const.EVENT_TITLE, calendarItem.title)
            putString(Const.EVENT_COMMENT, calendarItem.description)
            putSerializable(Const.EVENT_START, calendarItem.dtstart)
            putSerializable(Const.EVENT_END, calendarItem.dtend)
            putString(Const.EVENT_NR, calendarItem.nr)
        }

        val intent = Intent(requireContext(), CreateEventActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
        detailsFragment?.dismiss()
    }

    override fun onEventDeleted(eventId: String) {
        val db = TcaDb.getInstance(requireContext())
        db.calendarDao().delete(eventId)

        val id = eventId.toInt()
        db.scheduledNotificationsDao().delete(NotificationType.CALENDAR.id, id)

        refreshWeekView()
        Utils.showToast(requireContext(), R.string.delete_event_confirmation)
    }

    private fun refreshWeekView() {
        setupDateTimeInterpreter(isWeekMode)
        val icon: Int

        if (isWeekMode) {
            icon = R.drawable.ic_outline_calendar_view_day_24px
            weekView.numberOfVisibleDays = 5
            weekView.setEventTextSize(FontUtils.getFontSizeInPx(requireContext(), 12f))
        } else {
            icon = R.drawable.ic_outline_view_column_24px
            weekView.numberOfVisibleDays = 1
            weekView.setEventTextSize(FontUtils.getFontSizeInPx(requireContext(), 14f))
        }

        // Go to current date or the one given in the intent
        showDate?.let {
            weekView.goToDate(it.toGregorianCalendar())
            weekView.goToHour(it.hourOfDay)
        } ?: weekView.goToCurrentTime()

        menuItemSwitchView?.setIcon(icon)
    }

    private fun setupDateTimeInterpreter(shortDate: Boolean) {
        weekView.dateTimeInterpreter = object : DateTimeInterpreter {

            private val timeFormat = DateTimeFormat.forPattern("HH:mm").withLocale(Locale.getDefault())

            override fun interpretDate(date: Calendar): String {
                val weekDayFormat = if (shortDate) "E" else "EEEE"
                val weekDay = DateTimeFormat.forPattern(weekDayFormat)
                        .withLocale(Locale.getDefault())
                        .print(DateTime(date.timeInMillis))

                val dateString = DateUtils.formatDateTime(
                        requireContext(), date.timeInMillis,
                        DateUtils.FORMAT_NUMERIC_DATE or DateUtils.FORMAT_NO_YEAR)

                return weekDay.toUpperCase(Locale.getDefault()) + ' '.toString() + dateString
            }

            override fun interpretTime(hour: Int): String {
                val time = DateTime().withTime(hour, 0, 0, 0)
                return timeFormat.print(time)
            }
        }
    }

    private fun deleteCalendarFromGoogle() {
        if (!isPermissionGranted(REQUEST_DELETE)) {
            return
        }

        AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.dialog_delete_calendar))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    val deleted = CalendarController.deleteLocalCalendar(requireContext())
                    Utils.setSetting(requireContext(), Const.SYNC_CALENDAR, false)
                    requireActivity().invalidateOptionsMenu()

                    if (deleted > 0) {
                        Utils.showToast(requireContext(), R.string.calendar_deleted_toast)
                    } else {
                        Utils.showToast(requireContext(), R.string.calendar_not_existing_toast)
                    }
                }
                .setNegativeButton(getString(R.string.no), null)
                .show()
    }

    companion object {

        private const val REQUEST_SYNC = 0
        private const val REQUEST_DELETE = 1

        private val PERMISSIONS_CALENDAR = arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
        )

        @JvmStatic
        fun newInstance(
                date: Long? = null,
                eventId: String? = null
        ) = CalendarFragment().apply {

        }

    }

}
