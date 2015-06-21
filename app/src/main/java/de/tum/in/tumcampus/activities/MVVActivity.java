package de.tum.in.tumcampus.activities;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
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
import de.tum.in.tumcampus.adapters.MvvAdapter;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.MVVDeparture;
import de.tum.in.tumcampus.models.MVVObject;
import de.tum.in.tumcampus.models.MVVSuggestion;
import de.tum.in.tumcampus.models.managers.MVVDelegate;
import de.tum.in.tumcampus.models.managers.MVVJsoupParser;

/**
 * Created by enricogiga on 15/06/2015.
 * Activity for searching timetables from the queried station using mvg live
 */
public class MVVActivity extends ActivityForDownloadingExternal implements MVVDelegate, AdapterView.OnItemClickListener {

    private EditText mvv_query;
    private Button btnSearch;
    private ListView departurelist;
    private MvvAdapter dataAdapter;
    private TextView listHeader;
    private final String baseURL1 = "http://www.mvg-live.de/ims/dfiStaticAuswahl.svc?haltestelle=";
    private final String baseURL2 = "&ubahn=checked&bus=checked&tram=checked&sbahn=checked";

    public MVVActivity() {
        super("MVV", R.layout.activity_mvv_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseSetup();

        final MVVDelegate delegate = this;
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String query = mvv_query.getText().toString();
                String url= buildQueryUrl(query);
                ( new MVVJsoupParser(delegate) ).execute(new String[]{url});
            }
        });
    }

    private String buildQueryUrl(String query){
        return baseURL1 + query + baseURL2;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }


    @Override
    public void showSuggestionList(MVVObject sug) {
        Log.d("SuggestionList","gut");
        for(int j = 0; j<sug.getResultList().size();j++){
            MVVSuggestion suggestion =(MVVSuggestion) sug.getResultList().get(j);
            Log.d("Suggestion Name"+j, ""+suggestion.getName());
            Log.d("Suggestion Link"+j, suggestion.getLink());
        }

        try {
            Utils.log(getString(R.string.mvv_suggestion_hint));
            listHeader.setText(R.string.mvv_suggestion_hint);
            dataAdapter = new MvvAdapter(sug, this, this);
            departurelist.setAdapter(dataAdapter);
        }catch (Exception e){
            Utils.log(e);
            Utils.showToast(this,"Sorry something went wrong");
        }
    }

    @Override
    public void showDepartureList(MVVObject dep) {
        for(int j = 0; j<dep.getResultList().size();j++){
            MVVDeparture departure =(MVVDeparture) dep.getResultList().get(j);
            Log.d("Departure Time"+j, ""+departure.getMin());
            Log.d("Departure Direction"+j, departure.getDirection());
            Log.d("Departure line"+j, departure.getLine());
        }
        try {
            String stationHeader = dep.getDepartureHeader().trim() + " " + dep.getDepartureServerTime() +" Uhr";
            listHeader.setText(stationHeader);
            listHeader.setTypeface(null, Typeface.BOLD);

            dataAdapter = new MvvAdapter(dep, this, this);
            departurelist.setAdapter(dataAdapter);
        }catch (Exception e){
            Utils.log(e);
            Utils.showToast(this,"Sorry something went wrong");
        }
    }

    @Override
    public void showError(MVVObject object) {

        Toast.makeText(this,object.getMessage(),Toast.LENGTH_LONG).show();

    }


    public void baseSetup(){
        try{
            mvv_query = (EditText) findViewById(R.id.mvv_query);
            btnSearch = (Button) findViewById(R.id.mvv_search);
            listHeader = (TextView)findViewById(R.id.mvv_list_header);
            departurelist = (ListView)findViewById(R.id.mvv_details);
            departurelist.setOnItemClickListener(this);
        }catch(Exception e){
            Utils.log("something went wrong!");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Utils.log("MVV ITEM CLICKED");
        MVVObject object = (MVVObject)parent.getItemAtPosition(position);
        if (object.isSuggestion()){
            MVVSuggestion suggestion = (MVVSuggestion) object;
            String suggestion_url = suggestion.getLink();
            Utils.log("suggestion link is " + suggestion_url);
            requestData(suggestion_url);
        }
    }

    private void requestData(String url){
        ( new MVVJsoupParser(this) ).execute(new String[]{url});
    }
}


