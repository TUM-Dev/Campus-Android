package de.tum.`in`.tumcampusapp.component.ui.tufilm

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ProgressActivity
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import de.tum.`in`.tumcampusapp.databinding.ActivityKinoBinding
import de.tum.`in`.tumcampusapp.di.ViewModelFactory
import de.tum.`in`.tumcampusapp.utils.Const
import javax.inject.Inject
import javax.inject.Provider

/**
 * Activity to show TU film movies
 */
class KinoActivity : ProgressActivity<Void>(R.layout.activity_kino) {

    private var startPosition: Int = 0

    @Inject
    internal lateinit var viewModelProvider: Provider<KinoViewModel>
    private lateinit var viewPager: ViewPager;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.kinoComponent().inject(this)

        window.setBackgroundDrawableResource(R.color.secondary_window_background)

        val factory = ViewModelFactory(viewModelProvider)
        val kinoViewModel = ViewModelProviders.of(this, factory).get(KinoViewModel::class.java)

        val movieDate = intent.getStringExtra(Const.KINO_DATE)
        val movieId = intent.getIntExtra(Const.KINO_ID, -1)

        startPosition = when {
            movieDate != null -> kinoViewModel.getPositionByDate(movieDate)
            movieId != -1 -> kinoViewModel.getPositionById("" + movieId)
            else -> 0
        }

        val margin = resources.getDimensionPixelSize(R.dimen.material_default_padding)
        viewPager = findViewById(R.id.kinoViewPager)

        viewPager.pageMargin = margin

        kinoViewModel.kinos.observe(this, Observer<List<Kino>> { this.showMoviesOrPlaceholder(it) })
        kinoViewModel.error.observe(this, Observer<Int> { this.showError(it) })
    }

    private fun showMoviesOrPlaceholder(kinos: List<Kino>) {
        if (kinos.isEmpty()) {
            showEmptyResponseLayout(R.string.no_movies, R.drawable.no_movies)
            return
        }

        // Disable clip to padding
        viewPager.clipToPadding = false;
        viewPager.setPadding(60, 0, 60, 0);
        viewPager.pageMargin = 20;
        viewPager.adapter = KinoAdapter(supportFragmentManager, kinos)
        viewPager.currentItem = startPosition
    }
}
