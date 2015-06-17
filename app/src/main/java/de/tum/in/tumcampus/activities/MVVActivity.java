package de.tum.in.tumcampus.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.models.MVVDeparture;
import de.tum.in.tumcampus.models.MVVObject;
import de.tum.in.tumcampus.models.MVVSuggestion;
import de.tum.in.tumcampus.models.managers.MVVDelegate;
import de.tum.in.tumcampus.models.managers.MVVJsoupParser;

/**
 * Created by enricogiga on 15/06/2015.
 * Activity for searching timetables from the queried station using mvg live
 */
public class MVVActivity extends ActivityForDownloadingExternal implements MVVDelegate {

    String url = "";
    String query = "";
    EditText mvv_query;
    Button btnSearch;



    public MVVActivity() {
        super("MVV", R.layout.activity_mvv_main);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mvv_query = (EditText) findViewById(R.id.mvv_query);
        btnSearch = (Button) findViewById(R.id.mvv_search);
        final MVVDelegate delegate = this;
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                query = mvv_query.getText().toString();
                url= "http://www.mvg-live.de/ims/dfiStaticAuswahl.svc?haltestelle="+query+"&ubahn=checked&bus=checked&tram=checked&sbahn=checked";
                ( new MVVJsoupParser(delegate) ).execute(new String[]{url});
            }
        });
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
            Log.d("Suggestion Name"+j, ""+suggestion.name);
            Log.d("Suggestion Link"+j, suggestion.link);
        }
    }

    @Override
    public void showDepartureList(MVVObject dep) {
        for(int j = 0; j<dep.getResultList().size();j++){
            MVVDeparture departure =(MVVDeparture) dep.getResultList().get(j);
            Log.d("Departure Time"+j, ""+departure.min);
            Log.d("Departure Direction"+j, departure.direction);
            Log.d("Departure line"+j, departure.line);
        }

    }

    @Override
    public void showError(MVVObject object) {

        Toast.makeText(this,object.getMessage(),Toast.LENGTH_LONG).show();

    }
}


