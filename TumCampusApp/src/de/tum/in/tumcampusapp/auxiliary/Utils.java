package de.tum.in.tumcampusapp.auxiliary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

/** Class for helper functions */
@SuppressLint("SimpleDateFormat")
public class Utils {
	/** Counter for unfinished downloads */
	public static int openDownloads = 0;

	/**
	 * Builds a HTML document out of a css file and the body content.
	 * 
	 * @param css
	 *            The CSS specification
	 * @param body
	 *            The body content
	 * @return The HTML document.
	 */
	public static String buildHTMLDocument(String css, String body) {
		String header = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"de\" lang=\"de\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head>";
		css = "<style type=\"text/css\">" + css + "</style>";
		body = "<body>" + body + "</body>";
		String footer = "</html>";
		return header + css + body + footer;
	}

	/**
	 * Convert an input stream to a string
	 * 
	 * <pre>
	 * @param is input stream from file, download
	 * @return output string
	 * </pre>
	 */
	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		// TODO Who handles exception?
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * Cut substring from a text.
	 * 
	 * @param text
	 *            The text.
	 * @param startString
	 *            Start string where the cutting begins.
	 * @param endString
	 *            End string where the cutting ends.
	 * @return The cut text.
	 */
	public static String cutText(String text, String startString,
			String endString) {
		int startPos = text.indexOf(startString);
		int endPos = text.indexOf(endString, startPos) - endString.length();

		if (startPos == -1) {
			startPos = 1;
		}
		if (endPos == -1 || endPos < startPos) {
			endPos = text.length();
		}

		text = text.substring(startPos, endPos);

		return text;
	}

	/**
	 * Returns the number of datasets in a table
	 * 
	 * <pre>
	 * @param db Database connection
	 * @param table Table name
	 * @return number of datasets in a table
	 * </pre>
	 */
	public static int dbGetTableCount(SQLiteDatabase db, String table) {
		Cursor c = db.rawQuery("SELECT count(*) FROM " + table, null);
		if (c.moveToNext()) {
			return c.getInt(0);
		}
		return 0;
	}

	/**
	 * Checks if a database table exists
	 * 
	 * <pre>
	 * @param db Database connection
	 * @param table Table name
	 * @return true if table exists, else false
	 * </pre>
	 */
	public static boolean dbTableExists(SQLiteDatabase db, String table) {
		// TODO Who handles exception?
		try {
			Cursor c = db.rawQuery("SELECT 1 FROM " + table + " LIMIT 1", null);
			if (c.moveToNext()) {
				return true;
			}
		} catch (Exception e) {
			log(e, "");
		}
		return false;
	}

