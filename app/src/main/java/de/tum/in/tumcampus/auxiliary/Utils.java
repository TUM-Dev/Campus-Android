package de.tum.in.tumcampus.auxiliary;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.tum.in.tumcampus.BuildConfig;

/**
 * Class for common helper functions used by a lot of classes
 */
public final class Utils {

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
        css = "<style type=\"text/css\">" + css + "</style>";
        body = "<body>" + body + "</body>";
        String footer = "</html>";
        return header + css + body + footer;
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
     * Returns the number of datasets in a table
     *
     * @param db    Database connection
     * @param table Table name
     * @return number of datasets in a table
     */
    public static int dbGetTableCount(SQLiteDatabase db, String table) {
        Cursor c = db.rawQuery("SELECT count(*) FROM " + table, null);
        try {
            if (c.moveToNext()) {
                return c.getInt(0);
            }
            return 0;
        } finally {
            c.close();
        }
    }

    /**
     * Converts a date-string to Date
     *
     * @param str String with ISO-Date (yyyy-mm-dd)
     * @return Date
     */
    public static Date getDate(String str) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.parse(str);
        } catch (Exception e) {
            log(e, str);
        }
        return new Date();
    }

    /**
     * Converts Date to an ISO date-string
     *
     * @param d Date
     * @return String (yyyy-mm-dd)
     */
    public static String getDateString(Date d) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(d);
    }

    /**
     * Converts Date to an ISO datetime-string
     *
     * @param d Date
     * @return String (yyyy-mm-dd hh:mm:ss)
     */
    public static String getDateTimeString(Date d) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(d);
    }

    /**
     * Converts a datetime-string to Date
     *
     * @param str String with ISO-DateTime (yyyy-mm-dd hh:mm:ss)
     * @return Date
     */
    public static Date getISODateTime(String str) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.parse(str);
        } catch (Exception e) {
            log(e, str);
        }
        return new Date();
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
     * use {@link #log(Exception, String)} instead.
     *
     * @param e Exception (source for message and stack trace)
     */
    public static void log(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String s = Thread.currentThread().getStackTrace()[3].getClassName().replaceAll("[a-zA-Z0-9.]+\\.", "");
        Log.e(s, e + "\n" + sw.toString());
    }

    /**
     * Logs an exception and additional information
     * Use this anywhere in the app when a fatal error occurred.
     * If you can't give an exact error description simply use
     * {@link #log(Exception)} instead.
     *
     * @param e       Exception (source for message and stack trace)
     * @param message Additional information for exception message
     */
    public static void log(Exception e, String message) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String s = Thread.currentThread().getStackTrace()[3].getClassName().replaceAll("[a-zA-Z0-9.]+\\.", "");
        Log.e(s, e + " " + message + "\n" + sw.toString());
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
        String s = Thread.currentThread().getStackTrace()[3].getClassName().replaceAll("[a-zA-Z0-9.]+\\.", "");
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
        String s = Thread.currentThread().getStackTrace()[3].getClassName().replaceAll("[a-zA-Z0-9.]+\\.", "");
        Log.v(s, message);
    }

    /**
     * Get md5 hash from string
     *
     * @param str String to hash
     * @return md5 hash as string
     */
    public static String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(str.getBytes());
            BigInteger bigInt = new BigInteger(1, md.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            log(e, str);
        }
        return "";
    }

    /**
     * Returns a String[]-List from a CSV input stream
     *
     * @param fin CSV input stream
     * @return String[]-List with Columns matched to array values
     */
    public static List<String[]> readCsv(InputStream fin) {
        List<String[]> list = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(fin, "ISO-8859-1"));
            String reader;
            while ((reader = in.readLine()) != null) {
                list.add(splitCsvLine(reader));
            }
            in.close();
        } catch (Exception e) {
            log(e, "");
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
        sp.edit().putBoolean(key, value).apply();
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
        sp.edit().putString(key, value).apply();
    }

    /**
     * Sets the value of a setting
     *
     * @param c   Context
     * @param key setting key
     */
    public static void setSetting(Context c, String key, Object value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        sp.edit().putString(key, new Gson().toJson(value)).apply();
    }

    /**
     * Shows a long {@link Toast} message.
     *
     * @param context The activity where the toast is shown
     * @param msg     The toast message id
     */
    public static void showToast(Context context, int msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows a long {@link Toast} message.
     *
     * @param context The activity where the toast is shown
     * @param msg     The toast message
     */
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Splits a line from a CSV file into column values
     * <p/>
     * e.g. "aaa;aaa";"bbb";1 gets aaa,aaa;bbb;1;
     *
     * @param str CSV line
     * @return String[] with CSV column values
     */
    private static String[] splitCsvLine(String str) {
        StringBuilder result = new StringBuilder();
        boolean open = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '"') {
                open = !open;
                continue;
            }
            if (open && c == ';') {
                result.append(",");
            } else {
                result.append(c);
            }
        }
        // fix trailing ";", e.g. ";;;".split().length = 0
        result.append("; ");
        return result.toString().split(";");
    }

    /**
     * Converts a meter based value to a formatted string
     *
     * @param meters Meters to represent
     * @return Formatted meters. e.g. 10m, 12.5km
     */
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
        prefs.edit().putBoolean(key, value).apply();
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
        prefs.edit().putInt(key, value).apply();
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
        prefs.edit().putLong(key, value).apply();
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
        prefs.edit().putString(key, value).apply();
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
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static void showToastOnUIThread(final Activity activity, final int s) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showToast(activity, s);
            }
        });
    }

    public static void showToastOnUIThread(final Activity activity, final String s) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showToast(activity, s);
            }
        });
    }


    /**
     * Loads the private key from preferences
     *
     * @return The private key object
     */
    public static PrivateKey getPrivateKeyFromSharedPrefs(Context context) {
        String privateKeyString = Utils.getInternalSettingString(context, Const.PRIVATE_KEY, "");
        if (privateKeyString.isEmpty())
            return null;

        byte[] privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT);
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Utils.log(e);
        }
        return null;
    }
}
