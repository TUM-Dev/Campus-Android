package de.tum.`in`.tumcampusapp.component.ui.cafeteria.rating

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForAccessingTumCabe
import de.tum.`in`.tumcampusapp.databinding.FragmentShowCafeteriaRatingsBinding


class ShowCafeteriaRatingsFragment : FragmentForAccessingTumCabe<List<String>>(
    R.layout.fragment_show_cafeteria_ratings,
    R.string.view_cafeteria_rating
) {


    private val binding by viewBinding(FragmentShowCafeteriaRatingsBinding::bind)


    private val itemsList = ArrayList<ShowRatingAverage>()
    private lateinit var showTagRatingAdapter: ShowRatingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cafeterias = arrayOf("Mensa Garching", "Mensa Leopoldstrasse")
        val meals = arrayOf("Only The Cafeteria", "Pizza Margeritha")



        binding.pickCafeteriaShowSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item, cafeterias
        )


        binding.pickDishShowSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item, meals
        )

        prepareItems()


        showTagRatingAdapter = ShowRatingAdapter(itemsList)
        val layoutManager = LinearLayoutManager(requireContext())
        binding.showSingleRatingsRecyclerView.layoutManager = layoutManager
        binding.showSingleRatingsRecyclerView.adapter = showTagRatingAdapter
    }


    private fun prepareItems() {
        val nameTagResults = listOf<ShowTagRatingAverage>(
            ShowTagRatingAverage("bad", 1.0, 1, 5, 1.0),
            ShowTagRatingAverage("Variety Vegetarian", 2.0, 1, 5, 1.0),
            ShowTagRatingAverage("Variety Vegan", 3.0, 1, 5, 1.0),
            ShowTagRatingAverage("something", 4.0, 1, 5, 1.0),
            ShowTagRatingAverage("clean", 5.0, 1, 5, 1.0)
        )
        itemsList.add(
            ShowRatingAverage(
                false,
                1,
                "Important comment on this beautiful rating. Sometimes, a comment must be longer than one line",
                nameTagResults,
                nameTagResults
            )
        )
        itemsList.add(
            ShowRatingAverage(
                false,
                2,
                "",
                nameTagResults,
                nameTagResults
            )
        )
        itemsList.add(
            ShowRatingAverage(
                false,
                3,
                "",
                nameTagResults,
                nameTagResults
            )
        )
        itemsList.add(
            ShowRatingAverage(
                false,
                4,
                "Important comment on this beautiful rating",
                nameTagResults,
                nameTagResults
            )
        )
        itemsList.add(
            ShowRatingAverage(
                false,
                5,
                "Important comment on this beautiful rating",
                nameTagResults,
                nameTagResults
            )
        )
        itemsList.add(
            ShowRatingAverage(
                true,
                3,
                "Important comment on this beautiful rating",
                nameTagResults,
                nameTagResults
            )
        )

    }


    companion object {
        @JvmStatic
        fun newInstance() = CreateCafeteriaRatingFragment()
    }

}