package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.navigatum.domain.NavigationEntity
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.BaseFragment
import de.tum.`in`.tumcampusapp.di.ViewModelFactory
import de.tum.`in`.tumcampusapp.di.injector
import kotlinx.android.synthetic.main.fragment_navigation_details.*
import kotlinx.android.synthetic.main.fragment_navigation_details.progressIndicator
import kotlinx.android.synthetic.main.toolbar_search.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class NavigationDetailsFragment : BaseFragment<Unit>(
    layoutId = R.layout.fragment_navigation_details,
    titleResId = R.string.roomfinder
) {

    private lateinit var navigationEntity: NavigationEntity

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            navigationEntity = it.getSerializable(NAVIGATION_ENTITY) as NavigationEntity
        } ?: run {
            // show error
            println("error to show")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.title = navigationEntity.getFormattedName()
        toolbar.subtitle = navigationEntity.getFormattedSubtext()

        lifecycleScope.launch {
            handleDetailsLoading()
        }

        viewModel.loadNavigationDetails(navigationEntity.id)
    }

    private suspend fun handleDetailsLoading() {
        viewModel.state.collect { state ->
            if (state.isLoading)
                progressIndicator.show()
            else
                progressIndicator.hide()

            if (state.navigationDetails != null) {
                val map = state.navigationDetails.map
                if (map != null) {
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
            }
        }
    }

    companion object {
        const val NAVIGATION_ENTITY = "navigationEntity"
    }
}
