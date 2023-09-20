package de.tum.`in`.tumcampusapp.component.ui.ticket.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.databinding.FragmentEventDetailsBinding
import de.tum.`in`.tumcampusapp.di.ViewModelFactory
import de.tum.`in`.tumcampusapp.utils.Const
import javax.inject.Inject
import javax.inject.Provider

/**
 * Fragment for displaying information about an [Event]. Manages content that's shown in the
 * PagerAdapter.
 */
class EventDetailsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private val viewModel: EventDetailsViewModel by lazy {
        val factory = ViewModelFactory(viewModelProviders)
        ViewModelProvider(this, factory).get(EventDetailsViewModel::class.java)
    }

    @Inject
    lateinit var viewModelProviders: Provider<EventDetailsViewModel>

    private val binding by viewBinding(FragmentEventDetailsBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LayoutInflater.from(container?.context).inflate(R.layout.fragment_event_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }

    override fun onRefresh() {
        viewModel.fetchTicketCount()
    }

    companion object {

        @JvmStatic
        fun newInstance(event: Event): EventDetailsFragment {
            return EventDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(Const.KEY_EVENT, event)
                }
            }
        }
    }
}
