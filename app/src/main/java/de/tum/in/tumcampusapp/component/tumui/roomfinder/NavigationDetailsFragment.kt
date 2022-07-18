package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.navigatum.domain.NavigationDetails
import de.tum.`in`.tumcampusapp.api.navigatum.domain.NavigationEntity
import de.tum.`in`.tumcampusapp.api.navigatum.domain.NavigationMap
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.BaseFragment
import de.tum.`in`.tumcampusapp.di.ViewModelFactory
import de.tum.`in`.tumcampusapp.di.injector
import kotlinx.android.synthetic.main.fragment_navigation_details.*
import kotlinx.android.synthetic.main.navigation_property_row.view.*
import kotlinx.android.synthetic.main.toolbar_search.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jetbrains.anko.sdk27.coroutines.onItemSelectedListener
import javax.inject.Inject
import javax.inject.Provider

class NavigationDetailsFragment : BaseFragment<Unit>(
    layoutId = R.layout.fragment_navigation_details,
    titleResId = R.string.roomfinder
) {

    private val navigationEntity: NavigationEntity? by lazy {
        arguments?.getSerializable(NAVIGATION_ENTITY) as NavigationEntity
    }

    @Inject
    lateinit var viewModelProvider: Provider<NavigationDetailsViewModel>

    private val viewModel: NavigationDetailsViewModel by lazy {
        val factory = ViewModelFactory(viewModelProvider)
        ViewModelProvider(this, factory).get(NavigationDetailsViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.navigationDetailsComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setTitle(R.string.location_details)

        lifecycleScope.launch {
            handleDetailsLoading()
        }

        navigationEntity?.let {
            viewModel.loadNavigationDetails(navigationEntity!!.id)
        } ?: run {
            showLoadingError()
        }
    }

    private suspend fun handleDetailsLoading() {
        viewModel.state.collect { state ->

            if (state.isLoading)
                progressIndicator.show()
            else
                progressIndicator.hide()

            if (state.navigationDetails != null) {
                showLocationDetails(state.navigationDetails)
            }
        }
    }

    private fun showLocationDetails(navigationDetails: NavigationDetails) {
        parentLocations.text = navigationDetails.getFormattedParentNames()
        locationName.text = navigationDetails.name
        locationType.text = getCapitalizeType(navigationDetails.type)

        setOpenInOtherAppBtnListener(navigationDetails)

        showNavigationDetailsProperties(navigationDetails)

        showAvailableMaps(navigationDetails)
    }

    private fun setOpenInOtherAppBtnListener(navigationDetails: NavigationDetails) {
        openLocationBtn.setOnClickListener {
            val coordinates = "${navigationDetails.cordsLat},${navigationDetails.cordsLon}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$coordinates"))
            startActivity(intent)
        }
    }

    private fun showNavigationDetailsProperties(navigationDetails: NavigationDetails) {
        navigationDetails.properties.forEach { property ->
            val propertyRow = layoutInflater.inflate(R.layout.navigation_property_row, propsList, false)
            propertyRow.propertyName.text = getTranslationForPropertyTitle(property.title)
            propertyRow.propertyValue.text = property.value
            propsList.addView(propertyRow)
        }
    }

    private fun showAvailableMaps(navigationDetails: NavigationDetails) {
        val availableMaps = navigationDetails.availableMaps
        if (availableMaps.isNotEmpty()) {
            val map = availableMaps[0]
            loadMapImage(map)

            val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            availableMapSpinner.adapter = adapter
            availableMaps.forEach {
                adapter.add(it.mapName)
            }
            availableMapSpinner.onItemSelectedListener {
                this.onItemSelected { adapterView, _, position, _ ->
                    val selectedMapName = adapterView?.getItemAtPosition(position).toString()
                    val selectedMap = availableMaps.find { it.mapName == selectedMapName }
                    selectedMap?.let {
                        loadMapImage(it)
                    }
                }
            }
            adapter.notifyDataSetChanged()
        } else {
            Picasso.get()
                .load(R.drawable.site_plans_not_available)
                .into(photoView)
        }

        interactiveMapBtn.setOnClickListener {
            showInteractiveMapDialog()
        }
    }

    private fun loadMapImage(map: NavigationMap) {
        val pinDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_place, null)
            ?: throw IllegalStateException("Pin icon not loaded")
        val pointerDrawer = DrawCordsPointerTransformation(
            cordX = map.pointerXCord,
            cordY = map.pointerYCord,
            pinDrawable = pinDrawable
        )
        Picasso.get()
            .load(map.getFullMapImgUrl())
            .transform(pointerDrawer)
            .into(photoView)
    }

    private fun getTranslationForPropertyTitle(title: String): String {
        return when (title) {
            "Raumkennung" -> getString(R.string.room_id)
            "Adresse" -> getString(R.string.address)
            "Architekten-Name" -> getString(R.string.architect_name)
            "Sitzplätze" -> getString(R.string.seats)
            "Anzahl Räume" -> getString(R.string.number_of_rooms)
            else -> title
        }
    }

    private fun getCapitalizeType(type: String): String {
        return type.replaceFirstChar { it.titlecase() }
    }

    private fun showInteractiveMapDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.not_implemented)
            .setMessage(R.string.not_implemented_interactive_map)
            .setPositiveButton(R.string.redirect) { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nav.tum.sexy/room/${viewModel.state.value.navigationDetails?.id}"))
                startActivity(intent)
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
        dialog.changeThemeColor()
    }

    private fun showLoadingError() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.error_something_wrong)
            .create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
    }

    companion object {
        const val NAVIGATION_ENTITY = "navigationEntity"

        fun newInstance(navigationEntity: NavigationEntity) = NavigationDetailsFragment().apply {
            arguments = bundleOf(NAVIGATION_ENTITY to navigationEntity)
        }
    }
}

fun AlertDialog.changeThemeColor() {
    this.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.text_primary))
    this.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
}
