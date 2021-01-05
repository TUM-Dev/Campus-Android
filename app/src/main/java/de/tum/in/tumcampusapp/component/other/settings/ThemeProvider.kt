package de.tum.`in`.tumcampusapp.component.other.settings

import android.app.UiModeManager
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import de.tum.`in`.tumcampusapp.utils.Const
import java.security.InvalidParameterException

class ThemeProvider(private val context: Context) {

    fun getThemeFromPreferences(): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val selectedTheme = sharedPreferences.getString(Const.DESIGN_THEME, "system")

        return selectedTheme?.let {
            getTheme(it)
        } ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    fun getTheme(selectedTheme: String): Int = when (selectedTheme) {
        "system" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        "light" -> UiModeManager.MODE_NIGHT_NO
        "dark" -> UiModeManager.MODE_NIGHT_YES
        else -> throw InvalidParameterException("Theme not defined for $selectedTheme")
    }
}