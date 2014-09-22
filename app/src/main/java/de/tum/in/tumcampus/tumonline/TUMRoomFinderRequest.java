package de.tum.in.tumcampus.tumonline;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.auxiliary.XMLParser;

/**
 * Base class for communication with TUMRoomFinder
 */
public class TUMRoomFinderRequest {

	// server address
	// public static final String SERVICE_BASE_URL =
	// "http://vmbaumgarten3.informatik.tu-muenchen.de/";

	public static final String KEY_ARCHITECT_NUMBER = "architect_number";
	public static final String KEY_Building = "building";
	public static final String KEY_Campus = "Campus";

	// XML node keys
	public static final String KEY_ID = "Id";
	private static final String KEY_MapId = "mapId";
	public static final String KEY_ROOM = "room";
	public static final String KEY_TITLE = "title";
	private static final String KEY_WEB_CODE = "web_code";
	/** asynchronous task for interactive fetch */
    private AsyncTask<String, Void, ArrayList<HashMap<String, String>>> backgroundTask = null;

    /** method to call */
	private String method = null;
	/** a list/map for the needed parameters */
	private final Map<String, String> parameters;
	private final String SERVICE_BASE_URL = "http://vmbaumgarten3.informatik.tu-muenchen.de/";

    public TUMRoomFinderRequest() {
		/* http client instance for fetching */
        HttpClient client = getThreadSafeClient();
		HttpParams params = client.getParams();
		HttpConnectionParams.setSoTimeout(params, Const.HTTP_TIMEOUT);
		HttpConnectionParams.setConnectionTimeout(params, Const.HTTP_TIMEOUT);
		parameters = new HashMap<String, String>();
		this.method = "search";
	}

	public void cancelRequest(boolean mayInterruptIfRunning) {
		// Cancel background task just if one has been established
		if (backgroundTask != null) {
			backgroundTask.cancel(mayInterruptIfRunning);
		}
	}

	/**
	 * Fetches the result of the HTTPRequest (which can be seen by using
	 * getRequestURL)
	 *
	 * @param searchString Text to search for
	 * @return output will be a raw String
	 * @see TUMRoomFinderRequest#getRequestURL(java.lang.String)
	 */
    ArrayList<HashMap<String, String>> fetch(String searchString) {
		setParameter("s", searchString);
		this.method = "search";

		ArrayList<HashMap<String, String>> roomsList = new ArrayList<HashMap<String, String>>();

        String ROOM_SERVICE_URL = SERVICE_BASE_URL + "roommaps/room/";
        String url = getRequestURL(ROOM_SERVICE_URL);
		Utils.log("fetching URL " + url);

		try {

			XMLParser parser = new XMLParser();
			String xml = parser.getXmlFromUrl(url); // getting XML from URL
			Document doc = parser.getDomElement(xml); // getting DOM element

			NodeList roomList = doc.getElementsByTagName(KEY_ROOM);// building.getChildNodes();

			for (int k = 0; k < roomList.getLength(); k++) {

				Element room = (Element) roomList.item(k);
				HashMap<String, String> roomMap = new HashMap<String, String>();
				// adding each child node to HashMap key =&gt; value
				Element building = (Element) room.getParentNode();
				String buildingId = building.getAttribute(KEY_WEB_CODE);

				Element campus = (Element) building.getParentNode();
				roomMap.put(KEY_Campus + KEY_TITLE,
						parser.getValue(campus, KEY_TITLE));
				roomMap.put(KEY_Building + KEY_TITLE,
						parser.getValue(building, KEY_TITLE));
				roomMap.put(KEY_ROOM + KEY_TITLE,
						parser.getValue(room, KEY_TITLE));
				roomMap.put(KEY_Building + KEY_ID, buildingId);
				roomMap.put(KEY_ARCHITECT_NUMBER,
						parser.getValue(room, KEY_ARCHITECT_NUMBER));

				// adding HashList to ArrayList
				roomsList.add(roomMap);
			}

		} catch (Exception e) {
			Utils.log(e, "FetchError");
		}
		return roomsList;
	}

