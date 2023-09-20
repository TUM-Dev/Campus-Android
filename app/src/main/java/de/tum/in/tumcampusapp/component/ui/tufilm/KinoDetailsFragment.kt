package de.tum.`in`.tumcampusapp.component.ui.tufilm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.utils.Const
import javax.inject.Provider

/**
 * Fragment for KinoDetails. Manages content that gets shown on the pagerView
 */
class KinoDetailsFragment : Fragment() {

    internal lateinit var viewModelProvider: Provider<KinoDetailsViewModel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_kinodetails_section, container, false)

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
