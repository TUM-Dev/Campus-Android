package de.tum.`in`.tumcampusapp.component.ui.ticket.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.view.View
import android.view.autofill.AutofillManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.stripe.android.*
import com.stripe.android.model.Source
import com.stripe.android.model.SourceCardData
import com.stripe.android.view.PaymentMethodsActivity
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.TicketEphemeralKeyProvider
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsLocalRepository
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.activity_payment_stripe.*
import kotlinx.android.synthetic.main.loading_overlay.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StripePaymentActivity : BaseActivity(R.layout.activity_payment_stripe) {

    private var paymentSession: PaymentSession? = null
    private var didSelectPaymentMethod: Boolean = false

    // Ticket ID, since the ticket was reserved in the prior activity and we need the ID to init the purchase
    private lateinit var ticketIds: List<Int>

    private lateinit var ticketPrice: String
    private lateinit var termsOfServiceLink: String
    private lateinit var stripePublishableKey: String

    private lateinit var localTicketRepo: TicketsLocalRepository

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        localTicketRepo = TicketsLocalRepository(TcaDb.getInstance(this))

        val ticketPrice = intent.getStringExtra(Const.KEY_TICKET_PRICE)
        val ticketIds = intent.getIntegerArrayListExtra(Const.KEY_TICKET_IDS)
        val termsOfServiceLink = intent.getStringExtra(Const.KEY_TERMS_LINK)
        val stripePublishableKey = intent.getStringExtra(Const.KEY_STRIPE_API_PUBLISHABLE_KEY)

        if (ticketIds == null || termsOfServiceLink == null ||
                ticketIds.isEmpty() ||
                ticketPrice == null ||
                termsOfServiceLink.isEmpty() ||
                stripePublishableKey == null) {
            Utils.showToast(this, R.string.error_something_wrong)
            finish()
            return
        } else {
            this.ticketPrice = ticketPrice
            this.ticketIds = ticketIds
            this.termsOfServiceLink = termsOfServiceLink
            this.stripePublishableKey = stripePublishableKey
        }

        initViews()
        initStripeSession()
    }

    private fun initViews() {
        val cardholder = Utils.getSetting(this, Const.KEY_CARD_HOLDER, "")
        cardholderEditText.setText(cardholder)
        cardholderEditText.setSelection(cardholder.length)

        if (cardholder.isEmpty()) {
            // We only request focus if the user has not entered their name. Otherwise, we assume
            // that the user will perform the payment method selection next.
            cardholderEditText.requestFocus()
        }

        cardholderEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val input = s.toString().trim { it <= ' ' }
                Utils.setSetting(this@StripePaymentActivity, Const.KEY_CARD_HOLDER, input)
                updateBuyButton()
            }

            override fun afterTextChanged(s: Editable) {}
        })

        selectPaymentMethodSwitcher.setOnClickListener { paymentSession?.presentPaymentMethodSelection() }

        completePurchaseButton.text = getString(R.string.buy_format_string, ticketPrice)
        completePurchaseButton.setOnClickListener { purchaseTicket() }

        termsOfServiceCheckBox.setOnClickListener { updateBuyButton() }
        termsOfServiceButton.setOnClickListener { view ->
            val browserIntent = Intent(Intent.ACTION_VIEW)
            browserIntent.data = Uri.parse(termsOfServiceLink)
            view.context.startActivity(browserIntent)
        }
    }

    private fun updateBuyButton() {
        val hasCardholder = cardholderEditText.text.toString().isNotEmpty()
        val enabled = (hasCardholder &&
                didSelectPaymentMethod &&
                termsOfServiceCheckBox.isChecked)
        val alpha = if (enabled) 1.0f else 0.5f

        completePurchaseButton.isEnabled = enabled
        completePurchaseButton.alpha = alpha
    }

    private fun purchaseTicket() {
        val cardholder = cardholderEditText.text.toString()
        showLoading(true)

        try {
            val methodId = paymentSession?.paymentSessionData?.selectedPaymentMethodId
            if (methodId == null) {
                Utils.showToast(this, R.string.error_something_wrong)
                return
            }

            TUMCabeClient
                    .getInstance(this)
                    .purchaseTicketStripe(this, ticketIds,
                            methodId, cardholder, object : Callback<List<Ticket>> {
                        override fun onResponse(
                            call: Call<List<Ticket>>,
                            response: Response<List<Ticket>>
                        ) {
                            val tickets = response.body()
                            if (tickets != null && tickets.isNotEmpty()) {
                                handleTicketPurchaseSuccess(tickets)
                            } else {
                                handleTicketPurchaseFailure()
                            }
                        }

                        override fun onFailure(call: Call<List<Ticket>>, t: Throwable) {
                            Utils.log(t)
                            handleTicketPurchaseFailure()
                        }
                    })
        } catch (e: NoPrivateKey) {
            Utils.log(e)
            handleTicketPurchaseFailure()
        }
    }

    private fun handleTicketPurchaseSuccess(tickets: List<Ticket>) {
        showLoading(false)
        localTicketRepo.insert(*tickets.toTypedArray())
        openPaymentConfirmation(tickets)
    }

    private fun openPaymentConfirmation(tickets: List<Ticket>) {
        val intent = Intent(this, PaymentConfirmationActivity::class.java)
        intent.putExtra(Const.KEY_EVENT_ID, tickets[0].eventId)
        intent.putExtra(Const.KEY_TICKET_AMOUNT, tickets.size)
        startActivity(intent)
    }

    private fun handleTicketPurchaseFailure() {
        showLoading(false)
        showError(getString(R.string.error_something_wrong))
    }

    private fun showLoading(isLoading: Boolean) {
        loadingLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
        TransitionManager.beginDelayedTransition(loadingLayout)
    }

    private fun showError(message: String) {
        val dialog = AlertDialog.Builder(this)
                .setTitle(getString(R.string.error))
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            // Data might be null if user tapped the back button from credit card selection
            return
        }

        val selectedSource = data.getStringExtra(PaymentMethodsActivity.EXTRA_SELECTED_PAYMENT)
        val source = Source.fromString(selectedSource)

        // Note: It isn't possible for a null or non-card source to be returned.
        if (source != null && Source.CARD == source.type) {
            val cardData = source.sourceTypeModel as SourceCardData

            val methodTextView = findViewById<TextView>(R.id.selected_payment_method_text_view)
            val cardBrandTextView = findViewById<TextView>(R.id.selected_payment_method_brand_text_view)

            methodTextView.text = buildCardString(cardData)
            cardBrandTextView.text = cardData.brand

            selectPaymentMethodSwitcher.showNext()

            didSelectPaymentMethod = true
            updateBuyButton()
        }
    }

    private fun initStripeSession() {
        PaymentConfiguration.init(stripePublishableKey)
        initCustomerSession()
    }

    private fun initCustomerSession() {
        CustomerSession.initCustomerSession(TicketEphemeralKeyProvider(this) { string ->
            if (string.startsWith("Error: ")) {
                showError(string)
                finish()
            } else {
                initPaymentSession()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requestAutofillIfEmptyCardholder()
                }
                showLoading(false)
            }
        })
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun requestAutofillIfEmptyCardholder() {
        if (cardholderEditText.text.toString().isEmpty()) {
            cardholderEditText.setAutofillHints(View.AUTOFILL_HINT_NAME)

            val autofillManager = getSystemService(AutofillManager::class.java)
            if (autofillManager != null && autofillManager.isEnabled) {
                autofillManager.requestAutofill(cardholderEditText)
            }
        }
    }

    private fun initPaymentSession() {
        val config = PaymentSessionConfig.Builder()
                .setShippingMethodsRequired(false)
                .setShippingInfoRequired(false)
                .build()

        paymentSession = PaymentSession(this)
        paymentSession?.init(object : PaymentSession.PaymentSessionListener {

            override fun onCommunicatingStateChanged(isCommunicating: Boolean) {
                loadingLayout.isVisible = isCommunicating
                TransitionManager.beginDelayedTransition(loadingLayout)
            }

            override fun onError(errorCode: Int, errorMessage: String?) {
                Utils.log("Error: ${errorMessage ?: "Unknown"}")
                showError(getString(R.string.customersession_init_failed))
            }

            override fun onPaymentSessionDataChanged(data: PaymentSessionData) {
                updateBuyButton()
                selectPaymentMethodSwitcher.isEnabled = true
            }
        }, config)
    }

    private fun buildCardString(data: SourceCardData) = getString(R.string.credit_card_format_string, data.last4)

    override fun onDestroy() {
        super.onDestroy()
        paymentSession?.onDestroy()
    }
}
