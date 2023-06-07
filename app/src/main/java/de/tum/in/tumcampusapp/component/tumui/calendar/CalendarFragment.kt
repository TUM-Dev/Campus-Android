package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import de.tum.`in`.tumcampusapp.utils.ThemedAlertDialogBuilder
import android.provider.CalendarContract
import android.text.format.DateUtils
import android.view.*
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alamkanak.weekview.DateTimeInterpreter
import com.alamkanak.weekview.WeekViewDisplayable
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.notifications.persistence.NotificationType
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.Event
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.EventsResponse
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportController
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.databinding.FragmentCalendarBinding
import de.tum.`in`.tumcampusapp.service.QueryLocationService
import de.tum.`in`.tumcampusapp.utils.*
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.time.YearMonth
import java.util.*

class CalendarFragment :
    FragmentForAccessingTumOnline<EventsResponse>(
        R.layout.fragment_calendar,
        R.string.calendar
    ),
    CalendarDetailsFragment.OnEventInteractionListener {

    private val calendarController: CalendarController by lazy {
        CalendarController(requireContext())
    }

    private val showDate: DateTime? by lazy {
        val value = arguments?.getLong(Const.EVENT_TIME, -1) ?: -1
        if (value != -1L) DateTime(value) else null
    }

    private val eventId: String by lazy {
        val value = arguments?.getString(Const.KEY_EVENT_ID)
        value ?: ""
    }

    private enum class ViewMode {
        DAY {
            override fun numberOfVisibleDays(): Int {
                return 1
            }
        },
        WEEK {
            override fun numberOfVisibleDays(): Int {
                return 5
            }
        },
        MONTH {
            override fun numberOfVisibleDays(): Int {
                return 30
            }
        };

        abstract fun numberOfVisibleDays(): Int
    }

    private var viewMode = ViewMode.MONTH

    private var isFetched: Boolean = false
    private var menuItemSwitchView: MenuItem? = null
    private var menuItemFilterCanceled: MenuItem? = null

    private val compositeDisposable = CompositeDisposable()

    private var detailsFragment: CalendarDetailsFragment? = null

    private val binding by viewBinding(FragmentCalendarBinding::bind)

    private lateinit var monthYearText: TextView
    private lateinit var monthRecyclerView: RecyclerView
    private var selectedDate: LocalDate = LocalDate.now()
    private lateinit var monthViewAdapter: MonthViewAdapter

    override val swipeRefreshLayout get() = binding.swipeRefreshLayout
    override val layoutAllErrorsBinding get() = binding.layoutAllErrors

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            // The week view adds a horizontal bar below the Toolbar. When refreshing, the refresh
            // spinner covers it. Therefore, we adjust the spinner's end position.
            swipeRefreshLayout.let {
                val startOffset = it.progressViewStartOffset
                val endOffset = it.progressViewEndOffset
                it.setProgressViewOffset(false, startOffset, endOffset)
            }

            weekView.setOnMonthChangeListener { startDate, endDate ->
                val begin = DateTime(startDate)
                val end = DateTime(endDate)
                prepareCalendarItems(begin, end)
            }

            weekView.setOnEventClickListener { data, _ ->
                openEvent(data as CalendarItem)
            }

            todayButton.setOnClickListener { weekView.goToToday() }
        }

        showDate?.let { openEvent(eventId) }

        viewMode = ViewMode.valueOf(Utils.getSetting(requireContext(), Const.CALENDAR_VIEW_MODE, ViewMode.MONTH.toString()))

        disableRefresh()

        monthYearText = binding.layoutMonth.monthYearText
        monthRecyclerView = binding.layoutMonth.monthGrid
        refreshMonthView()
        binding.layoutMonth.monthBackButton.setOnClickListener {
            selectedDate = selectedDate.minusMonths(1)
            refreshMonthView()
        }
        binding.layoutMonth.monthForwardButton.setOnClickListener {
            selectedDate = selectedDate.plusMonths(1)
            refreshMonthView()
        }

        // Tracks whether the user has used the calendar module before. This is used in determining when to prompt for a
        // Google Play store review
        Utils.setSetting(requireContext(), Const.HAS_VISITED_CALENDAR, true)
    }

    override fun onStart() {
        super.onStart()
        refreshWeekView()
        refreshMonthView()
        // In case the timezone changes when reopening the calendar, while the app is still open, this ensures
        // that the lectures are still adjusted to the new timezone
        loadEvents(CacheControl.BYPASS_CACHE)
    }

    override fun onRefresh() {
        refresh()
    }

    fun refresh() {
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
        QueryLocationService.enqueueWork(requireContext())
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val menuItemExportGoogle = menu.findItem(R.id.action_export_calendar)
        val menuItemDeleteCalendar = menu.findItem(R.id.action_delete_calendar)

        menuItemExportGoogle?.isEnabled = isFetched
        menuItemDeleteCalendar?.isEnabled = isFetched

        val autoSyncCalendar = Utils.getSettingBool(requireContext(), Const.SYNC_CALENDAR, false)
        menuItemExportGoogle?.isVisible = !autoSyncCalendar
        menuItemDeleteCalendar?.isVisible = autoSyncCalendar
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sync_calendar, menu)

        menuItemSwitchView = menu.findItem(R.id.action_switch_view_mode)
        menuItemFilterCanceled = menu.findItem(R.id.action_calendar_filter_canceled)

        refreshWeekView()
        initFilterCheckboxes()

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_switch_view_mode -> {
                viewMode = when (viewMode) {
                    ViewMode.DAY -> ViewMode.WEEK
                    ViewMode.WEEK -> ViewMode.MONTH
                    ViewMode.MONTH -> ViewMode.DAY
                }
                Utils.setSetting(requireContext(), Const.CALENDAR_VIEW_MODE, viewMode.toString())
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
                val currentDate = LocalDate(binding.weekView.firstVisibleDate)
                val intent = Intent(requireContext(), CreateEventActivity::class.java)
                intent.putExtra(Const.DATE, currentDate)
                startForResult.launch(intent)
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
        if (checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR) ||
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALENDAR)
            ) {
                ThemedAlertDialogBuilder(requireContext())
                    .setMessage(getString(R.string.permission_calendar_explanation))
                    .setPositiveButton(R.string.ok) { _, _ ->
                        showPermissionRequestDialog(id)
                    }
                    .show()
            } else {
                showPermissionRequestDialog(id)
            }
        }
        return false
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasPermissions = permissions.all { it.value }
        if (hasPermissions) {
            if (calendarController.requestCode == REQUEST_SYNC) {
                exportCalendarToGoogle()
            } else if (calendarController.requestCode == REQUEST_DELETE) {
                deleteCalendarFromGoogle()
            }
        }
    }

    private fun showPermissionRequestDialog(id: Int) {
        calendarController.requestCode = id
        requestPermissionLauncher.launch(PERMISSIONS_CALENDAR)
    }

    private fun displayCalendarSyncSuccessDialog() {
        ThemedAlertDialogBuilder(requireContext())
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

            while (i + 1 < calendarItems.size && calendarItem.isSameEventButForLocation(
                    calendarItems[i + 1]
                )
            ) {
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
        detailsFragment?.show(childFragmentManager, null)
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
        startForResult.launch(intent)
        detailsFragment?.dismiss()
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode != Activity.RESULT_OK) {
                val failed = result.data?.getStringExtra("failed") ?: 1
                val sum = result.data?.getStringExtra("sum") ?: 1
                ThemedAlertDialogBuilder(requireContext())
                    .setTitle(R.string.error_something_wrong)
                    .setMessage(getString(R.string.create_event_some_failed, failed, sum))
                    .setPositiveButton(R.string.ok, null)
                    .show()
            }
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
        setupDateTimeInterpreter(viewMode == ViewMode.WEEK)

        val icon = when (viewMode) {
            ViewMode.DAY -> R.drawable.ic_outline_calendar_view_month_24px
            ViewMode.WEEK -> R.drawable.ic_outline_calendar_view_day_24px
            ViewMode.MONTH -> R.drawable.ic_outline_view_column_24px
        }

        when (viewMode) {
            ViewMode.DAY, ViewMode.WEEK -> {
                binding.layoutWeek.visibility = View.VISIBLE
                binding.layoutMonth.root.visibility = View.GONE
                binding.weekView.numberOfVisibleDays = viewMode.numberOfVisibleDays()
            }

            ViewMode.MONTH -> {
                binding.layoutWeek.visibility = View.GONE
                binding.layoutMonth.root.visibility = View.VISIBLE
            }
        }

        // Go to current date or the one given in the intent
        showDate?.let {
            binding.weekView.goToDate(it.toGregorianCalendar())
            binding.weekView.goToHour(it.hourOfDay)
        } ?: run {
            binding.weekView.firstVisibleDate?.let {
                binding.weekView.goToDate(it)
            } ?: run {
                binding.weekView.goToCurrentTime()
            }
        }

        menuItemSwitchView?.setIcon(icon)
    }

    private fun setupDateTimeInterpreter(shortDate: Boolean) {
        binding.weekView.dateTimeInterpreter = object : DateTimeInterpreter {

            private val timeFormat =
                DateTimeFormat.forPattern("HH:mm").withLocale(Locale.getDefault())

            override fun interpretDate(date: Calendar): String {
                val weekDayFormat = if (shortDate) "E" else "EEEE"
                val weekDay = DateTimeFormat.forPattern(weekDayFormat)
                    .withLocale(Locale.getDefault())
                    .print(DateTime(date.timeInMillis))

                val dateString = DateUtils.formatDateTime(
                    requireContext(), date.timeInMillis,
                    DateUtils.FORMAT_NUMERIC_DATE or DateUtils.FORMAT_NO_YEAR
                )

                return weekDay.uppercase(Locale.getDefault()) + ' '.toString() + dateString
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

        ThemedAlertDialogBuilder(requireContext())
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

    private fun refreshMonthView() {
        monthYearText.text = formatLocalDate(selectedDate)
        val daysInMonth = daysInMonth(selectedDate)

        val eventMap = calendarController.getEventsForMonth(selectedDate)

        if (!::monthViewAdapter.isInitialized) {
            monthViewAdapter = MonthViewAdapter(daysInMonth, eventMap)
            monthRecyclerView.adapter = monthViewAdapter
        } else {
            monthViewAdapter.updateData(daysInMonth, eventMap)
        }

        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(requireContext(), 7)
        monthRecyclerView.layoutManager = layoutManager
    }

    private fun daysInMonth(date: LocalDate): ArrayList<String> {
        val daysInMonthArray: ArrayList<String> = ArrayList()
        val yearMonth = YearMonth.of(date.year, date.monthOfYear)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstOfMonth = date.withDayOfMonth(1)
        var dayOfWeek = firstOfMonth.dayOfWeek().get() - 1 // Monday is the first day of the week in Europe
        for (i in 1..42) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("")
            } else {
                daysInMonthArray.add((i - dayOfWeek).toString())
            }
        }
        return daysInMonthArray
    }

    private fun formatLocalDate(date: LocalDate): String {
        val formatter = DateTimeFormat.forPattern("MMMM yyyy")
        return formatter.print(date.withDayOfMonth(1))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        menuItemSwitchView = null
        menuItemFilterCanceled = null
    }

    companion object {

        private const val REQUEST_SYNC = 0
        private const val REQUEST_DELETE = 1
        private const val REQUEST_CREATE = 2
        const val RESULT_ERROR = 4

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
