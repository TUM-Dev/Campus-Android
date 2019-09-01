package de.tum.`in`.tumcampusapp.component.ui.ticket.activity

import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.model.TUMCabeVerification
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration
import de.tum.`in`.tumcampusapp.component.ui.ticket.TicketAmountViewHolder
import de.tum.`in`.tumcampusapp.component.ui.ticket.adapter.TicketAmountAdapter
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketType
import de.tum.`in`.tumcampusapp.component.ui.ticket.payload.TicketReservation
import de.tum.`in`.tumcampusapp.component.ui.ticket.payload.TicketReservationResponse
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsRemoteRepository
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_buy_ticket.*
import kotlinx.android.synthetic.main.loading_overlay.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * This activity shows an overview of the available tickets and a selection of all ticket type.
 * It directs the user to the PaymentConfirmationActivity or back to EventDetailsActivity
 */
class BuyTicketActivity : BaseActivity(R.layout.activity_buy_ticket), TicketAmountViewHolder.SelectTicketInterface {

    private var eventId: Int = 0

    private var ticketTypes: List<TicketType>? = null
    private var currentTicketAmounts: IntArray? = null

    @Inject
    internal lateinit var ticketsRemoteRepo: TicketsRemoteRepository

    @Inject
    internal lateinit var eventsLocalRepo: EventsLocalRepository

    private val compositeDisposable = CompositeDisposable()

    private val totalPrice: Int
        get() {
            currentTicketAmounts?.let {
                val types = ticketTypes ?: arrayListOf()
                return it.zip(types).fold(0, { acc, amount -> acc + amount.first * amount.second.price })
            } ?: return 0
        }

    private val totalTickets: Int
        get() {
            if (currentTicketAmounts == null) {
                Utils.log("currentTicketAmounts not initialized")
                return 0
            }
            return currentTicketAmounts?.sum() ?: 0
        }

    private val ticketTypeIds: Array<Int>
        get() = ticketTypes?.map { it.id }?.toTypedArray() ?: emptyArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = intent.getIntExtra(Const.KEY_EVENT_ID, 0)

        totalPriceTextView.text = Utils.formatPrice(0)

        injector.ticketsComponent()
                .eventId(eventId)
                .build()
                .inject(this)

        // Get ticket type information from API
        val disposable = ticketsRemoteRepo.fetchTicketTypesForEvent(eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Utils.log(it) }
                .subscribe({ handleTicketTypesDownloadSuccess(it) }, {
                    Utils.showToast(this@BuyTicketActivity, R.string.error_something_wrong)
                    finish()
                })
        compositeDisposable.add(disposable)
    }

    private fun handleTicketTypesDownloadSuccess(ticketTypes: List<TicketType>) {
        this.ticketTypes = ticketTypes

        currentTicketAmounts = IntArray(ticketTypes.size) { 0 }
        setupUi()
    }

    private fun setupUi() {
        initEventTextViews()
        initTicketAmount()

        loadingLayout.isVisible = false
        paymentButton.setOnClickListener { reserveTicket() }
    }

    private fun initTicketAmount() {
        val ticketAmounts = findViewById<RecyclerView>(R.id.ticketAmountsRecyclerView)
        ticketAmounts.layoutManager = LinearLayoutManager(this)
        ticketAmounts.setHasFixedSize(true)
        ticketAmounts.adapter = TicketAmountAdapter(ticketTypes ?: emptyList())
        ticketAmounts.isNestedScrollingEnabled = false
        val spacing = resources.getDimension(R.dimen.material_small_padding).roundToInt()
        ticketAmounts.addItemDecoration(EqualSpacingItemDecoration(spacing))
    }

    override fun ticketAmountUpdated(ticketTypeId: Int, amount: Int) {
        currentTicketAmounts?.set(ticketTypeId, amount)
        totalPriceTextView.text = Utils.formatPrice(totalPrice)
    }

    private fun showError(title: Int, message: Int) {
        val dialog = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.ok, null)
                .create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
    }

    private fun initEventTextViews() {
        val event = eventsLocalRepo.getEventById(eventId)
        event?.let {
            eventName.text = it.title
            buyEventLocation.text = it.locality
            eventDate.text = it.getFormattedStartDateTime(this)
        }
    }

    private fun reserveTicket() {

        if (totalTickets == 0) {
            showError(R.string.error_no_ticket_selected, R.string.error_message_select_at_least_one_ticket)
            return
        }

        // don't allow user to click anything
        showLoadingLayout(true)

        val reservation = TicketReservation(ticketTypeIds, currentTicketAmounts?.toTypedArray() ?: emptyArray())
        val verification = TUMCabeVerification.create(this, reservation)
        if (verification == null) {
            handleTicketReservationFailure(R.string.internal_error)
            return
        }

        TUMCabeClient
                .getInstance(this)
                .reserveTicket(verification, object : Callback<TicketReservationResponse> {
                    override fun onResponse(
                        call: Call<TicketReservationResponse>,
                        response: Response<TicketReservationResponse>
                    ) {
                        // ResponseBody can be null if the user has already bought a ticket
                        // but has not fetched it from the server yet
                        val reservationResponse = response.body()
                        if (response.isSuccessful &&
                                reservationResponse != null &&
                                reservationResponse.error == null) {
                            handleTicketReservationSuccess(reservationResponse)
                        } else {
                            if (reservationResponse == null || !response.isSuccessful) {
                                handleTicketNotReserved()
                            } else {
                                handleTicketReservationFailure(R.string.event_imminent_error)
                                finish()
                            }
                        }
                    }

                    override fun onFailure(call: Call<TicketReservationResponse>, t: Throwable) {
                        Utils.log(t)
                        handleTicketReservationFailure(R.string.error_something_wrong)
                    }
                })
    }

    private fun handleTicketReservationSuccess(response: TicketReservationResponse) {
        showLoadingLayout(false)

        val intent = Intent(this, StripePaymentActivity::class.java)
        intent.putExtra(Const.KEY_TICKET_PRICE, Utils.formatPrice(totalPrice))
        intent.putIntegerArrayListExtra(Const.KEY_TICKET_IDS, response.ticketIds)
        intent.putExtra(Const.KEY_TERMS_LINK, ticketTypes?.get(0)?.paymentInfo?.termsLink ?: "")
        intent.putExtra(Const.KEY_STRIPE_API_PUBLISHABLE_KEY, ticketTypes?.get(0)?.paymentInfo?.stripePublicKey ?: "")
        startActivity(intent)
    }

    private fun handleTicketNotReserved() {
        val dialog = AlertDialog.Builder(this)
                .setTitle(getString(R.string.error))
                .setMessage(getString(R.string.ticket_not_fetched))
                .setPositiveButton(R.string.ok) { _, _ ->
                    showLoadingLayout(false)
                }
                .create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
    }

    private fun handleTicketReservationFailure(messageResId: Int) {
        showLoadingLayout(false)
        Utils.showToast(this, messageResId)
    }

    private fun showLoadingLayout(show: Boolean) {
        loadingLayout.isVisible = show
        TransitionManager.beginDelayedTransition(loadingLayout)
        paymentButton.isEnabled = show.not()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
