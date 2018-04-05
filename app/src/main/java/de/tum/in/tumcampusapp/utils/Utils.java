package de.tum.in.tumcampusapp.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Charsets;
import com.google.common.escape.CharEscaperBuilder;
import com.google.common.escape.Escaper;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.BuildConfig;

/**
 * Class for common helper functions used by a lot of classes.
 */
public final class Utils {
    private static final String LOGGING_REGEX = "[a-zA-Z0-9.]+\\.";

    private Utils() {
        // Utils is a utility class
    }

    /**
     * Builds a HTML document out of a css file and the body content.
     *
     * @param css  The CSS specification
     * @param body The body content
     * @return The HTML document.
     */
    public static String buildHTMLDocument(String css, String body) {
        String header = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"de\" lang=\"de\">" +
                        "<head><meta name=\"viewport\" content=\"width=device-width\" />" +
                        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head>";
        String resultCss = "<style type=\"text/css\">" + css + "</style>";
        String resultBody = "<body>" + body + "</body>";
        String footer = "</html>";
        //noinspection StringConcatenationMissingWhitespace
        return header + resultCss + resultBody + footer;
    }

    /**
     * Cut substring from a text.
     *
     * @param text        The text.
     * @param startString Start string where the cutting begins.
     * @param endString   End string where the cutting ends.
     * @return The cut text.
     */
    public static String cutText(String text, String startString, String endString) {
        int startPos = text.indexOf(startString);
        int endPos = text.indexOf(endString, startPos);

        if (startPos == -1) {
            startPos = 0;
        }
        if (endPos == -1 || endPos < startPos) {
            endPos = text.length();
        }

        return text.substring(startPos + startString.length(), endPos);
    }

