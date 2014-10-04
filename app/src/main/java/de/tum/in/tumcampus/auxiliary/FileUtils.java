package de.tum.in.tumcampus.auxiliary;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Utility functions to ease the work with files and file contents.
 */
public class FileUtils {


	/**
	 * Gets a document from a URL and returns the source code as text.
	 * 
	 * @param httpClient HTTP client used to fetch the document.
	 * @param url The documents URL
	 * 
	 * @return The document's source/the requests response
	 */
	public static String sendGetRequest(DefaultHttpClient httpClient, String url) {
		return sendRequest(httpClient, new HttpGet(url));
	}

	/**
	 * Sends a HTTP request.
	 * 
	 * @param httpClient The corresponding HTTP client.
	 * @param request The request to be send.
	 * @return The response as String.
	 */
	private static String sendRequest(DefaultHttpClient httpClient,
                                      HttpRequestBase request) {
		HttpResponse response;
		String respContent = "";

		try {
			response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				respContent = EntityUtils.toString(entity);
				entity.consumeContent();
			}
		} catch (Exception e) {
			Utils.log(e, "FileUtils.sendRequest");
		}
		return respContent;
	}

	/**
	 * Writes a String to a text file.
	 * 
	 * @param file The target file.
	 * @param content The text content to be written.
	 */
	public static void writeFile(File file, String content) {
		InputStream in;
		in = new ByteArrayInputStream(content.getBytes());

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);

			byte[] buffer = new byte[1024];
			int len1;
			while ((len1 = in.read(buffer)) > 0) {
				fos.write(buffer, 0, len1);
			}

			fos.close();
		} catch (Exception e) {
			Utils.log(e, "FileUtils.writeFile");
		}
	}

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}
