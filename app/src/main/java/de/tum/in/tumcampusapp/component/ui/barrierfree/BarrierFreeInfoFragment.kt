package de.tum.`in`.tumcampusapp.component.ui.barrierfree

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.BaseFragment
import de.tum.`in`.tumcampusapp.component.ui.search.SearchActivity
import de.tum.`in`.tumcampusapp.databinding.FragmentBarrierfreeInfoBinding

class BarrierFreeInfoFragment : BaseFragment<Unit>(
    R.layout.fragment_barrierfree_info,
    R.string.barrier_free
) {

    private val binding by viewBinding(FragmentBarrierfreeInfoBinding::bind)

    override val layoutAllErrorsBinding get() = binding.layoutAllErrors

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.barrierFreeListView.setOnItemClickListener { _, _, position, _ ->
            val intent = when (position) {
                0 -> Intent(requireContext(), BarrierFreeContactActivity::class.java)
                in 1..2 -> {
                    val filter = when (position) {
                        1 -> "usage:wc-barrierefrei"
                        2 -> "usage:aufzugsanlage"
                        else -> throw IllegalStateException("Invalid index $position in BarrierFreeInfoFragment")
                    }
                    val tmp_int = Intent(requireContext(), SearchActivity::class.java)
                    val lat = 0.0
                    val lon = 0.0
                    tmp_int.putExtra(SearchManager.QUERY, "$filter near:$lat,$lon")
                    tmp_int
                }
                3 -> Intent(requireContext(), BarrierFreeMoreInfoActivity::class.java)
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
