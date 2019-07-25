package de.tum.`in`.tumcampusapp.component.ui.overview

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.BaseFragment
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.di.ViewModelFactory
import de.tum.`in`.tumcampusapp.di.injector
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import de.tum.`in`.tumcampusapp.service.SilenceService
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.observe
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.connectivityManager
import org.jetbrains.anko.support.v4.runOnUiThread
import javax.inject.Inject
import javax.inject.Provider

class MainFragment : BaseFragment<Unit>(
    R.layout.fragment_main,
    R.string.home
), SwipeRefreshLayout.OnRefreshListener, CardInteractionListener {

    private var isConnectivityChangeReceiverRegistered = false

    private val connectivityManager: ConnectivityManager by lazy {
        requireContext().connectivityManager
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            runOnUiThread { this@MainFragment.refreshCards() }
        }
    }

    private val cardsAdapter: CardAdapter by lazy { CardAdapter(this) }

    @Inject
    lateinit var viewModelProvider: Provider<MainActivityViewModel>

    private val viewModel: MainActivityViewModel by lazy {
        val factory = ViewModelFactory(viewModelProvider)
        ViewModelProviders.of(this, factory).get(MainActivityViewModel::class.java)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout?.setOnRefreshListener(this)
        swipeRefreshLayout?.isRefreshing = true
        swipeRefreshLayout?.setColorSchemeResources(
                R.color.color_primary,
                R.color.tum_A100,
                R.color.tum_A200)

        registerForContextMenu(cardsRecyclerView)

        cardsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cardsRecyclerView.adapter = cardsAdapter

        // Add equal spacing between CardViews in the RecyclerView
        val spacing = Math.round(resources.getDimension(R.dimen.material_card_view_padding))
        cardsRecyclerView.addItemDecoration(EqualSpacingItemDecoration(spacing))

        // Swipe gestures
        ItemTouchHelper(ItemTouchHelperCallback()).attachToRecyclerView(cardsRecyclerView)

        // Start silence Service (if already started it will just invoke a check)
        val service = Intent(requireContext(), SilenceService::class.java)
        requireContext().startService(service)

        viewModel.cards.observe(viewLifecycleOwner) {
            it?.let { onNewCardsAvailable(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Utils.getSettingBool(requireContext(), Const.REFRESH_CARDS, false)) {
            refreshCards()
            Utils.setSetting(requireContext(), Const.REFRESH_CARDS, false)
        }
    }

    internal fun refreshCards() {
        swipeRefreshLayout?.isRefreshing = true
        onRefresh()
        downloadNewsAlert()
    }

    private fun onNewCardsAvailable(cards: List<Card>) {
        swipeRefreshLayout?.isRefreshing = false
        cardsAdapter.updateItems(cards)

        if (!NetUtils.isConnected(requireContext()) && !isConnectivityChangeReceiverRegistered) {
            val request = NetworkRequest.Builder()
                    .addCapability(NetUtils.internetCapability)
                    .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
            isConnectivityChangeReceiverRegistered = true
        }
    }

    private fun downloadNewsAlert() {
        WorkManager.getInstance().enqueue(DownloadWorker.getWorkRequest())
    }

    override fun onRefresh() {
        viewModel.refreshCards()
    }

    override fun onAlwaysHideCard(position: Int) {
        cardsAdapter.remove(position) // TODO
    }

    override fun onDestroy() {
        if (isConnectivityChangeReceiverRegistered) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            isConnectivityChangeReceiverRegistered = false
        }
        super.onDestroy()
    }

    companion object {
        @JvmStatic fun newInstance() = MainFragment()
    }

    private inner class ItemTouchHelperCallback : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun getSwipeDirs(recyclerView: RecyclerView,
                                  viewHolder: RecyclerView.ViewHolder): Int {
            val cardViewHolder = viewHolder as CardViewHolder
            val card = cardViewHolder.currentCard
            return if (card == null || !card.isDismissible) 0 else super.getSwipeDirs(recyclerView, viewHolder)
        }

        override fun onMove(recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder): Boolean {
            cardsAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun isLongPressDragEnabled(): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val cardViewHolder = viewHolder as CardViewHolder
            val card = cardViewHolder.currentCard
            val lastPos = cardViewHolder.adapterPosition
            cardsAdapter.remove(lastPos)

            Snackbar.make(cardsRecyclerView, R.string.card_dismissed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) {
                        card?.let {
                            cardsAdapter.insert(lastPos, it)
                        }

                        val layoutManager = cardsRecyclerView.layoutManager
                        layoutManager?.smoothScrollToPosition(cardsRecyclerView, null, lastPos)
                    }
                    .setActionTextColor(Color.WHITE)
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar?, event: Int) {
                            super.onDismissed(snackbar, event)
                            if (event != DISMISS_EVENT_ACTION) {
                                // DISMISS_EVENT_ACTION means, the snackbar was dismissed via the undo button
                                // and therefore, we didn't really dismiss the card
                                card?.discard()
                            }
                        }
                    })
                    .show()
        }
    }

}
