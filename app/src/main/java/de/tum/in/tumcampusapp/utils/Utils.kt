package de.tum.`in`.tumcampusapp.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.BatteryManager
import android.os.Build
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.pm.PackageInfoCompat
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.widget.Toast
import com.google.common.base.Charsets
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.BuildConfig
import org.jetbrains.anko.defaultSharedPreferences
import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.util.regex.Pattern

/**
 * Class for common helper functions used by a lot of classes.
 */
object Utils {
    private const val LOGGING_REGEX = "[a-zA-Z0-9.]+\\."

    @JvmStatic
    fun migrateSharedPreferences(context: Context) {
        val prefsLegacy = context.getSharedPreferences("internal_prefs", Context.MODE_PRIVATE)
        val entries = prefsLegacy.all

        if (entries.isEmpty()) {
            return
        }

        val prefsEditor = context.defaultSharedPreferences.edit()

        for ((key, value) in entries) {
            when (value) {
                is Boolean -> prefsEditor.putBoolean(key, value)
                is String -> prefsEditor.putString(key, value)
                is Int -> prefsEditor.putInt(key, value)
                is Float -> prefsEditor.putFloat(key, value)
                is Long -> prefsEditor.putLong(key, value)
            }
        }
        prefsEditor.apply()

        // Delete any old settings
        prefsLegacy.edit()
                .clear()
                .apply()
    }

    /**
     * Get a value from the default shared preferences
     *
     * @param c          Context
     * @param key        setting name
     * @param defaultVal default value
     * @return setting value, defaultVal if undefined
     */
    @JvmStatic
    fun getSetting(c: Context, key: String, defaultVal: String): String {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        return sp.getString(key, defaultVal)!!
    }

    /**
     * Get a value from the default shared preferences.
     *
     * @param c          Context
     * @param key        setting name
     * @param defaultVal default value
     * @return setting value, defaultVal if undefined
     */
    @JvmStatic
    fun getSettingLong(c: Context, key: String, defaultVal: Long): Long {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        return try {
            sp.getLong(key, defaultVal)
        } catch (ignore: ClassCastException) {
            sp.getString(key, null)?.toLongOrNull() ?: defaultVal
        }
    }

    /**
     * Get a value from the default shared preferences.
     *
     * @param c          Context
     * @param key        setting name
     * @param defaultVal default value
     * @return setting value, defaultVal if undefined
     */
    @JvmStatic
    fun getSettingFloat(c: Context, key: String, defaultVal: Float): Float {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        return try {
            sp.getFloat(key, defaultVal)
        } catch (ignore: ClassCastException) {
            sp.getString(key, null)?.toFloatOrNull() ?: defaultVal
        }
    }

    /**
     * Get a value from the default shared preferences.
     *
     * @param c          Context
     * @param key        setting name
     * @param defaultVal default value
     * @return setting value, defaultVal if undefined
     */
    @JvmStatic
    fun getSettingInt(c: Context, key: String, defaultVal: Int): Int {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        return try {
            sp.getInt(key, defaultVal)
        } catch (ignore: ClassCastException) {
            sp.getString(key, null)?.toIntOrNull() ?: defaultVal
        }
    }

    /**
     * Get a value from the default shared preferences.
     *
     * @param c         Context
     * @param key       setting name
     * @param classInst e.g. ChatMember.class
     * @return setting value
     */
    @JvmStatic
    fun <T> getSetting(c: Context, key: String, classInst: Class<T>): T? {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        val value = sp.getString(key, null) ?: return null
        return Gson().fromJson(value, classInst)
    }

