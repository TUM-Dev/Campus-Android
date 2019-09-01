package de.tum.`in`.tumcampusapp.component.ui.ticket.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventCard
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventDiffUtil
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.ShowTicketActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventBetaInfo
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventItem
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsLocalRepository
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import java.util.*
import java.util.regex.Pattern

class EventsAdapter(private val mContext: Context) : RecyclerView.Adapter<CardViewHolder>() {

    private val ticketsLocalRepo: TicketsLocalRepository = TicketsLocalRepository(TcaDb.getInstance(mContext))

    private var events: List<EventItem> = ArrayList()
    private val betaInfo = EventBetaInfo()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        if (viewType == CARD_INFO) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_events_info, parent, false)
            return CardViewHolder(view)
        }

        val layoutRes = if (viewType == CARD_HORIZONTAL) {
            R.layout.card_events_item
        } else {
            R.layout.card_events_item_vertical
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return EventViewHolder(view, null, false)
    }

    override fun getItemViewType(position: Int): Int {
        val item = events[position]
        if (item is EventBetaInfo) {
            return CARD_INFO
        }
        return if ((item as Event).kino == -1) {
            CARD_HORIZONTAL
        } else CARD_VERTICAL
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val eventItem = events[position]
        if (eventItem is EventBetaInfo) {
            return
        }
        val event = eventItem as Event
        val eventCard = EventCard(mContext)
        eventCard.event = event
        holder.currentCard = eventCard

        val ticketCount = ticketsLocalRepo.getTicketCount(event)
        (holder as EventViewHolder).bind(event, ticketCount)
    }

    override fun getItemCount() = events.size

    fun update(newEvents: MutableList<EventItem>) {
        if (newEvents.isEmpty() || newEvents[0] !is EventBetaInfo) {
            newEvents.add(0, betaInfo)
        }
        val callback = EventDiffUtil(events, newEvents)
        val diffResult = DiffUtil.calculateDiff(callback)
        events = newEvents
        diffResult.dispatchUpdatesTo(this)
    }

    class EventViewHolder(
        view: View,
        interactionListener: CardInteractionListener?,
        private val showOptionsButton: Boolean
    ) : CardViewHolder(view, interactionListener) {

        private var optionsButtonGroup: Group = view.findViewById(R.id.cardMoreIconGroup)
        private var progressBar: ProgressBar = view.findViewById(R.id.poster_progress_bar)
        private var imageView: ImageView = view.findViewById(R.id.events_img)
        private var titleTextView: TextView = view.findViewById(R.id.events_title)
        private var startDateTextView: TextView = view.findViewById(R.id.events_src_date)
        private var ticketButton: MaterialButton = view.findViewById(R.id.ticketButton)

        fun bind(event: Event, ticketCount: Int) {

            optionsButtonGroup.isVisible = showOptionsButton

            val imageUrl = event.imageUrl
            val showImage = imageUrl != null && imageUrl.isNotEmpty()
            if (showImage) {
                Picasso.get()
                        .load(imageUrl)
                        .into(imageView, object : Callback {
                            override fun onSuccess() {
                                progressBar.visibility = GONE
                            }

                            override fun onError(e: Exception) {
                                Utils.log(e)
                                progressBar.visibility = GONE
                            }
                        })
            } else {
                progressBar.visibility = GONE
                imageView.visibility = GONE
            }

            titleTextView.text = TITLE_DATE.matcher(event.title).replaceAll("")
            startDateTextView.text = event.getFormattedStartDateTime(itemView.context)

            ticketButton.isVisible = ticketCount != 0
            if (ticketCount == 0) {
                return
            }
            ticketButton.text = itemView.context.resources
                    .getQuantityString(R.plurals.tickets, ticketCount, ticketCount)
            ticketButton.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ShowTicketActivity::class.java)
                intent.putExtra(Const.KEY_EVENT_ID, event.id)
                context.startActivity(intent)
            }
        }
    }

    companion object {
        private val TITLE_DATE = Pattern.compile("^[0-9]+\\. [0-9]+\\. [0-9]+:[ ]*")

        private const val CARD_INFO = 0
        private const val CARD_HORIZONTAL = 1
        private const val CARD_VERTICAL = 2
    }
}