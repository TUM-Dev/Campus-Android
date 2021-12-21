package de.tum.`in`.tumcampusapp.component.ui.barrierfree

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.BaseFragment
import de.tum.`in`.tumcampusapp.databinding.FragmentBarrierfreeInfoBinding

class BarrierFreeInfoFragment : BaseFragment<Unit>(
    R.layout.fragment_barrierfree_info,
    R.string.barrier_free
) {

    private val binding by viewBinding(FragmentBarrierfreeInfoBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.barrierFreeListView.setOnItemClickListener { _, _, position, _ ->
            val intent = when (position) {
                0 -> Intent(requireContext(), BarrierFreeContactActivity::class.java)
                1 -> Intent(requireContext(), BarrierFreeFacilitiesActivity::class.java)
                2 -> Intent(requireContext(), BarrierFreeMoreInfoActivity::class.java)
                else -> throw IllegalStateException("Invalid index $position in BarrierFreeInfoFragment")
            }
            startActivity(intent)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = BarrierFreeInfoFragment()
    }
}