    /**
     * Get a value from the default shared preferences
     *
     * @param c          Context
     * @param key        setting name
     * @param defaultVal default value
     * @return setting value, "" if undefined
     */
    public static String getSetting(Context c, String key, String defaultVal) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getString(key, defaultVal);
    }

    /**
     * Get a value from the default shared preferences
     *
     * @param c         Context
     * @param key       setting name
     * @param classInst e.g. ChatMember.class
     * @return setting value, "" if undefined
     */
    public static <T> T getSetting(Context c, String key, Class<T> classInst) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        String val = sp.getString(key, null);
        if (val != null) {
            return new Gson().fromJson(val, classInst);
        }
        return null;
    }

    /**
     * Return the boolean value of a setting
     *
     * @param c          Context
     * @param name       setting name
     * @param defaultVal default value
     * @return true if setting was checked, else value
     */
    public static boolean getSettingBool(Context c, String name, boolean defaultVal) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getBoolean(name, defaultVal);
    }

    /**
     * Logs an exception and additional information
     * Use this anywhere in the app when a fatal error occurred.
     * If you can give a better description of what went wrong
     * use {@link #log(Throwable, String)} instead.
     *
     * @param e Exception (source for message and stack trace)
     */
    public static void log(Throwable e) {
        try (StringWriter sw = new StringWriter()) {
            e.printStackTrace(new PrintWriter(sw));
            String s = Thread.currentThread()
                             .getStackTrace()[3].getClassName()
                                                .replaceAll(LOGGING_REGEX, "");
            Log.e(s, e + "\n" + sw);
        } catch (IOException e1) {
            // there is a time to stop logging errors
        }
    }

    /**
     * Logs an exception and additional information
     * Use this anywhere in the app when a fatal error occurred.
     * If you can't give an exact error description simply use
     * {@link #log(Throwable)} instead.
     *
     * @param e       Exception (source for message and stack trace)
     * @param message Additional information for exception message
     */
    public static void log(Throwable e, String message) {
        try (StringWriter sw = new StringWriter()) {
            e.printStackTrace(new PrintWriter(sw));
            String s = Thread.currentThread()
                             .getStackTrace()[3].getClassName()
                                                .replaceAll(LOGGING_REGEX, "");
            Log.e(s, e + " " + message + '\n' + sw);
        } catch (IOException e1) {
            // there is a time to stop logging errors
        }
    }

    /**
     * Logs a message
     * Use this to log the current app state.
     *
     * @param message Information or Debug message
     */
    public static void log(String message) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        String s = Thread.currentThread()
                         .getStackTrace()[3].getClassName()
                                            .replaceAll(LOGGING_REGEX, "");
        Log.d(s, message);
    }

    /**
     * Logs a message
     * Use this to log additional information that is not important in most cases.
     *
     * @param message Information or Debug message
     */
    public static void logv(String message) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        String s = Thread.currentThread()
                         .getStackTrace()[3].getClassName()
                                            .replaceAll(LOGGING_REGEX, "");
        Log.v(s, message);
    }

    /**
     * Logs a message with specified tag
     * Use this to log a particular work
     *
     * @param message Information or Debug message
     */
    public static void logwithTag(String tag, String message) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        Log.v(tag, message);
    }

    /**
     * Get a persistent hash from string
     *
     * @param str String to hash
     * @return hash hash as string
     */
    static String hash(String str) {
        return Hashing.murmur3_128()
                      .hashBytes(str.getBytes(Charsets.UTF_8))
                      .toString();
    }

    /**
     * Returns a String[]-List from a CSV input stream
     *
     * @param fin CSV input stream
     * @return String[]-List with Columns matched to array values
     */
    public static List<String[]> readCsv(InputStream fin) {
        List<String[]> list = new ArrayList<>(64);
        try {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(fin, Charsets.UTF_8))) {
                String reader;
                while ((reader = in.readLine()) != null) {
                    list.add(splitCsvLine(reader));
                }
            }
        } catch (IOException e) {
            log(e);
        }
        return list;
    }

    /**
     * Sets the value of a setting
     *
     * @param c   Context
     * @param key setting key
     */
    public static void setSetting(Context c, String key, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        sp.edit()
          .putBoolean(key, value)
          .apply();
    }

    /**
     * Sets the value of a setting
     *
     * @param c     Context
     * @param key   setting key
     * @param value String value
     */
    public static void setSetting(Context c, String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        sp.edit()
          .putString(key, value)
          .apply();
    }

    /**
     * Sets the value of a setting
     *
     * @param c   Context
     * @param key setting key
     */
    public static void setSetting(Context c, String key, Object value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        sp.edit()
          .putString(key, new Gson().toJson(value))
          .apply();
    }

    /**
     * Shows a long {@link Toast} message.
     *
     * @param context The activity where the toast is shown
     * @param msg     The toast message id
     */
    public static void showToast(Context context, int msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG)
             .show();
    }

    /**
     * Shows a long {@link Toast} message.
     *
     * @param context The activity where the toast is shown
     * @param msg     The toast message
     */
    public static void showToast(Context context, CharSequence msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG)
             .show();
    }

    /**
     * Splits a line from a CSV file into column values
     * <p/>
     * e.g. "aaa;aaa";"bbb";1 gets aaa,aaa;bbb;1;
     *
     * @param str CSV line
     * @return String[] with CSV column values
     */
    private static String[] splitCsvLine(CharSequence str) {
        StringBuilder result = new StringBuilder();
        boolean open = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '"') {
                open = !open;
                continue;
            }
            if (open && c == ';') {
                result.append(',');
            } else {
                result.append(c);
            }
        }
        // fix trailing ";", e.g. ";;;".split().length = 0
        result.append("; ");
        return result.toString()
                     .split(";");
    }

    /**
     * Converts a meter based value to a formatted string
     *
     * @param meters Meters to represent
     * @return Formatted meters. e.g. 10m, 12.5km
     */
    @SuppressWarnings("StringConcatenationMissingWhitespace")
    public static String formatDist(float meters) {
        if (meters < 1000) {
            return ((int) meters) + "m";
        } else if (meters < 10000) {
            int front = (int) (meters / 1000f);
            int back = (int) Math.abs(meters / 100f) % 10;
            return front + "." + back + "km";
        } else {
            return ((int) (meters / 1000f)) + "km";
        }
    }

    /**
     * Sets an internal preference's boolean value
     *
     * @param context Context
     * @param key     Key
     * @param value   Value
     */
    public static void setInternalSetting(Context context, String key, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(Const.INTERNAL_PREFS, Context.MODE_PRIVATE);
        prefs.edit()
             .putBoolean(key, value)
             .apply();
    }

    /**
     * Sets an internal preference's integer value
     *
     * @param context Context
     * @param key     Key
     * @param value   Value
     */
    public static void setInternalSetting(Context context, String key, int value) {
        SharedPreferences prefs = context.getSharedPreferences(Const.INTERNAL_PREFS, Context.MODE_PRIVATE);
        prefs.edit()
             .putInt(key, value)
             .apply();
    }

    /**
     * Sets an internal preference's long value
     *
     * @param context Context
     * @param key     Key
     * @param value   Value
     */
    public static void setInternalSetting(Context context, String key, long value) {
        SharedPreferences prefs = context.getSharedPreferences(Const.INTERNAL_PREFS, Context.MODE_PRIVATE);
        prefs.edit()
             .putLong(key, value)
             .apply();
    }

    /**
     * Sets an internal preference's string value
     *
     * @param context Context
     * @param key     Key
     * @param value   Value
     */
    public static void setInternalSetting(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(Const.INTERNAL_PREFS, Context.MODE_PRIVATE);
        prefs.edit()
             .putString(key, value)
             .apply();
    }

    /**
     * Sets an internal preference's string value
     *
     * @param context Context
     * @param key     Key
     * @param value   Value
     */
    public static void setInternalSetting(Context context, String key, float value) {
        SharedPreferences prefs = context.getSharedPreferences(Const.INTERNAL_PREFS, Context.MODE_PRIVATE);
        prefs.edit()
             .putFloat(key, value)
             .apply();
    }

    /**
     * Gets an internal preference's boolean value
     *
     * @param context Context
     * @param key     Key
     * @param value   Default value
     * @return The value of the setting or the default value,
     * if no setting with the specified key exists
     */
    public static boolean getInternalSettingBool(Context context, String key, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(Const.INTERNAL_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(key, value);
    }

    /**
     * Gets an internal preference's integer value
     *
     * @param context Context
     * @param key     Key
     * @param value   Default value
     * @return The value of the setting or the default value,
     * if no setting with the specified key exists
     */
    public static int getInternalSettingInt(Context context, String key, int value) {
        SharedPreferences prefs = context.getSharedPreferences(Const.INTERNAL_PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(key, value);
    }

    /**
     * Gets an internal preference's long value
     *
     * @param context Context
     * @param key     Key
     * @param value   Default value
     * @return The value of the setting or the default value,
     * if no setting with the specified key exists
     */
    public static long getInternalSettingLong(Context context, String key, long value) {
        SharedPreferences prefs = context.getSharedPreferences(Const.INTERNAL_PREFS, Context.MODE_PRIVATE);
        return prefs.getLong(key, value);
    }

    /**
     * Gets an internal preference's string value
     *
     * @param context Context
     * @param key     Key
     * @param value   Default value
     * @return The value of the setting or the default value,
     * if no setting with the specified key exists
     */
    public static String getInternalSettingString(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(Const.INTERNAL_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(key, value);
    }

    /**
     * Gets an internal preference's float value
     *
     * @param context Context
     * @param key     Key
     * @param value   Default value
     * @return The value of the setting or the default value,
     * if no setting with the specified key exists
     */
    public static float getInternalSettingFloat(Context context, String key, float value) {
        SharedPreferences prefs = context.getSharedPreferences(Const.INTERNAL_PREFS, Context.MODE_PRIVATE);
        return prefs.getFloat(key, value);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            return context.getPackageManager()
                          .getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
        }
        return 0;
    }

    public static void showToastOnUIThread(final Activity activity, final int s) {
        activity.runOnUiThread(() -> Utils.showToast(activity, s));
    }

    public static void showToastOnUIThread(final Activity activity, final CharSequence s) {
        activity.runOnUiThread(() -> Utils.showToast(activity, s));
    }

    /**
     * Removes all html tags from a string
     *
     * @param html text which contains html tags
     * @return cleaned text without any tags
     */
    public static String stripHtml(String html) {
        return fromHtml(html).toString();
    }

    public static boolean isBackgroundServicePermitted(Context context) {
        return isBackgroundServiceEnabled(context) && (isBackgroundServiceAlwaysEnabled(context) || NetUtils.isConnectedWifi(context));
    }

    private static boolean isBackgroundServiceEnabled(Context context) {
        return Utils.getSettingBool(context, Const.BACKGROUND_MODE, false);
    }

    private static boolean isBackgroundServiceAlwaysEnabled(Context context) {
        return "0".equals(Utils.getSetting(context, "background_mode_set_to", "0"));
    }

    public static String arrayListToString(Iterable<String> array) {
        return TextUtils.join(",", array);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
               Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY) :
               Html.fromHtml(source);
    }

    private static Escaper umlautEscaper() {
        return new CharEscaperBuilder()
                .addEscape('ä', "&auml;")
                .addEscape('ö', "&ouml;")
                .addEscape('ü', "&uuml;")
                .toEscaper();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static String escapeUmlauts(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return Html.escapeHtml(text);
        }
        // Just escape umlauts for older devices, MVV should be happy with that
        return umlautEscaper().escape(text);
    }

    public static float getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent == null) {
            return -1;
        }
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level == -1 || scale == -1) {
            return -1;
        }
        return ((float) level / (float) scale) * 100.0f;
    }

    public static String extractRoomNumberFromLocation(String location) {
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(location);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return location;
        }
    }

    /**
     * Creates a bitmap for a vector image (.xml) to be able to use it for notifications.
     *
     * @param c   the current context
     * @param res the resource id of the drawable we want
     * @return bitmap of the xml vector graphic
     */

    public static Bitmap getLargeIcon(Context c, int res) {
        Drawable icon = c.getResources()
                         .getDrawable(res);
        Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        icon.draw(canvas);
        return bitmap;
    }

}
