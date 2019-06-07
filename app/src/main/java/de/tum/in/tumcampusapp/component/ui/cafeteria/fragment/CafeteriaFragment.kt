package de.tum.`in`.tumcampusapp.component.ui.cafeteria.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForDownloadingExternal
import de.tum.`in`.tumcampusapp.component.other.locations.LocationManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaMenuFormatter
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaNotificationSettingsActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaDetailsSectionsPagerAdapter
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaViewModel
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.di.CafeteriaModule
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.di.ViewModelFactory
import de.tum.`in`.tumcampusapp.di.injector
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.observeNonNull
import kotlinx.android.synthetic.main.fragment_cafeteria.pager
import kotlinx.android.synthetic.main.fragment_cafeteria.spinnerToolbar
import org.joda.time.DateTime
import javax.inject.Inject
import javax.inject.Provider

class CafeteriaFragment : FragmentForDownloadingExternal(
    R.layout.fragment_cafeteria,
    R.string.cafeteria
), AdapterView.OnItemSelectedListener {

    @Inject
    lateinit var viewModelProvider: Provider<CafeteriaViewModel>

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var cafeteriaManager: CafeteriaManager

    @Inject
    lateinit var cafeteriaDownloadAction: DownloadWorker.Action

    private var cafeterias = mutableListOf<Cafeteria>()

    private val cafeteriaViewModel: CafeteriaViewModel by lazy {
        val factory = ViewModelFactory(viewModelProvider)
        ViewModelProviders.of(this, factory).get(CafeteriaViewModel::class.java)
    }

    private val adapter: ArrayAdapter<Cafeteria> by lazy { createArrayAdapter() }
    private val sectionsPagerAdapter: CafeteriaDetailsSectionsPagerAdapter by lazy {
        CafeteriaDetailsSectionsPagerAdapter(childFragmentManager)
    }

    override val method: DownloadWorker.Action?
        get() = cafeteriaDownloadAction

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        injector
            .cafeteriaComponent()
            .cafeteriaModule(CafeteriaModule())
            .build()
            .inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pager.offscreenPageLimit = 50

        spinnerToolbar.adapter = adapter
        spinnerToolbar.onItemSelectedListener = this

        cafeteriaViewModel.cafeterias.observeNonNull(this) { updateCafeterias(it) }
        cafeteriaViewModel.selectedCafeteria.observeNonNull(this) { onNewCafeteriaSelected(it) }
        cafeteriaViewModel.menuDates.observeNonNull(this) { updateSectionsPagerAdapter(it) }

        cafeteriaViewModel.error.observeNonNull(this) { isError ->
            if (isError) {
                showError(R.string.error_something_wrong)
            } else {
                showContentLayout()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val location = locationManager.getCurrentOrNextLocation()
        cafeteriaViewModel.fetchCafeterias(location)
    }

    private fun updateCafeterias(newCafeterias: List<Cafeteria>) {
        cafeterias.clear()
        cafeterias.addAll(newCafeterias)

        adapter.clear()
        adapter.addAll(newCafeterias)
        adapter.notifyDataSetChanged()
        initCafeteriaSpinner()
    }

    private fun onNewCafeteriaSelected(cafeteria: Cafeteria) {
        sectionsPagerAdapter.setCafeteriaId(cafeteria.id)
        cafeteriaViewModel.fetchMenuDates()
    }

    private fun initCafeteriaSpinner() {
        val intent = requireActivity().intent
        val cafeteriaId: Int

        if (intent != null && intent.hasExtra(Const.MENSA_FOR_FAVORITEDISH)) {
            cafeteriaId = intent.getIntExtra(Const.MENSA_FOR_FAVORITEDISH, NONE_SELECTED)
            intent.removeExtra(Const.MENSA_FOR_FAVORITEDISH)
        } else if (intent != null && intent.hasExtra(Const.CAFETERIA_ID)) {
            cafeteriaId = intent.getIntExtra(Const.CAFETERIA_ID, 0)
        } else {
            // If we're not provided with a cafeteria ID, we choose the best matching cafeteria.
            cafeteriaId = cafeteriaManager.bestMatchMensaId
        }

        updateCafeteriaSpinner(cafeteriaId)
    }

    private fun updateCafeteriaSpinner(cafeteriaId: Int) {
        var selectedIndex = NONE_SELECTED

        for (cafeteria in cafeterias) {
            val index = cafeterias.indexOf(cafeteria)
            if (cafeteriaId == NONE_SELECTED || cafeteriaId == cafeteria.id) {
                selectedIndex = index
                break
            }
        }

        if (selectedIndex != NONE_SELECTED) {
            spinnerToolbar.setSelection(selectedIndex)
        }
    }

    private fun updateSectionsPagerAdapter(menuDates: List<DateTime>) {
        pager.adapter = null
        sectionsPagerAdapter.update(menuDates)
        pager.adapter = sectionsPagerAdapter
    }

    private fun createArrayAdapter(): ArrayAdapter<Cafeteria> {
        return object : ArrayAdapter<Cafeteria>(
            requireContext(), R.layout.simple_spinner_item_actionbar) {
            private val inflater = LayoutInflater.from(context)

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = inflater.inflate(
                    R.layout.simple_spinner_dropdown_item_actionbar_two_line, parent, false)
                val cafeteria = getItem(position)

                val name = v.findViewById<TextView>(android.R.id.text1)
                val address = v.findViewById<TextView>(android.R.id.text2)
                val distance = v.findViewById<TextView>(R.id.distance)

                if (cafeteria != null) {
                    name.text = cafeteria.name
                    address.text = cafeteria.address
                    distance.text = Utils.formatDistance(cafeteria.distance)
                }

                return v
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val selected = cafeterias.get(position)
        cafeteriaViewModel.updateSelectedCafeteria(selected)
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) = Unit

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_section_fragment_cafeteria_details, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_ingredients -> {
                showIngredientsInfo()
                true
            }
            R.id.action_settings -> {
                openNotificationSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openNotificationSettings() {
        val intent = Intent(requireContext(), CafeteriaNotificationSettingsActivity::class.java)
        startActivity(intent)
    }

    private fun showIngredientsInfo() {
        // Build a alert dialog containing the mapping of ingredients to the numbers
        val formatter = CafeteriaMenuFormatter(requireContext())
        val message = formatter.format(R.string.cafeteria_ingredients, true)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.action_ingredients)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
            }
            .show()
    }

    companion object {

        private const val NONE_SELECTED = -1

        @JvmStatic
        fun newInstance() = CafeteriaFragment()

    }

}
