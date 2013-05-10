package de.tum.in.tumcampusapp.auxiliary;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;

/**
 * Utility functions to ease the work with files and file contents.
 * 
 * @author Vincenz Doelle
 */
public class FileUtils {
	public static final String HTML_TYPE = "text/html";
	// mime-types
	public static final String PDF_TYPE = "application/pdf";

	/**
	 * Fetches a document from a URL and writes the content to the given file.
	 * 
	 * @param httpClient
	 *            HTTP client used to fetch the document.
	 * @param url
	 *            The documents URL
	 * @param targetFile
	 *            The target file where the document's content should be written
	 *            to
	 * 
	 * @return The target file or null if error occurred.
	 */
	public static File getFileFromURL(DefaultHttpClient httpClient, String url, File targetFile) {
		// check required variables
		if (url == null || httpClient == null) {
			return targetFile;
		}

		try {
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpClient.execute(request);

			// download file from url
			HttpEntity entity = response.getEntity();
			InputStream in;
			in = entity.getContent();

			FileOutputStream fos;
			fos = new FileOutputStream(targetFile);

			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = in.read(buffer)) > 0) {
				fos.write(buffer, 0, len1);
			}

			fos.close();

			return targetFile;

		} catch (/* ClientProtocolException, IOException */Exception e) {
			Log.e("EXCEPTION", e.getMessage());
		}

		return null;
	}

	/**
	 * Removes whitespaces and appends the file type.
	 * 
	 * @param name
	 *            The string that should be used as filename.
	 * @param appendix
	 *            The file type (e.g. ".pdf")
	 * 
	 * @return The file name build from the arguments.
	 */
	public static String getFilename(String name, String appendix) {
		return name.replace(" ", "_") + appendix;
	}

	/**
	 * Returns a file on SD card. Creates it if not exists.
	 * 
	 * @param folder
	 *            The file's folder, relative to the cache directory.
	 * @param filename
	 *            The file's name.
	 * 
	 * @return The file.
	 * @throws Exception
	 *             If SD-card does not exist
	 */
	public static File getFileOnSD(String folder, String filename) throws Exception {
		// Save the file to/open from SD
		File path = new File(Utils.getCacheDir(folder));

		return new File(path, filename);
	}

	/**
	 * Open a file with given mime-type using the ACTION_VIEW intent.
	 * 
	 * @param file
	 *            File to be opened.
	 * @param context
	 *            Activity calling.
	 * @param mimeType
	 *            The file's mime-type
	 */
	public static void openFile(File file, Activity context, String mimeType) {
		if (file != null) {
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), mimeType);
			context.startActivity(intent);
		} else {
			Toast.makeText(context, context.getString(R.string.error_occurred_while_opening), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Reads a text file and returns the content as a String.
	 * 
	 * @param file
	 *            The text file.
	 * @return The file's content.
	 */
	public static String readFile(File file) {
		StringBuilder contents = new StringBuilder();

		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			BufferedReader input = new BufferedReader(new FileReader(file));
			try {
				String line = null;
				while ((line = input.readLine()) != null) {
					contents.append(line);
					// contents.append(System.getProperty("line.separator"));
				}
			} finally {
				input.close();
			}
		} catch (IOException e) {
			Log.e("EXCEPTION", e.getMessage());
		}

		return contents.toString();
	}

	/**
	 * Gets a document from a URL and returns the source code as text.
	 * 
	 * @param httpClient
	 *            HTTP client used to fetch the document.
	 * @param url
	 *            The documents URL
	 * 
	 * @return The document's source/the requests response
	 */
	public static String sendGetRequest(DefaultHttpClient httpClient, String url) {
		return sendRequest(httpClient, new HttpGet(url));

	}

	/**
	 * Sends a HTTP post request to given URL.
	 * 
	 * @param httpClient
	 *            The HTTP client.
	 * @param url
	 *            The request URL.
	 */
	public static String sendPostRequest(DefaultHttpClient httpClient, String url) {
		return sendRequest(httpClient, new HttpPost(url));
	}

	/**
	 * Sends a HTTP request.
	 * 
	 * @param httpClient
	 *            The corresponding HTTP client.
	 * @param request
	 *            The request to be send.
	 * @return The response as String.
	 */
	public static String sendRequest(DefaultHttpClient httpClient, HttpRequestBase request) {
		HttpResponse response;
		String respContent = "";

		try {
			response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				respContent = EntityUtils.toString(entity);
				// Log.d("RESP", "content: " + respContent);
				entity.consumeContent();
			}
		} catch (Exception /* ClientProtocolException, IOException */e) {
			Log.e("EXCEPTION", e.getMessage());
		}
		return respContent;
	}

	/**
	 * Writes a String to a text file.
	 * 
	 * @param file
	 *            The target file.
	 * 
	 * @param content
	 *            The text content to be written.
	 */
	public static void writeFile(File file, String content) {
		InputStream in;
		in = new ByteArrayInputStream(content.getBytes());

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);

			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = in.read(buffer)) > 0) {
				fos.write(buffer, 0, len1);
			}

			fos.close();
		} catch (Exception e) {
			Log.e("EXCEPTION", e.getMessage());
		}
	}
}