	String fetchDefaultMapId(String buildingID) {
		setParameter("id", buildingID);

        String ROOM_SERVICE_DEFAULTMAPURL = SERVICE_BASE_URL + "roommaps/building/";
        String url = getRequestURL(ROOM_SERVICE_DEFAULTMAPURL);
		Utils.log("fetching Map URL " + url);

		String result = null;

		try {

			XMLParser parser = new XMLParser();
			String xml = parser.getXmlFromUrl(url); // getting XML from URL
			Document doc = parser.getDomElement(xml); // getting DOM element

			NodeList defaultMapIdList = doc.getElementsByTagName(KEY_MapId);
			Element defaultMapId = (Element) defaultMapIdList.item(0);
			result = parser.getElementValue(defaultMapId);
			if (result.equals(""))
				result = "10";// default room for unknown buildings

		} catch (Exception e) {
			Utils.log(e, "FetchError");
			// return e.getMessage();
		}
		return result;
	}

	public void fetchDefaultMapIdJob(final Context context,
			final TUMRoomFinderRequestFetchListener listener, String mapID) {

		method = "defaultMapId";
		// fetch information in a background task and show progress dialog in
		// meantime
        AsyncTask<String, Void, String> backgroundTaskMap = new AsyncTask<String, Void, String>() {

            /** property to determine if there is an internet connection */
            boolean isOnline;

            @Override
            protected String doInBackground(String... buildingID) {
                // set parameter on the TUMRoomFinder request an fetch the
                // results
                isOnline = Utils.isConnected(context);
                if (!isOnline) {
                    // not online, fetch does not make sense
                    return null;
                }
                // we are online, return fetch result
                return fetchDefaultMapId(buildingID[0]);
            }

            @Override
            protected void onPostExecute(String resultId) {
                // handle result
                if (!isOnline) {
                    listener.onCommonError(context
                            .getString(R.string.no_internet_connection));
                    return;
                }
                if (resultId == null) {
                    listener.onFetchError(context
                            .getString(R.string.empty_result));
                    return;
                }
                if (resultId.equals("10")) {
                    listener.onCommonError(context
                            .getString(R.string.no_map_available));
                    return;
                }
                // If there could not be found any problems return usual on
                // Fetch method
                listener.onFetchDefaultMapId(resultId);
            }

        };

		backgroundTaskMap.execute(mapID);
	}

	/**
	 * this fetch method will fetch the data from the TUMRoomFinder Request and
	 * will address the listeners onFetch if the fetch succeeded, else the
	 * onFetchError will be called
	 * 
	 * @param context the current context (may provide the current activity)
	 * @param listener the listener, which takes the result
	 * @param searchString Text to search for
	 */
	public void fetchSearchInteractive(final Context context,
			final TUMRoomFinderRequestFetchListener listener,
			String searchString) {

		// fetch information in a background task and show progress dialog in
		// meantime
		backgroundTask = new AsyncTask<String, Void, ArrayList<HashMap<String, String>>>() {

			/** property to determine if there is an internet connection */
			boolean isOnline;

			@Override
			protected ArrayList<HashMap<String, String>> doInBackground(
					String... searchString) {
				// set parameter on the TUMRoomFinder request an fetch the
				// results
				isOnline = Utils.isConnected(context);
				if (!isOnline) {
					// not online, fetch does not make sense
					return null;
				}
				// we are online, return fetch result
				return fetch(searchString[0]);
			}

			@Override
			protected void onPostExecute(
					ArrayList<HashMap<String, String>> result) {
				// handle result
				if (!isOnline) {
					listener.onCommonError(context
							.getString(R.string.no_internet_connection));
					return;
				}
				if (result == null) {
					listener.onFetchError(context
							.getString(R.string.empty_result));
					return;
					// TODO Check whether to move to string.xml
				}
				// If there could not be found any problems return usual on
				// Fetch method
				listener.onFetch(result);
			}

		};

		backgroundTask.execute(searchString);
	}

    /**
	 * This will return the URL to the TUMRoomFinderRequest with regard to the
	 * set parameters
	 * 
	 * @return a String URL
	 */
    String getRequestURL(String baseURL) {
		String url = baseURL + method + "?";
        for (Entry<String, String> pairs : parameters.entrySet()) {
            url += pairs.getKey() + "=" + pairs.getValue() + "&";
        }
		return url;
	}

	private DefaultHttpClient getThreadSafeClient() {
		DefaultHttpClient client = new DefaultHttpClient();
		ClientConnectionManager mgr = client.getConnectionManager();
		HttpParams params = client.getParams();

		client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
				mgr.getSchemeRegistry()), params);

		return client;
	}

    /**
	 * Sets one parameter name to its given value
	 * 
	 * @param name identifier of the parameter
	 * @param value value of the parameter
	 */
    void setParameter(String name, String value) {
		parameters.put(name, value);
	}

}
