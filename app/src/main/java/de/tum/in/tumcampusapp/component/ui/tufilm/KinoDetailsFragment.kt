package de.tum.`in`.tumcampusapp.component.ui.tufilm

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventHelper
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.ShowTicketActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.payload.TicketStatus
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import de.tum.`in`.tumcampusapp.databinding.FragmentKinodetailsSectionBinding
import de.tum.`in`.tumcampusapp.di.ViewModelFactory
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Const.KEY_EVENT_ID
import javax.inject.Inject
import javax.inject.Provider

/**
 * Fragment for KinoDetails. Manages content that gets shown on the pagerView
 */
class KinoDetailsFragment : Fragment() {

    private var event: Event? = null

    @Inject
    internal lateinit var viewModelProvider: Provider<KinoDetailsViewModel>

    @Inject
    internal lateinit var ticketsLocalRepo: TicketsLocalRepository

    private lateinit var kinoViewModel: KinoDetailsViewModel

    private val binding by viewBinding(FragmentKinodetailsSectionBinding::bind)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as BaseActivity).injector
                .kinoComponent()
                .inject(this)

        val factory = ViewModelFactory(viewModelProvider)
        kinoViewModel = ViewModelProvider(this, factory).get(KinoDetailsViewModel::class.java)

        kinoViewModel.kino.observe(this, Observer<Kino> { showMovieDetails(it) })
        kinoViewModel.event.observe(this, Observer<Event> { showEventTicketDetails(it) })
        kinoViewModel.aggregatedTicketStatus.observe(this, Observer { showTicketCount(it) })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
            inflater.inflate(R.layout.fragment_kinodetails_section, container, false)

    override fun onResume() {
        super.onResume()
        event?.let {
            initBuyOrShowTicket(it)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val position = arguments?.getInt(Const.POSITION) ?: 0
        kinoViewModel.fetchKinoByPosition(position)
    }

    private fun showEventTicketDetails(event: Event) {
        this.event = event
        initBuyOrShowTicket(event)

        with(binding) {
            eventInformation.visibility = View.VISIBLE
            locationTextView.text = event.locality
        }


        kinoViewModel.fetchTicketCount(event.id)
    }

    private fun initBuyOrShowTicket(event: Event) {
        val ticketBoughtCount = ticketsLocalRepo.getTicketCount(event)

        if (ticketBoughtCount > 0) {
            binding.buyTicketButton.text = resources.getQuantityString(R.plurals.show_tickets, ticketBoughtCount)
            binding.buyTicketButton.setOnClickListener {
                val intent = Intent(context, ShowTicketActivity::class.java).apply {
                    putExtra(KEY_EVENT_ID, event.id)
                }
                startActivity(intent)
            }
        } else if (!EventHelper.isEventImminent(event)) {
            binding.buyTicketButton.setText(R.string.buy_ticket)
            binding.buyTicketButton.setOnClickListener {
                this.event?.let {
                    EventHelper.buyTicket(it, binding.buyTicketButton, context)
                }
            }
        }
    }

    private fun showTicketCount(status: TicketStatus?) {
        val event = event
        val isEventBooked = event != null && ticketsLocalRepo.getTicketCount(event) > 0
        val isEventImminent = event != null && EventHelper.isEventImminent(event)

        with(binding) {
            EventHelper.showRemainingTickets(
                    status,
                    isEventBooked,
                    isEventImminent,
                    buyTicketButton,
                    remainingTicketsContainer,
                    remainingTicketsTextView,
                    getString(R.string.no_tickets_remaining_tufilm_message))
        }

    }

    private fun showMovieDetails(kino: Kino) {
        kinoViewModel.fetchEventByMovieId(kino.id)

        loadPoster(kino)

        with(binding) {
            kinoMovieTitle.text = kino.title.split(":".toRegex(), 2).toTypedArray()[1]
            dateTextView.text = kino.formattedShortDate
            runtimeTextView.text = kino.runtime
            ratingTextView.text = kino.formattedRating

            val colorPrimary = ContextCompat.getColor(requireContext(), R.color.color_primary)
            setCompoundDrawablesTint(dateTextView, colorPrimary)
            setCompoundDrawablesTint(runtimeTextView, colorPrimary)
            setCompoundDrawablesTint(ratingTextView, colorPrimary)

            descriptionTextView.text = kino.formattedDescription
            genresTextView.text = kino.genre
            releaseYearTextView.text = kino.year
            actorsTextView.text = kino.actors
            directorTextView.text = kino.director

            moreInfoButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(kino.link))
                startActivity(intent)
            }
        }

    }

    private fun loadPoster(kino: Kino) {
        binding.trailerButton.setOnClickListener { showTrailer(kino) }

        Picasso.get()
                .load(kino.cover)
                .into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                        binding.kinoCoverPlaceholder.visibility = View.GONE
                        binding.kinoCover.setImageBitmap(bitmap)
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        binding.kinoCoverProgress.visibility = View.GONE
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        // intentionally left blank
                    }
                })
    }

    private fun setCompoundDrawablesTint(textView: TextView, color: Int) {
        for (drawable in textView.compoundDrawables) {
            drawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

    private fun showTrailer(kino: Kino) {
        val url = kino.trailerSearchUrl
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        requireActivity().startActivity(intent)
    }

    companion object {

        fun newInstance(position: Int): KinoDetailsFragment {
            val fragment = KinoDetailsFragment()
            fragment.arguments = Bundle().apply {
                putInt(Const.POSITION, position)
            }
            return fragment
        }
    }
}