	/**
	 * 
	 * @param dir
	 */
	public static void deleteAllCacheData(File dir) {
		Log.d("DeleteRecursive", "DELETEPREVIOUS TOP" + dir.getPath());
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				File temp = new File(dir, children[i]);
				if (temp.isDirectory()) {
					Log.d("DeleteRecursive", "Recursive Call" + temp.getPath());
					deleteAllCacheData(temp);
				} else {
					Log.d("DeleteRecursive", "Delete File" + temp.getPath());
					boolean b = temp.delete();
					if (b == false) {
						Log.d("DeleteRecursive", "DELETE FAIL");
					}
				}
			}

		}
		dir.delete();
	}

	/**
	 * Download a file in the same thread
	 * 
	 * <pre>
	 * @param url Download location
	 * @param target Target filename in local file system
	 * @throws Exception
	 * </pre>
	 */
	private static void downloadFile(String url, String target)
			throws Exception {
		File f = new File(target);
		if (f.exists()) {
			return;
		}
		HttpClient httpclient = new DefaultHttpClient();
		HttpEntity entity = httpclient.execute(new HttpGet(url)).getEntity();

		if (entity == null) {
			return;
		}
		File file = new File(target);
		InputStream in = entity.getContent();

		FileOutputStream out = new FileOutputStream(file);
		byte[] buffer = new byte[8192];
		int count = -1;
		while ((count = in.read(buffer)) != -1) {
			out.write(buffer, 0, count);
		}
		out.flush();
		out.close();
		in.close();
	}

	/**
	 * Download a file in a new thread
	 * 
	 * <pre>
	 * @param url Download location
	 * @param target Target filename in local file system
	 * </pre>
	 */
	public static void downloadFileThread(final String url, final String target) {
		openDownloads++;
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Who handles exception?
				try {
					Utils.log(url);
					downloadFile(url, target);
					openDownloads--;
				} catch (Exception e) {
					log(e, url);
				}
			}
		}).start();
	}

	/**
	 * Download an icon in the same thread
	 * 
	 * <pre>
	 * @param url Download location
	 * @param target Target filename in local file system
	 * @throws Exception
	 * </pre>
	 */
	private static void downloadIconFile(String url, String target)
			throws Exception {
		File f = new File(target);
		if (f.exists()) {
			return;
		}
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);

		// force mobile version of a web page
		httpget.addHeader("User-Agent",
				"Mozilla/5.0 (iPhone; de-de) AppleWebKit/528.18 Safari/528.16");

		HttpEntity entity = httpclient.execute(httpget).getEntity();
		if (entity == null) {
			return;
		}
		InputStream in = entity.getContent();
		String data = convertStreamToString(in);

		String icon = "";
		Pattern link = Pattern.compile("<link[^>]+>");
		Pattern href = Pattern.compile("href=[\"'](.+?)[\"']");

		Matcher matcher = link.matcher(data);
		while (matcher.find()) {
			String match = matcher.group(0);

			Matcher href_match = href.matcher(match);
			if (href_match.find()) {
				if (match.contains("shortcut icon") && icon.length() == 0) {
					icon = href_match.group(1);
				}
				if (match.contains("apple-touch-icon")) {
					icon = href_match.group(1);
				}
			}
		}

		Uri uri = Uri.parse(url);
		// icon not found
		if (icon.length() == 0) {
			icon = "http://" + uri.getHost() + "/favicon.ico";
		}
		// relative url
		if (!icon.contains("://")) {
			icon = "http://" + uri.getHost() + "/" + icon;
		}
		// download icon
		downloadFile(icon, target);
	}

	/**
	 * Download an icon in a new thread
	 * 
	 * <pre>
	 * @param url Download location
	 * @param target Target filename in local file system
	 * </pre>
	 */
	public static void downloadIconFileThread(final String url,
			final String target) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Who handles exception?
				try {
					Utils.log(url);
					downloadIconFile(url, target);
				} catch (Exception e) {
					log(e, url);
				}
			}
		}).start();
	}

	/**
	 * 
	 * @param url
	 * @param target
	 */
	public static void downloadImageAndCompressThread(final String url,
			final String target, final String targetImageThumbnail) {
		openDownloads++;
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Who handles exception?
				try {
					Utils.log(url);
					downloadFile(url, target);
					openDownloads--;

					try {
						Bitmap sourceImage = BitmapFactory.decodeFile(target);
						Bitmap thumbail = Bitmap.createScaledBitmap(
								sourceImage, 200, 200, false);

						FileOutputStream out = new FileOutputStream(
								targetImageThumbnail);
						thumbail.compress(Bitmap.CompressFormat.JPEG, 60, out);
						out.flush();
						out.close();
					} catch (Exception e) {
						Log.w("Gallery", "Error scaling image");
					}
				} catch (Exception e) {
					log(e, url);
				}
			}
		}).start();
	}

	/**
	 * Download a JSON stream from a URL
	 * 
	 * <pre>
	 * @param url Valid URL
	 * @return JSONObject
	 * @throws Exception
	 * </pre>
	 */
	public static JSONObject downloadJson(String url) throws Exception {
		Utils.log(url);

		HttpClient httpclient = new DefaultHttpClient();
		HttpParams params = httpclient.getParams();
		HttpConnectionParams.setSoTimeout(params, Const.HTTP_TIMEOUT);
		HttpConnectionParams.setConnectionTimeout(params, Const.HTTP_TIMEOUT);

		HttpEntity entity = null;
		try {
			entity = httpclient.execute(new HttpGet(url)).getEntity();
		} catch (Exception e) {
			// Throw a new TimeputException which is treted later
			throw new TimeoutException("HTTP Timeout");
		}

		String data = "";
		if (entity != null) {

			// JSON Response Read
			InputStream instream = entity.getContent();
			data = convertStreamToString(instream);

			Utils.log(data);
			instream.close();
		}
		return new JSONObject(data);
	}

	/**
	 * Download a String from a URL
	 * 
	 * <pre>
	 * @param url Valid URL
	 * @return String
	 * @throws Exception
	 * </pre>
	 */
	public static String downloadString(String url) throws Exception {
		Utils.log(url);

		HttpClient httpclient = new DefaultHttpClient();
		HttpEntity entity = httpclient.execute(new HttpGet(url)).getEntity();
		if (entity == null) {
			return "";
		}
		String data = EntityUtils.toString(entity);
		Utils.log(data);
		return data;
	}

	/**
	 * Deletes all contents of a cache directory
	 * 
	 * <pre>
	 * @param directory directory postfix (e.g. feeds/cache)
	 * </pre>
	 */
	public static void emptyCacheDir(String directory) {
		// TODO Who handles exception?
		try {
			File dir = new File(getCacheDir(directory));
			if (dir.isDirectory() && dir.canWrite()) {
				for (String child : dir.list()) {
					new File(dir, child).delete();
				}
			}
		} catch (Exception e) {
			log(e, directory);
		}
	}

	/**
	 * Puts a .nomedia file into each directory listed, so the images don't
	 * appear in the Gallery.
	 * 
	 * <pre>
	 * @throws Exception
	 * </pre>
	 */

	public static void ensureImagesAreNotIndexed() throws Exception {
		// ArrayList<String> directoriesNotToIndex = new ArrayList<String>();
		// directoriesNotToIndex.add("rss/cache");
		// directoriesNotToIndex.add("feeds/cache");
		// directoriesNotToIndex.add("gallery/cache");
		String[] directoriesNotToIndex = { "organisations/cache",
				"links/cache", "rss/cache", "feeds/cache", "gallery/cache",
				"news/cache" };
		for (String directory : directoriesNotToIndex) {
			File file = new File(getCacheDir(directory) + ".nomedia");
			if (!file.exists()) {
				file.createNewFile();
				FileOutputStream fOut = new FileOutputStream(file);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
				myOutWriter.append("1");
				myOutWriter.close();
				fOut.close();
			}
		}
	}

	/**
	 * Returns the full path of a cache directory and checks if it is readable
	 * and writable
	 * 
	 * <pre>
	 * @param directory directory postfix (e.g. feeds/cache)
	 * @return full path of the cache directory
	 * @throws Exception
	 * </pre>
	 */
	// TODO Think how not to hardcode Exception text.
	public static String getCacheDir(String directory) throws IOException {
		File f = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/tumcampus/" + directory);
		if (!f.exists()) {
			f.mkdirs();
		}
		if (!f.canRead()) {
			throw new IOException("Cannot read from SD-Card");
		}
		if (!f.canWrite()) {
			throw new IOException("Cannot write to SD-Card");
		}
		return f.getPath() + "/";
	}

	/**
	 * Converts a date-string to Date
	 * 
	 * <pre>
	 * @param str String with ISO-Date (yyyy-mm-dd)
	 * @return Date
	 * </pre>
	 */
	public static Date getDate(String str) {
		// TODO Who handles exception?
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
	 * <pre>
	 * @param d Date
	 * @return String (yyyy-mm-dd)
	 * </pre>
	 */
	public static String getDateString(Date d) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(d);
	}

	/**
	 * Converts Date to a German date-string
	 * 
	 * <pre>
	 * @param d Date
	 * @return String (dd.mm.yyyy)
	 * </pre>
	 */
	public static String getDateStringDe(Date d) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		return dateFormat.format(d);
	}

	/**
	 * Converts a datetime-string to Date
	 * 
	 * <pre>
	 * @param str String with ISO-DateTime (yyyy-mm-ddThh:mm:ss)
	 * @return Date
	 * </pre>
	 */
	public static Date getDateTime(String str) {
		// TODO Who handles exception?
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss");
			return dateFormat.parse(str);
		} catch (Exception e) {
			log(e, str);
		}
		return new Date();
	}

	/**
	 * Converts a German datetime-string to Date
	 * 
	 * <pre>
	 * @param str String with German-DateTime (dd.mm.yyyy hh:mm)
	 * @return Date
	 * </pre>
	 */
	public static Date getDateTimeDe(String str) {
		// TODO Who handles exception?
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"dd.MM.yyyy HH:mm");
			return dateFormat.parse(str);
		} catch (Exception e) {
			log(e, str);
		}
		return new Date();
	}

	/**
	 * Converts a datetime-string to Date
	 * 
	 * <pre>
	 * @param str String with ISO-DateTime (yyyy-mm-ddThh:mm:ss)
	 * @return Date
	 * </pre>
	 */
	public static Date getDateTimeISO(String str) {
		// TODO Who handles exception?
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			return dateFormat.parse(str);
		} catch (Exception e) {
			log(e, str);
		}
		return new Date();
	}

	/**
	 * Converts a rfc822 datetime-string to Date
	 * 
	 * <pre>
	 * @param str String with RFC822-Date (e.g. Tue, 12 Jul 2011 14:30:00)
	 * @return Date
	 * </pre>
	 */
	public static Date getDateTimeRfc822(String str) {
		// TODO Who handles exception?
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss", Locale.US);
			return dateFormat.parse(str);
		} catch (Exception e) {
			log(e, str);
		}
		return new Date();
	}

	/**
	 * Converts Date to an ISO datetime-string
	 * 
	 * <pre>
	 * @param d Date
	 * @return String (yyyy-mm-dd hh:mm:ss)
	 * </pre>
	 */
	public static String getDateTimeString(Date d) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(d);
	}

	/**
	 * Configure a {@link WebView} with default settings.
	 * 
	 * @param context
	 *            Context of web view
	 * @param id
	 *            id of web view
	 * @return configured web view
	 */
	public static WebView getDefaultWebView(Activity context, int id) {
		WebView webView = (WebView) context.findViewById(id);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
		webView.setHorizontalScrollBarEnabled(true);
		webView.getSettings().setUseWideViewPort(true);
		return webView;
	}

	/**
	 * Converts a datetime-string to Date
	 * 
	 * <pre>
	 * @param str String with ISO-DateTime (yyyy-mm-ddThh:mm:ss)
	 * @return Date
	 * </pre>
	 */
	public static Date getISODateTime(String str) {
		// TODO Who handles exception?
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			return dateFormat.parse(str);
		} catch (Exception e) {
			log(e, str);
		}
		return new Date();
	}

	/**
	 * Returns a URL from an internet shortcut file (.url)
	 * 
	 * <pre>
	 * @param file Internet shortcut file (.url)
	 * @return URL
	 * </pre>
	 */
	public static String getLinkFromUrlFile(File file) {
		// TODO Who handles exception?
		try {
			byte[] buffer = new byte[(int) file.length()];
			FileInputStream in = new FileInputStream(file.getAbsolutePath());
			in.read(buffer);
			in.close();
			Pattern pattern = Pattern.compile("URL=(.*?)$");
			Matcher matcher = pattern.matcher(new String(buffer));
			matcher.find();
			return matcher.group(1);
		} catch (Exception e) {
			log(e, file.toString());
		}
		return "";
	}

	/**
	 * Returns a RSS-URL from a web page URL
	 * 
	 * e.g. http://www.spiegel.de returns http://www.spiegel.de/index.rss
	 * 
	 * <pre>
	 * @param url Web page URL
	 * @return RSS-URL
	 * </pre>
	 */
	public static String getRssLinkFromUrl(String url) {
		Utils.log(url);

		String result = url;
		// TODO Who handles exception?
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpEntity entity = httpclient.execute(new HttpGet(url))
					.getEntity();
			if (entity == null) {
				return result;
			}
			InputStream instream = entity.getContent();
			String data = convertStreamToString(instream);

			if (data.startsWith("<?xml")) {
				return result;
			}
			Pattern link = Pattern.compile("<link[^>]+>");
			Pattern href = Pattern.compile("href=[\"'](.+?)[\"']");

			Matcher matcher = link.matcher(data);
			while (matcher.find()) {
				String match = matcher.group(0);

				Matcher href_match = href.matcher(match);
				if (href_match.find()
						&& (match.contains("application/rss+xml") || match
								.contains("application/atom+xml"))) {
					result = href_match.group(1);
				}
			}

			// relative url
			Uri uri = Uri.parse(url);
			if (!result.contains("://")) {
				result = "http://" + uri.getHost() + "/" + result;
			}
		} catch (Exception e) {
			log(e, url);
		}
		return result;
	}

	/**
	 * Return the value of a setting
	 * 
	 * <pre>
	 * @param c Context
	 * @param name setting name
	 * @return setting value, "" if undefined
	 * </pre>
	 */
	public static String getSetting(Context c, String name) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		return sp.getString(name, "");
	}

	/**
	 * Return the boolean value of a setting
	 * 
	 * <pre>
	 * @param c Context
	 * @param name setting name
	 * @return true if setting was checked, else value
	 * </pre>
	 */
	public static boolean getSettingBool(Context c, String name) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		return sp.getBoolean(name, false);
	}

	/**
	 * this method will extract the german abreviation of a weekday
	 * 
	 * @author Daniel G. Mayr
	 * @param dObject
	 * @return
	 */
	// TODO Think how to make weekdays not hardcoded
	public static String getWeekDayByDate(Date dObject) {
		Calendar c = Calendar.getInstance();
		c.setTime(dObject);
		int wday = c.get(Calendar.DAY_OF_WEEK);
		switch (wday) {
		case Calendar.SUNDAY:
			return "So";
		case Calendar.MONDAY:
			return "Mo";
		case Calendar.TUESDAY:
			return "Di";
		case Calendar.WEDNESDAY:
			return "Mi";
		case Calendar.THURSDAY:
			return "Do";
		case Calendar.FRIDAY:
			return "Fr";
		case Calendar.SATURDAY:
			return "Sa";
		default:
			return null;
		}
	}

	/**
	 * Hides the keyboard.
	 * 
	 * @param activity
	 *            The corresponding activity.
	 * 
	 * @param binder
	 *            The view where the keyboard is bind to.
	 */
	public static void hideKeyboard(Activity activity, View binder) {
		InputMethodManager imm = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(binder.getWindowToken(), 0);
	}

	public static boolean isAccessTokenValid(String token) {
		if (token == null || token == "") {
			return false;
		}
		return true;
	}

	/**
	 * Check if a network connection is available or can be available soon
	 * 
	 * @return true if available
	 */
	public static boolean isConnected(Context con) {
		ConnectivityManager cm = (ConnectivityManager) con
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	/**
	 * Logs an exception and additional information
	 * 
	 * <pre>
	 * @param e Exception (source for message and stacktrace)
	 * @param message Additional information for exception message
	 * </pre>
	 */
	public static void log(Exception e, String message) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		Log.e("TumCampus", e + " " + message + "\n" + sw.toString());
	}

	/**
	 * Logs a message
	 * 
	 * <pre>
	 * @param message Information or Debug message
	 * </pre>
	 */
	public static void log(String message) {
		StackTraceElement s = Thread.currentThread().getStackTrace()[3];
		Log.d("TumCampus", s.toString() + " " + message);
	}

	/**
	 * Get md5 hash from string
	 * 
	 * <pre>
	 * @param str String to hash
	 * @return md5 hash as string
	 * </pre>
	 */
	public static String md5(String str) {
		// TODO Who handles exception?
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
	 * <pre>
	 * @param fin CSV input stream
	 * @param charset Encoding, e.g. ISO-8859-1
	 * @return String[]-List with Columns matched to array values
	 * </pre>
	 */
	public static List<String[]> readCsv(InputStream fin, String charset) {
		List<String[]> list = new ArrayList<String[]>();
		// TODO Who handles exception?
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(fin,
					charset));
			String reader = "";
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
	 * <pre>
	 * @param c Context
	 * @param key setting key
	 * </pre>
	 */
	public static void setSetting(Context c, String key, String value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		sp.edit().putString(key, value).commit();
	}

	/**
	 * Sets the boolean value of a setting
	 * 
	 * <pre>
	 * @param c Context
	 * @param name setting name
	 * @param value setting value
	 * </pre>
	 * 
	 * Manually added to Utils by Florian Schulz
	 */
	public static void setSettingBool(Context c, String name, boolean value) {
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(c).edit();
		editor.putBoolean(name, value);
		editor.commit();
	}

	/**
	 * Shows the keyboard.
	 * 
	 * @param activity
	 *            The corresponding activity.
	 * 
	 * @param binder
	 *            The view where the keyboard is bind to.
	 */
	public static void showKeyboard(Activity activity, View binder) {
		InputMethodManager imm = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(binder, 0);
	}

	/**
	 * Shows a long {@link Toast} centered on the screen.
	 * 
	 * @param activity
	 *            The activity where the toast is shown.
	 * @param msg
	 *            The toast message.
	 */
	public static void showLongCenteredToast(Activity activity, String msg) {
		Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}

	/**
	 * Splits a line from a CSV file into column values
	 * 
	 * e.g. "aaa;aaa";"bbb";1 gets aaa,aaa;bbb;1;
	 * 
	 * <pre>
	 * @param str CSV line
	 * @return String[] with CSV column values
	 * </pre>
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
	 * Truncates a string to a specified length and appends ...
	 * 
	 * <pre>
	 * @param str String
	 * @param limit maximum length
	 * @return truncated String
	 * </pre>
	 */
	public static String trunc(String str, int limit) {
		String result = str;
		if (str.length() > limit) {
			result = str.substring(0, limit) + " ...";
		}
		return result;
	}

}
