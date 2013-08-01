package de.tum.in.tumcampusapp.tumonline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.auxiliary.XMLParser;

public class TUMRoomFinderRequest {

	// server address
	// public static final String SERVICE_BASE_URL =
	// "http://vmbaumgarten3.informatik.tu-muenchen.de/";

	private final String SERVICE_BASE_URL = "http://vmbaumgarten3.informatik.tu-muenchen.de/";
	private final String ROOM_SERVICE_URL = SERVICE_BASE_URL + "roommaps/room/";
	private final String ROOM_SERVICE_MAPURL = SERVICE_BASE_URL + "roommaps/building/";

	// XML node keys
	static final String KEY_Campuses = "campuses"; // parent node
	public static final String KEY_Campus = "Campus";
	public static final String KEY_TITLE = "title";
	public static final String KEY_Building = "building";
	public static final String KEY_Room = "room";

	/** asynchronous task for interactive fetch */
	AsyncTask<String,Void,ArrayList<HashMap<String,String>>> backgroundTask = null;
	private AsyncTask<String, Void, Drawable> backgroundTaskMap = null;
	/** http client instance for fetching */
	private HttpClient client;
	/** method to call */
	private String method = null;
	/** a list/map for the needed parameters */
	private Map<String, String> parameters;
	private Context baseContext;
	

	public TUMRoomFinderRequest() {
		client = getThreadSafeClient();
		HttpParams params = client.getParams();
		HttpConnectionParams.setSoTimeout(params, Const.HTTP_TIMEOUT);
		HttpConnectionParams.setConnectionTimeout(params, Const.HTTP_TIMEOUT);
		parameters = new HashMap<String, String>();
		this.method = "search";
	}

	public TUMRoomFinderRequest(String method) {
		this();
		this.method = method;
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
	 * @author Daniel G. Mayr
	 * @param searchString
	 * @return output will be a raw String
	 * @see getRequestURL
	 */
	public ArrayList<HashMap<String, String>> fetch(String searchString) {
		setParameter("s", searchString);

		ArrayList<HashMap<String, String>> roomsList = new ArrayList<HashMap<String, String>>();
		
		String url = getRequestURL(ROOM_SERVICE_URL);
		Log.d("TUMRoomFinderXMLRequest", "fetching URL " + url);

		try {

			XMLParser parser = new XMLParser();
			String xml = parser.getXmlFromUrl(url); // getting XML from URL
			Document doc = parser.getDomElement(xml); // getting DOM element

			

			NodeList roomList = doc.getElementsByTagName(KEY_Room);// building.getChildNodes();

			for (int k = 0; k < roomList.getLength(); k++) {

				Element room = (Element) roomList.item(k);
				HashMap<String, String> roomMap = new HashMap<String, String>();
				// adding each child node to HashMap key =&gt; value
				Element building = (Element) room.getParentNode();
				Element campus = (Element) building.getParentNode();
				roomMap.put(KEY_Campus + KEY_TITLE,
						parser.getValue(campus, KEY_TITLE));
				roomMap.put(KEY_Building + KEY_TITLE,
						parser.getValue(building, KEY_TITLE));
				roomMap.put(KEY_Room + KEY_TITLE,
						parser.getValue(room, KEY_TITLE));

				// adding HashList to ArrayList
				roomsList.add(roomMap);
			}

		} catch (Exception e) {
			Log.d("FETCHerror", e.toString());
			e.printStackTrace();
			//return e.getMessage();
		}
		return roomsList;
	}

	/**
	 * this fetch method will fetch the data from the TUMRoomFinder Request and
	 * will address the listeners onFetch if the fetch succeeded, else the
	 * onFetchError will be called
	 * 
	 * @param context
	 *            the current context (may provide the current activity)
	 * @param listener
	 *            the listener, which takes the result
	 * @param searchString
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
			protected ArrayList<HashMap<String, String>> doInBackground(String... searchString) {
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
			protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
				// handle result
				if (isOnline == false) {
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
	
	
	public void fetchMapJob(final Context context,
			final TUMRoomFinderRequestFetchListener listener,
			String buildingID, String mapID) {

		baseContext=context;
		
		// fetch information in a background task and show progress dialog in
		// meantime
		backgroundTaskMap = new AsyncTask<String, Void, Drawable >() {

			/** property to determine if there is an internet connection */
			boolean isOnline;

			@Override
			protected Drawable doInBackground(String... buildingAndMapID) {
				// set parameter on the TUMRoomFinder request an fetch the
				// results
				isOnline = Utils.isConnected(context);
				if (!isOnline) {
					// not online, fetch does not make sense
					return null;
				}
				// we are online, return fetch result
				return fetchMap(buildingAndMapID[0],buildingAndMapID[1]);
			}

			@Override
			protected void onPostExecute(Drawable result) {
				// handle result
				if (isOnline == false) {
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
				listener.onFetchMap(result);
			}

		};

		backgroundTask.execute(buildingID,mapID);
	}

	
	public Drawable fetchMap(String buildingID, String mapID) {
		setParameter("id", buildingID);
		setParameter("mapid", mapID);

		ArrayList<HashMap<String, String>> roomsList = new ArrayList<HashMap<String, String>>();
		
		String url = getRequestURL(ROOM_SERVICE_MAPURL);
		Log.d("TUMRoomFinderXMLRequest", "fetching Map URL " + url);

		try
        {
        InputStream is = (InputStream) new URL(url).getContent();
        Drawable d = Drawable.createFromStream(is, mapID);
        return d;
        }catch (Exception e) {
        System.out.println("Exc="+e);
        return null;
        }
	}
	
	
	/**
	 * Returns a map with all set parameter pairs
	 * 
	 * @return Map<String, String> parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * This will return the URL to the TUMRoomFinderRequest with regard to the
	 * set parameters
	 * 
	 * @return a String URL
	 */
	public String getRequestURL(String baseURL) {
		String url = baseURL + method + "?";
		Iterator<Entry<String, String>> itMapIterator = parameters.entrySet()
				.iterator();
		while (itMapIterator.hasNext()) {
			Entry<String, String> pairs = itMapIterator.next();
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

}
