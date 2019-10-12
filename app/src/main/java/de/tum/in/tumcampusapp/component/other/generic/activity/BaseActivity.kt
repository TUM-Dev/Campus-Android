package de.tum.`in`.tumcampusapp.component.other.generic.activity

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import de.tum.`in`.tumcampusapp.di.AppComponent
import de.tum.`in`.tumcampusapp.di.app
import kotlinx.android.synthetic.main.toolbar.toolbar
import java.util.Locale

abstract class BaseActivity(
    @LayoutRes private val layoutId: Int
) : AppCompatActivity() {

    val injector: AppComponent by lazy { app.appComponent }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        // TODO Refactor
        if (this !is BaseNavigationActivity) {
            setSupportActionBar(toolbar)

            supportActionBar?.let {
                val parent = NavUtils.getParentActivityName(this)
                if (parent != null) {
                    it.setDisplayHomeAsUpEnabled(true)
                    it.setHomeButtonEnabled(true)
                }
            }
        }

        initLanguage(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    // load language from user's preferences
    private fun initLanguage(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val lang = sharedPreferences.getString("language_preference", Locale.getDefault().language)
        val locale = Locale(lang)

        Locale.setDefault(locale)
        val config = baseContext.resources.configuration
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}
