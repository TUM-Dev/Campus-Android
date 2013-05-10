package de.tum.in.tumcampusapp.tumonline;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * This class will handle all action needed to communicate with the TUMCampus
 * backend The difference between this and the TUMOnline-class is the use of
 * another webinterface
 * 
 * @author Thomas Behrens
 * @review Vincenz Doelle, Daniel G. Mayr
 */
public class TUMCampusRequest {

	/** TUMCampus-Token for this app */
	private static String ACCESSTOKEN = "NEEDS TO BE SET AGAIN";

	/** asynchronous task for interactive fetch */
	AsyncTask<Void, Void, String> backgroundTask = null;

	/** http client instance for fetching */
	private final HttpClient client = new DefaultHttpClient();

	/** Name of the called Function (of the interface) */
	private final String method;

	/** parameters map to be added to the url */
	private Map<String, String> parameters;

	/** Progress dialog while fetching information */
	private ProgressDialog progressDialog;

	/** Message to be displayed in progress dialog */
	private String progressDialogMessage = "";

	/** Server address: TUMCampus interface */
	private final String serviceBaseURL = "https://campus.tum.de/tumonlinej/ws/webservice_v1.0/cdm/organization/";

	/**
	 * @param method
	 *            the function name to which we are calling
	 */
	public TUMCampusRequest(String method) {
		this.method = method;
		resetParameters();
	}

	/**
	 * Fetches the result of the HTTPRequest (which can be seen by using
	 * getRequestURL)
	 * 
	 * @return output will be a raw String
	 * @see getRequestURL
	 */
	public String fetch() {
		String url = getRequestURL();
		Log.d("TUMCampusXMLRequest", "fetching URL " + url);

		try {
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			HttpEntity responseEntity = response.getEntity();

			if (responseEntity != null) {
				// do something with the response
				String result = EntityUtils.toString(responseEntity);
				result.trim();
				return result;
			}

		} catch (Exception e) {
			Log.d("FETCH-ERROR", e.toString());
			e.printStackTrace();
			return e.getMessage();
		}
		return null;
	}

	/**
	 * this fetch method will fetch the data from the TUMOnline Request and will
	 * address the listeners onFetch if the fetch succeeded, else the
	 * onFetchError will be called
	 * 
	 * @param context
	 *            the current context (may provide the current activity)
	 * @param listener
	 *            the listener, which takes the result
	 */
	public void fetchInteractive(final Context context, final TUMOnlineRequestFetchListener listener) {
		// start the progress dialog
		progressDialog = ProgressDialog.show(context, "", getProgressDialogMessage());
		progressDialog.setCancelable(true);

		// terminate background task if running
		progressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (backgroundTask != null) {
					backgroundTask.cancel(true);
					listener.onFetchCancelled();
				}

			}
		});

		// fetch information in a background task and show progress dialog in
		// meantime
		backgroundTask = new AsyncTask<Void, Void, String>() {

			/** property to determine if there is an internet connection */
			boolean isOnline;

			@Override
			protected String doInBackground(Void... params) {
				// set parameter on the TUMOnline request an fetch the results
				isOnline = Utils.isConnected(context);
				if (!isOnline) {
					// not online, fetch does not make sense
					return null;
				}
				// we are online, return fetch result
				return fetch();
			}

			@Override
			protected void onPostExecute(String result) {
				// stop dialog first
				progressDialog.dismiss();

				// handle result
				if (isOnline == false) {
					listener.onFetchError(context.getString(R.string.no_internet_connection));
					return;
				}
				if (result == null) {
					listener.onFetchError(context.getString(R.string.empty_result));
					// TODO Check whether to move to string.xml
				} else if (result.contains(TUMOnlineConst.TOKEN_NICHT_BESTAETIGT)) {
					// TODO Token is not valid
					listener.onFetchError(context.getString(R.string.dialog_access_token_invalid));
				}

				listener.onFetch(result);
			}

		};

		backgroundTask.execute();
	}

	/**
	 * Returns a map with all set parameter pairs
	 * 
	 * @return Map<String, String> parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	public String getProgressDialogMessage() {
		return progressDialogMessage;
	}

	/**
	 * This will return the URL to the TUMOnlineRequest with regard to the set
	 * parameters
	 * 
	 * @return a String URL
	 */
	public String getRequestURL() {
		String url = serviceBaseURL + method + "/xml?";
		Iterator<Entry<String, String>> itMapIterator = parameters.entrySet().iterator();
		while (itMapIterator.hasNext()) {
			Entry<String, String> pairs = itMapIterator.next();
			url += pairs.getKey() + "=" + pairs.getValue() + "&";
		}
		return url;
	}

	/** Reset parameters to an empty Map */
	public void resetParameters() {
		parameters = new HashMap<String, String>();
		parameters.put("token", ACCESSTOKEN);
	}

	/**
	 * Sets one parameter name to its given value
	 * 
	 * @param name
	 *            identifier of the parameter
	 * @param value
	 *            value of the parameter
	 */
	public void setParameter(String name, String value) {
		parameters.put(name, value);
	}

	/**
	 * If you want to put a complete Parameter Map into the request, use this
	 * function to merge them with the existing parameter map
	 * 
	 * @param existingMap
	 *            a Map<String,String> which should be set
	 */
	public void setParameters(Map<String, String> existingMap) {
		parameters.putAll(existingMap);
	}

	public void setProgressDialogMessage(String progressDialogMessage) {
		this.progressDialogMessage = progressDialogMessage;
	}
}
