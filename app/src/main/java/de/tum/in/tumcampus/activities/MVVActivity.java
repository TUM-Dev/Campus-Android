package de.tum.in.tumcampus.activities;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.activities.generic.ActivityForSearching;
import de.tum.in.tumcampus.adapters.MvvAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.MVVStationSuggestionProvider;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.MVVDeparture;
import de.tum.in.tumcampus.models.MVVObject;
import de.tum.in.tumcampus.models.MVVSuggestion;
import de.tum.in.tumcampus.models.managers.MVVDelegate;
import de.tum.in.tumcampus.models.managers.MVVJsoupParser;
import de.tum.in.tumcampus.models.managers.RecentsManager;

/**
 * Created by enricogiga on 15/06/2015.
 * Activity for searching timetables from the queried station using mvg live
 */
public class MVVActivity extends ActivityForSearching implements MVVDelegate, AdapterView.OnItemClickListener {

    private ListView departurelist;
    private MvvAdapter dataAdapter;
    private TextView listHeader;
    private RecentsManager recentsManager;
    private SimpleCursorAdapter adapterStations;

    public MVVActivity() {
        super(R.layout.activity_mvv_main, MVVStationSuggestionProvider.AUTHORITY, 3);
    }

    @Override
    protected void onStartSearch() {
        if (!NetUtils.isConnected(this)) {
            showNoInternetLayout();
            return;
        }
        showLoadingStart();
        getListFromMemory();
    }

    @Override
    protected void onStartSearch(String query) {
        if (!NetUtils.isConnected(this)) {
            showNoInternetLayout();
            return;
        }
        showLoadingStart();
        ( new MVVJsoupParser(this) ).execute(new String[]{query});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseSetup();
    }


    @Override
    public void showSuggestionList(MVVObject sug) {
        try {
            listHeader.setText(R.string.mvv_suggestion_hint);
            dataAdapter = new MvvAdapter(sug, this, this);
            departurelist.setAdapter(dataAdapter);
            showLoadingEnded();
        }catch (Exception e){
            Utils.log(e);
            Utils.showToast(this,"Sorry something went wrong");
        }
    }

    @Override
    public void showDepartureList(MVVObject dep) {
        try {
            recentsManager.replaceIntoDb(dep.getDepartureHeader().trim());
            String stationHeader = dep.getDepartureHeader().trim() + " " + dep.getDepartureServerTime() +" Uhr";
            listHeader.setText(stationHeader);
            listHeader.setTypeface(null, Typeface.BOLD);

            dataAdapter = new MvvAdapter(dep, this, this);
            departurelist.setAdapter(dataAdapter);
            showLoadingEnded();
        }catch (Exception e){
            Utils.log(e);
            Utils.showToast(this,"Sorry something went wrong");
        }
    }

    @Override
    public void showError(MVVObject object) {
        showLoadingEnded();
        Toast.makeText(this,object.getMessage(),Toast.LENGTH_LONG).show();
    }


    public void baseSetup(){
        try{
            listHeader = (TextView)findViewById(R.id.mvv_list_header);
            departurelist = (ListView)findViewById(R.id.mvv_details);
            departurelist.setOnItemClickListener(this);

            // get all stations from db
            recentsManager = new RecentsManager(this, RecentsManager.STATIONS);
            getListFromMemory();

        }catch(Exception e){
            Utils.log("something went wrong!");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item = parent.getItemAtPosition(position);
        if (item instanceof MVVObject) {
            MVVObject object = (MVVObject) parent.getItemAtPosition(position);
            if (object.isSuggestion()) {
                MVVSuggestion suggestion = (MVVSuggestion) object;
                String suggestion_url = suggestion.getLink();
                Utils.log("suggestion link is " + suggestion_url);
                onStartSearch(suggestion_url);
            }
        }else{
            Cursor departureCursor = (Cursor) parent.getAdapter().getItem(position);
            onStartSearch(departureCursor.getString(departureCursor.getColumnIndex(Const.NAME_COLUMN)));
        }
    }


    private void getListFromMemory(){
        listHeader.setText(R.string.mvv_recent);
        listHeader.setTypeface(null, Typeface.BOLD);
        Cursor stationCursor = recentsManager.getAllFromDb();
        adapterStations = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, stationCursor,
                stationCursor.getColumnNames(), new int[]{android.R.id.text1}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        if(adapterStations.getCount()==0) {
            openSearch();
        } else {
            departurelist.setAdapter(adapterStations);
            departurelist.requestFocus();
        }
        showLoadingEnded();
    }
}


