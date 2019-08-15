package de.tum.`in`.tumcampusapp.component.ui.tufilm

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino

class KinoAdapter internal constructor(
        fragmentManager: FragmentManager, private val movies: List<Kino>
) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment = KinoDetailsFragment.newInstance(position)

    override fun getCount() = movies.size

}