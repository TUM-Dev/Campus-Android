package de.tum.in.tumcampus.activities;

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
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
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

import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.TransportManager;

/**
 * Activity to show transport stations and departures
 */
public class TransportationActivity extends ActionBarActivity implements OnItemClickListener, OnItemLongClickListener {

    private RelativeLayout errorLayout;
    private TextView infoTextView;
    private ListView listViewResults;

    private ListView listViewSuggestionsAndSaved = null;
    private RelativeLayout progressLayout;

    private EditText searchTextField;
    private TransportManager transportaionManager;
    private SharedPreferences sharedPrefs;
    private Activity activity;

    private Thread runningSearch = null;

    // public static int Counter=0;

    /**
     * Check if a network connection is available or can be available soon
     *
     * @return true if available
     */
    public boolean connected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public void onClick(View view) {

        this.infoTextView.setVisibility(View.GONE);

        int viewId = view.getId();
        switch (viewId) {
            case R.id.activity_transport_dosearch:
                this.searchForStations(this.searchTextField.getText().toString());
                Utils.hideKeyboard(this, this.searchTextField);
                break;
            case R.id.activity_transport_clear:
                this.searchTextField.setText("");
                break;
            case R.id.activity_transport_domore:
                Cursor stationCursor = this.transportaionManager.getAllFromDb();

                if (!this.transportaionManager.empty()) {
                    SimpleCursorAdapter adapter = (SimpleCursorAdapter) this.listViewSuggestionsAndSaved.getAdapter();
                    adapter.changeCursor(stationCursor);
                } else {
                    this.infoTextView.setText(getString(R.string.no_stored_search_requests));
                    this.infoTextView.setVisibility(View.VISIBLE);
                }
                this.listViewSuggestionsAndSaved.setVisibility(View.VISIBLE);
                this.listViewResults.setVisibility(View.GONE);
                this.errorLayout.setVisibility(View.GONE);
                Utils.hideKeyboard(this, this.searchTextField);
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
        this.setContentView(R.layout.activity_transportation);
        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (this.sharedPrefs.getBoolean("implicitly_id", true)) {
            ImplicitCounter.Counter("mvv_id", this.getApplicationContext());
        }

        // get all stations from db
        this.transportaionManager = new TransportManager(this);
        Cursor stationCursor = this.transportaionManager.getAllFromDb();

        this.searchTextField = (EditText) this.findViewById(R.id.activity_transport_searchfield);
        this.listViewResults = (ListView) this.findViewById(R.id.activity_transport_listview_result);
        this.listViewSuggestionsAndSaved = (ListView) this.findViewById(R.id.activity_transport_listview_suggestionsandsaved);
        this.progressLayout = (RelativeLayout) this.findViewById(R.id.progress_layout);
        this.errorLayout = (RelativeLayout) this.findViewById(R.id.error_layout);
        this.infoTextView = (TextView) this.findViewById(R.id.activity_transport_textview_info);

        @SuppressWarnings("deprecation")
        ListAdapter adapterSuggestionsAndSaved = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, stationCursor,
                stationCursor.getColumnNames(), new int[]{android.R.id.text1});

        this.listViewSuggestionsAndSaved.setAdapter(adapterSuggestionsAndSaved);
        this.listViewSuggestionsAndSaved.setOnItemClickListener(this);
        this.listViewSuggestionsAndSaved.setOnItemLongClickListener(this);

        // initialize empty departure list, disable on click in list
        MatrixCursor departureCursor = new MatrixCursor(new String[]{"name", "desc", "_id"});
        @SuppressWarnings("deprecation")
        SimpleCursorAdapter adapterResults = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, departureCursor,
                departureCursor.getColumnNames(), new int[]{android.R.id.text1, android.R.id.text2}) {

            @Override
            public boolean isEnabled(int position) {
                return false;
            }
        };
        this.listViewResults.setAdapter(adapterResults);

