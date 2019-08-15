package de.tum.`in`.tumcampusapp.component.ui.ticket.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration
import de.tum.`in`.tumcampusapp.component.ui.ticket.adapter.BoughtTicketAdapter
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketInfo
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsRemoteRepository
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_show_ticket.*
import javax.inject.Inject
import kotlin.math.roundToInt

class ShowTicketActivity : BaseActivity(R.layout.activity_show_ticket) {

    private lateinit var ticketInfoList: List<TicketInfo>
    private lateinit var event: Event

    @Inject
    internal lateinit var eventsLocalRepo: EventsLocalRepository

    @Inject
    internal lateinit var ticketsRemoteRepo: TicketsRemoteRepository

    @Inject
    internal lateinit var ticketsLocalRepo: TicketsLocalRepository

    private val compositeDisposable = CompositeDisposable()

    private val redemptionState: String
        get() {

            val redemptions = ticketInfoList.mapNotNull { it.tickets?.first()?.redemption }
            val lastRedemption = redemptions.max()

            val formattedDate = lastRedemption?.let {
                Ticket.getFormattedRedemptionDate(this, it)
            } ?: ""

            return when {
                redemptions.isEmpty() -> getString(R.string.not_redeemed_yet)
                redemptions.size < ticketInfoList.size -> {
                    val redeemed = redemptions.size
                    val all = ticketInfoList.size
                    getString(R.string.partially_redeemed, redeemed, all, formattedDate)
                }
                else -> getString(R.string.redeemed_at, formattedDate)
            }
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventId = intent.getIntExtra(Const.KEY_EVENT_ID, 0)
        injector.ticketsComponent()
                .eventId(eventId)
                .build()
                .inject(this)

        initViews()
        loadTicketData(eventId)
        setViewData()

        showQRCode()
        setWindowBrightnessToFull()
    }

    private fun showQRCode() {
        val qrCodeContent = ticketInfoList[0].tickets?.get(0)?.code ?: ""
        Utils.log(qrCodeContent)
        createQRCode(qrCodeContent)
    }

    private fun initViews() {
        ticketList.layoutManager = LinearLayoutManager(this)
        ticketList.setHasFixedSize(true)
        ticketList.isNestedScrollingEnabled = false

        val spacing = resources.getDimension(R.dimen.material_tiny_padding).roundToInt()
        ticketList.addItemDecoration(EqualSpacingItemDecoration(spacing))

        swipeRefreshLayout.setColorSchemeResources(
                R.color.color_primary,
                R.color.tum_A100,
                R.color.tum_A200
        )
        swipeRefreshLayout.setOnRefreshListener { loadRedemptionStatus() }
    }

    private fun loadRedemptionStatus() {
        val disposable = ticketsRemoteRepo
                .fetchTickets()
                .subscribe( { handleTicketRefreshSuccess(it) }, { handleTicketRefreshFailure() } )
        compositeDisposable.add(disposable)
    }

    private fun handleTicketRefreshSuccess(tickets: List<Ticket>) {
        ticketsLocalRepo.insert(*tickets.toTypedArray())
        ticketInfoList = ticketsLocalRepo.getTicketsByEventId(event.id)
        setViewData()
        swipeRefreshLayout.isRefreshing = false
    }

    private fun handleTicketRefreshFailure() {
        Utils.showToast(this, R.string.error_something_wrong)
        swipeRefreshLayout.isRefreshing = false
    }

    private fun loadTicketData(eventId: Int) {
        val ticketTypes = ticketsLocalRepo.getTicketTypesByEventId(eventId)
        if (ticketTypes.isEmpty()) {
            ticketsLocalRepo.addTicketTypes(ticketsRemoteRepo.fetchTicketTypesForEvent(eventId).blockingSingle())
        }
        ticketInfoList = ticketsLocalRepo.getTicketsByEventId(eventId)
        val eventForId = eventsLocalRepo.getEventById(eventId)
        if (eventForId == null) {
            finish()
            return
        } else {
            event = eventForId
        }
    }

    private fun setViewData() {
        eventTitle.text = event.title
        eventDateTime.text = event.getFormattedStartDateTime(this)
        redemptionStateTextView.text = redemptionState
        eventLocation.text = event.locality
        eventLocation.setOnClickListener { this.showMap(it) }

        ticketList?.adapter = BoughtTicketAdapter(ticketInfoList)
    }

    private fun showMap(view: View) {
        val url = "http://maps.google.co.in/maps?q=" + (view as TextView).text
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun setWindowBrightnessToFull() {
        window.attributes.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
    }

    private fun createQRCode(text: String) {
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200)
            val bitmap = BarcodeEncoder().createBitmap(bitMatrix)
            qrCode.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            Utils.log(e)
            Utils.showToast(this, R.string.error_something_wrong)
            finish()
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}