    /**
     * Return the boolean value of a setting.
     *
     * @param c          Context
     * @param name       setting name
     * @param defaultVal default value
     * @return true if setting was checked, else value
     */
    @JvmStatic
    fun getSettingBool(c: Context, name: String, defaultVal: Boolean): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getBoolean(name, defaultVal)
    }

    /**
     * Logs an exception and additional information
     * Use this anywhere in the app when a fatal error occurred.
     * If you can give a better description of what went wrong
     * use [.log] instead.
     *
     * @param t the source of message and stack trace
     */
    @JvmStatic
    fun log(t: Throwable) {
        try {
            StringWriter().use { sw ->
                t.printStackTrace(PrintWriter(sw))
                val s = Thread.currentThread()
                        .stackTrace[3].className
                        .replace(LOGGING_REGEX.toRegex(), "")
                Log.e(s, "$t\n$sw")
            }
        } catch (ignore: IOException) {
            // there is a time to stop logging errors
        }
    }

    /**
     * Logs an exception and additional information
     * Use this anywhere in the app when a fatal error occurred.
     * If you can't give an exact error description simply use
     * [.log] instead.
     *
     * @param e       Exception (source for message and stack trace)
     * @param message Additional information for exception message
     */
    @JvmStatic
    fun log(e: Throwable, message: String) {
        try {
            StringWriter().use { sw ->
                e.printStackTrace(PrintWriter(sw))
                val s = Thread.currentThread()
                        .stackTrace[3].className
                        .replace(LOGGING_REGEX.toRegex(), "")
                Log.e(s, "$e $message\n$sw")
            }
        } catch (e1: IOException) {
            // there is a time to stop logging errors
        }

    }

    /**
     * Logs a message
     * Use this to log the current app state.
     *
     * @param message Information or Debug message
     */
    @JvmStatic
    fun log(message: String) {
        if (!BuildConfig.DEBUG) {
            return
        }
        val s = Thread.currentThread()
                .stackTrace[3].className
                .replace(LOGGING_REGEX.toRegex(), "")
        Log.d(s, message)
    }

    /**
     * Logs a message
     * Use this to log additional information that is not important in most cases.
     *
     * @param message Information or Debug message
     */
    @JvmStatic
    fun logv(message: String) {
        if (!BuildConfig.DEBUG) {
            return
        }
        val s = Thread.currentThread()
                .stackTrace[3].className
                .replace(LOGGING_REGEX.toRegex(), "")
        Log.v(s, message)
    }

    /**
     * Logs a message with specified tag
     * Use this to log a particular work
     *
     * @param message Information or Debug message
     */
    @JvmStatic
    fun logwithTag(tag: String, message: String) {
        if (!BuildConfig.DEBUG) {
            return
        }
        Log.v(tag, message)
    }

    /**
     * Returns a String[]-List from a CSV input stream
     *
     * @param fin CSV input stream
     * @return String[]-List with Columns matched to array values
     */
    @JvmStatic
    fun readCsv(fin: InputStream): List<Array<String>> {
        return try {
            fin.bufferedReader(Charsets.UTF_8)
                    .lineSequence()
                    .map { splitCsvLine(it) }
                    .toList()
        } catch (e: IOException) {
            log(e)
            emptyList()
        }
    }

    /**
     * Sets the value of a setting
     *
     * @param c   Context
     * @param key setting key
     */
    @JvmStatic
    fun setSetting(c: Context, key: String, value: Boolean) {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        sp.edit()
                .putBoolean(key, value)
                .apply()
    }

    /**
     * Sets the value of a setting
     *
     * @param c     Context
     * @param key   setting key
     * @param value String value
     */
    @JvmStatic
    fun setSetting(c: Context, key: String, value: String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        sp.edit()
                .putString(key, value)
                .apply()
    }

    /**
     * Sets the value of a setting
     *
     * @param c   Context
     * @param key setting key
     */
    @JvmStatic
    fun setSetting(c: Context, key: String, value: Any) {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        sp.edit()
                .putString(key, Gson().toJson(value))
                .apply()
    }

    /**
     * Shows a long [Toast] message.
     *
     * @param context The activity where the toast is shown
     * @param msg     The toast message id
     */
    @JvmStatic
    fun showToast(context: Context, msg: Int) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    /**
     * Shows a long [Toast] message.
     *
     * @param context The activity where the toast is shown
     * @param msg     The toast message
     */
    @JvmStatic
    fun showToast(context: Context, msg: CharSequence) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    /**
     * Splits a line from a CSV file into column values
     *
     *
     * e.g. "aaa;aaa";"bbb";1 gets aaa,aaa;bbb;1;
     *
     * @param str CSV line
     * @return String[] with CSV column values
     */
    private fun splitCsvLine(str: CharSequence): Array<String> {
        val result = StringBuilder()
        var open = false
        for (i in 0 until str.length) {
            val c = str[i]
            if (c == '"') {
                open = !open
                continue
            }
            if (open && c == ';') {
                result.append(',')
            } else {
                result.append(c)
            }
        }
        // fix trailing ";", e.g. ";;;".split().length = 0
        result.append("; ")
        return result.toString()
                .split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    /**
     * Converts a meter based value to a formatted string
     *
     * @param meters Meters to represent
     * @return Formatted meters. e.g. 10m, 12.5km
     */
    @JvmStatic
    fun formatDistance(meters: Float): String {
        return when {
            meters < 1000 -> "${meters.toInt()}m"
            meters < 10000 -> {
                val front = (meters / 1000f).toInt()
                val back = Math.abs(meters / 100f).toInt() % 10
                "$front.${back}km"
            }
            else -> "${(meters / 1000f).toInt()}km"
        }
    }

    /**
     * @return Application's version code from the `PackageManager`.
     */
    @JvmStatic
    fun getAppVersion(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            PackageInfoCompat.getLongVersionCode(packageInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            // should never happen
            0
        }
    }

    @JvmStatic
    fun showToastOnUIThread(activity: Activity, s: Int) {
        activity.runOnUiThread { Utils.showToast(activity, s) }
    }

    @JvmStatic
    fun showToastOnUIThread(activity: Activity, s: CharSequence) {
        activity.runOnUiThread { Utils.showToast(activity, s) }
    }

    /**
     * Removes all html tags from a string
     *
     * @param html text which contains html tags
     * @return cleaned text without any tags
     */
    @JvmStatic
    fun stripHtml(html: String): String {
        return fromHtml(html).toString()
    }

    @JvmStatic
    fun isBackgroundServicePermitted(context: Context): Boolean {
        return isBackgroundServiceEnabled(context)
                && (isBackgroundServiceAlwaysEnabled(context) || NetUtils.isConnectedWifi(context))
    }

    private fun isBackgroundServiceEnabled(context: Context): Boolean {
        return Utils.getSettingBool(context, Const.BACKGROUND_MODE, false)
    }

    private fun isBackgroundServiceAlwaysEnabled(context: Context): Boolean {
        return "0" == Utils.getSetting(context, "background_mode_set_to", "0")
    }

    @JvmStatic
    @TargetApi(Build.VERSION_CODES.N)
    @Suppress("deprecation")
    fun fromHtml(source: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
        else
            Html.fromHtml(source)
    }

    @JvmStatic
    fun getBatteryLevel(context: Context): Float {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                ?: return -1f
        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        return if (level == -1 || scale == -1) {
            -1f
        } else level.toFloat() / scale.toFloat() * 100.0f
    }

    @JvmStatic
    fun extractRoomNumberFromLocation(location: String): String {
        val pattern = Pattern.compile("\\((.*?)\\)")
        val matcher = pattern.matcher(location)
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            location
        }
    }

    /**
     * Creates a bitmap for a vector image (.xml) to be able to use it for notifications.
     *
     * @param c   the current context
     * @param res the resource id of the drawable we want
     * @return bitmap of the xml vector graphic
     */
    @JvmStatic
    fun getLargeIcon(c: Context, res: Int): Bitmap? {
        val icon = ContextCompat.getDrawable(c, res) ?: return null
        val bitmap = Bitmap.createBitmap(icon.intrinsicWidth, icon.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        icon.setBounds(0, 0, canvas.width, canvas.height)
        icon.draw(canvas)
        return bitmap
    }
}
