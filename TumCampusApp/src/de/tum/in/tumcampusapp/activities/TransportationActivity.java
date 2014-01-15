package de.tum.in.tumcampusapp.activities;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.TransportManager;

/**
 * Activity to show transport stations and departures
 */
public class TransportationActivity extends Activity implements
		OnItemClickListener, OnItemLongClickListener {

	private RelativeLayout errorLayout;
	private TextView infoTextView;
	private ListView listViewResults;

	private ListView listViewSuggestionsAndSaved = null;
	private RelativeLayout progressLayout;

	private EditText searchTextField;
	private TransportManager transportaionManager;
	public static int Counter=0;

	/**
	 * Check if a network connection is available or can be available soon
	 * 
	 * @return true if available
	 */
	public boolean connected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	public void onClick(View view) {
		
		infoTextView.setVisibility(View.GONE);

		int viewId = view.getId();
		switch (viewId) {
		case R.id.activity_transport_dosearch:
			searchForStations(searchTextField.getText().toString());
			Utils.hideKeyboard(this, searchTextField);
			break;
		case R.id.activity_transport_clear:
			searchTextField.setText("");
			break;
		case R.id.activity_transport_domore:
			Cursor stationCursor = transportaionManager.getAllFromDb();

			if (!transportaionManager.empty()) {
				SimpleCursorAdapter adapter = (SimpleCursorAdapter) listViewSuggestionsAndSaved
						.getAdapter();
				adapter.changeCursor(stationCursor);
			} else {
				infoTextView.setText("No stored search requests");
				infoTextView.setVisibility(View.VISIBLE);
			}
			listViewSuggestionsAndSaved.setVisibility(View.VISIBLE);
			listViewResults.setVisibility(View.GONE);
			errorLayout.setVisibility(View.GONE);
			Utils.hideKeyboard(this, searchTextField);
			break;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transportation);

		// get all stations from db
		transportaionManager = new TransportManager(this);
		Cursor stationCursor = transportaionManager.getAllFromDb();

		searchTextField = (EditText) findViewById(R.id.activity_transport_searchfield);
		listViewResults = (ListView) findViewById(R.id.activity_transport_listview_result);
		listViewSuggestionsAndSaved = (ListView) findViewById(R.id.activity_transport_listview_suggestionsandsaved);
		progressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
		errorLayout = (RelativeLayout) findViewById(R.id.error_layout);
		infoTextView = (TextView) findViewById(R.id.activity_transport_textview_info);

		@SuppressWarnings("deprecation")
		ListAdapter adapterSuggestionsAndSaved = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, stationCursor,
				stationCursor.getColumnNames(),
				new int[] { android.R.id.text1 });

		listViewSuggestionsAndSaved.setAdapter(adapterSuggestionsAndSaved);
		listViewSuggestionsAndSaved.setOnItemClickListener(this);
		listViewSuggestionsAndSaved.setOnItemLongClickListener(this);

		// initialize empty departure list, disable on click in list
		MatrixCursor departureCursor = new MatrixCursor(new String[] { "name",
				"desc", "_id" });
		@SuppressWarnings("deprecation")
		SimpleCursorAdapter adapterResults = new SimpleCursorAdapter(this,
				android.R.layout.two_line_list_item, departureCursor,
				departureCursor.getColumnNames(), new int[] {
						android.R.id.text1, android.R.id.text2 }) {

			@Override
			public boolean isEnabled(int position) {
				return false;
			}
		};
		listViewResults.setAdapter(adapterResults);

		searchTextField
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							searchForStations(searchTextField.getText()
									.toString());
							return true;
						}
						return false;
					}
				});
		listViewSuggestionsAndSaved.requestFocus();
	}

	@Override
	public void onItemClick(final AdapterView<?> av, View v, int position,
			long id) {
		// click on station in list
		
		/*Counter=Counter+1;
		Toast.makeText(this, String.valueOf(Counter),
				Toast.LENGTH_LONG).show();*/
		/*SharedPreferences prefs =  
			    getSharedPreferences("MyPreferences", MODE_PRIVATE); 
		int[] list = new int[10];
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < list.length; i++) {
		    str.append(list[i]).append(",");
		}
		prefs.edit().putString("string", str.toString());*/

		//String savedString = prefs.getString("Counter", "");
		//StringTokenizer st = new StringTokenizer(savedString, ",");
		//int[] savedList = new int[10];
		//for (int i = 0; i < 10; i++) {
		 //   savedList[i] = Integer.parseInt(st.nextToken());
		//}
		//}
		//int x=savedList[1];
		//Toast.makeText(this, String.valueOf(x),
			//	Toast.LENGTH_LONG).show();
		
		Utils.hideKeyboard(this, searchTextField);

		Cursor departureCursor = (Cursor) av.getAdapter().getItem(position);
		final String location = departureCursor.getString(departureCursor
				.getColumnIndex(Const.NAME_COLUMN));

		listViewResults.setEnabled(true);
		searchTextField.setText(location);

		// save clicked station into db and refresh station list
		// (could be clicked on search result list)
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) av.getAdapter();
		TransportManager tm = new TransportManager(this);
		tm.replaceIntoDb(location);
		adapter.changeCursor(tm.getAllFromDb());

		progressLayout.setVisibility(View.VISIBLE);
		infoTextView.setVisibility(View.GONE);

		if (!Utils.isConnected(this)) {
			progressLayout.setVisibility(View.GONE);
			errorLayout.setVisibility(View.VISIBLE);
			Toast.makeText(this, R.string.no_internet_connection,
					Toast.LENGTH_SHORT).show();
			return;
		}

		new Thread(new Runnable() {
			int message;

			@Override
			public void run() {
				// get departures from website
				TransportManager tm = new TransportManager(av.getContext());
				Cursor departureCursor = null;
				try {
					departureCursor = tm.getDeparturesFromExternal(location);
				} catch (NoSuchElementException e) {
					message = R.string.no_departures_found;
				} catch (TimeoutException e) {
					message = R.string.exception_timeout;
				} catch (Exception e) {
					message = R.string.exception_unknown;
				}

				// show departures in list
				final Cursor finalDepartureCursor = departureCursor;
				final int showMessage = message;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						SimpleCursorAdapter adapter = (SimpleCursorAdapter) listViewResults
								.getAdapter();
						adapter.changeCursor(finalDepartureCursor);

						listViewResults.setVisibility(View.VISIBLE);
						progressLayout.setVisibility(View.GONE);
						errorLayout.setVisibility(View.GONE);
						listViewSuggestionsAndSaved.setVisibility(View.GONE);

						if (showMessage != 0) {
							infoTextView.setText(showMessage);
							infoTextView.setVisibility(View.VISIBLE);
						}
					}
				});
			}
		}).start();
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> av, View v,
			final int position, long id) {

		// confirm and delete station
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {

				// delete station from list, refresh station list
				Cursor c = (Cursor) av.getAdapter().getItem(position);
				String location = c.getString(c
						.getColumnIndex(Const.NAME_COLUMN));

				TransportManager tm = new TransportManager(av.getContext());
				tm.deleteFromDb(location);

				SimpleCursorAdapter adapter = (SimpleCursorAdapter) av
						.getAdapter();
				adapter.changeCursor(tm.getAllFromDb());
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.really_delete));
		builder.setPositiveButton(getString(R.string.yes), listener);
		builder.setNegativeButton(getString(R.string.no), null);
		builder.show();
		return false;
	}

	/**
	 * Searchs the Webservice for stations
	 * 
	 * @param inputText
	 * @return
	 */
	public void searchForStations(String inputTextRaw) {
		final Activity activity = this;
		progressLayout.setVisibility(View.VISIBLE);

		listViewSuggestionsAndSaved.setEnabled(true);

		// TODO: Workaround, because MVV does not find a station with the full
		// name as a text input
		String inputTextToCheck = inputTextRaw;
		if (inputTextRaw.length() > 2) {
			inputTextToCheck = inputTextRaw.substring(0,
					inputTextRaw.length() - 1);
		}
		final String inputText = inputTextToCheck;

		if (!Utils.isConnected(this)) {
			progressLayout.setVisibility(View.GONE);
			errorLayout.setVisibility(View.VISIBLE);
			Toast.makeText(this, R.string.no_internet_connection,
					Toast.LENGTH_SHORT).show();
			return;
		}

		new Thread(new Runnable() {
			int message;

			@Override
			public void run() {
				TransportManager tm = new TransportManager(activity);
				Cursor stationCursor = null;
				try {
					stationCursor = tm.getStationsFromExternal(inputText);
				} catch (NoSuchElementException e) {
					message = R.string.no_station_found;
				} catch (TimeoutException e) {
					message = R.string.exception_timeout;
				} catch (Exception e) {
					message = R.string.exception_unknown;
				}

				final Cursor finalStationCursor = stationCursor;
				final int showMessage = message;
				// show stations from search result in station list
				// show error message if necessary
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						SimpleCursorAdapter adapter = (SimpleCursorAdapter) listViewSuggestionsAndSaved
								.getAdapter();
						adapter.changeCursor(finalStationCursor);

						listViewSuggestionsAndSaved.setVisibility(View.VISIBLE);
						progressLayout.setVisibility(View.GONE);
						errorLayout.setVisibility(View.GONE);
						listViewResults.setVisibility(View.GONE);

						if (showMessage != 0) {
							infoTextView.setText(showMessage);
							infoTextView.setVisibility(View.VISIBLE);
						}
					}
				});
			}
		}).start();
	}
}