        this.searchTextField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    TransportationActivity.this.searchForStations(TransportationActivity.this.searchTextField.getText().toString());
                    return true;
                }
                return false;
            }
        });
        this.listViewSuggestionsAndSaved.requestFocus();
    }

    @Override
    public void onItemClick(final AdapterView<?> av, View v, int position, long id) {
        // Counter=Counter+1;

        // click on station in list
        Utils.hideKeyboard(this, this.searchTextField);

        Cursor departureCursor = (Cursor) av.getAdapter().getItem(position);
        final String location = departureCursor.getString(departureCursor.getColumnIndex(Const.NAME_COLUMN));

        this.listViewResults.setEnabled(true);
        this.searchTextField.setText(location);

        // save clicked station into db and refresh station list
        // (could be clicked on search result list)
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) av.getAdapter();
        TransportManager tm = new TransportManager(this);
        tm.replaceIntoDb(location);
        adapter.changeCursor(tm.getAllFromDb());

        this.progressLayout.setVisibility(View.VISIBLE);
        this.infoTextView.setVisibility(View.GONE);

        if (!Utils.isConnected(this)) {
            this.progressLayout.setVisibility(View.GONE);
            this.errorLayout.setVisibility(View.VISIBLE);
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
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
                    this.message = R.string.no_departures_found;
                } catch (TimeoutException e) {
                    this.message = R.string.exception_timeout;
                } catch (Exception e) {
                    this.message = R.string.exception_unknown;
                }

                // show departures in list
                final Cursor finalDepartureCursor = departureCursor;
                final int showMessage = this.message;
                TransportationActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleCursorAdapter adapter = (SimpleCursorAdapter) TransportationActivity.this.listViewResults.getAdapter();
                        adapter.changeCursor(finalDepartureCursor);

                        TransportationActivity.this.listViewResults.setVisibility(View.VISIBLE);
                        TransportationActivity.this.progressLayout.setVisibility(View.GONE);
                        TransportationActivity.this.errorLayout.setVisibility(View.GONE);
                        TransportationActivity.this.listViewSuggestionsAndSaved.setVisibility(View.GONE);

                        if (showMessage != 0) {
                            TransportationActivity.this.infoTextView.setText(showMessage);
                            TransportationActivity.this.infoTextView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> av, View v, final int position, long id) {

        // confirm and delete station
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                // delete station from list, refresh station list
                Cursor c = (Cursor) av.getAdapter().getItem(position);
                String location = c.getString(c.getColumnIndex(Const.NAME_COLUMN));

                TransportManager tm = new TransportManager(av.getContext());
                tm.deleteFromDb(location);

                SimpleCursorAdapter adapter = (SimpleCursorAdapter) av.getAdapter();
                adapter.changeCursor(tm.getAllFromDb());
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(this.getString(R.string.really_delete));
        builder.setPositiveButton(this.getString(R.string.yes), listener);
        builder.setNegativeButton(this.getString(R.string.no), null);
        builder.show();
        return false;
    }

    @Override
    public void onBackPressed() {
        if (this.runningSearch != null) {
            this.runningSearch.interrupt();
            this.progressLayout.setVisibility(View.GONE);
            this.runningSearch = null;
        } else {
            this.finish();
        }
    }

    /**
     * Searchs the Webservice for stations
     *
     * @param inputTextRaw
     * @return
     */
    public void searchForStations(String inputTextRaw) {
        final Activity activity = this;
        this.progressLayout.setVisibility(View.VISIBLE);

        this.listViewSuggestionsAndSaved.setEnabled(true);

        // TODO: Workaround, because MVV does not find a station with the full name as a text input
        String inputTextToCheck = inputTextRaw;
        if (inputTextRaw.length() > 2) {
            inputTextToCheck = inputTextRaw.substring(0, inputTextRaw.length() - 1);
        }
        final String inputText = inputTextToCheck;

        if (!Utils.isConnected(this)) {
            this.progressLayout.setVisibility(View.GONE);
            this.errorLayout.setVisibility(View.VISIBLE);
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }
        this.runningSearch = new Thread(new Runnable() {
            int message;

            @Override
            public void run() {
                // Remember my Thread
                Thread me = TransportationActivity.this.runningSearch;

                // Get Information
                TransportManager tm = new TransportManager(activity);
                Cursor stationCursor = null;
                try {
                    stationCursor = tm.getStationsFromExternal(inputText);
                } catch (NoSuchElementException e) {
                    this.message = R.string.no_station_found;
                } catch (TimeoutException e) {
                    this.message = R.string.exception_timeout;
                } catch (Exception e) {
                    this.message = R.string.exception_unknown;
                }

                // Drop results if canceled
                if (TransportationActivity.this.runningSearch == null || TransportationActivity.this.runningSearch != me) {
                    return;
                }

                final Cursor finalStationCursor = stationCursor;
                final int showMessage = this.message;
                // show stations from search result in station list
                // show error message if necessary
                TransportationActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleCursorAdapter adapter = (SimpleCursorAdapter) TransportationActivity.this.listViewSuggestionsAndSaved.getAdapter();
                        adapter.changeCursor(finalStationCursor);

                        TransportationActivity.this.listViewSuggestionsAndSaved.setVisibility(View.VISIBLE);
                        TransportationActivity.this.progressLayout.setVisibility(View.GONE);
                        TransportationActivity.this.errorLayout.setVisibility(View.GONE);
                        TransportationActivity.this.listViewResults.setVisibility(View.GONE);

                        if (showMessage != 0) {
                            TransportationActivity.this.infoTextView.setText(showMessage);
                            TransportationActivity.this.infoTextView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
        this.runningSearch.start();
    }
}