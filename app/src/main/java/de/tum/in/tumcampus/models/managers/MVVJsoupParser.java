package de.tum.in.tumcampus.models.managers;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.tum.in.tumcampus.models.MVVDeparture;
import de.tum.in.tumcampus.models.MVVObject;
import de.tum.in.tumcampus.models.MVVSuggestion;

/**
 * Created by enricogiga on 16/06/2015.
 * helper class to parse contents from MVGLive with JSOUP
 */



public class MVVJsoupParser extends AsyncTask<String, Void, MVVObject> {
    MVVDelegate delegate;

    public MVVJsoupParser(MVVDelegate delegate) {
        this.delegate = delegate;

    }
    @Override
    protected MVVObject doInBackground(String... strings) {
        MVVObject result = new MVVObject();
        StringBuffer buffer = new StringBuffer();

        try {
            Log.d("JSoup", "Connecting to [" + strings[0] + "]");
            Document doc = Jsoup.connect(strings[0]).get();
            Log.d("JSoup", "Connected to [" + strings[0] + "]");

            Elements suggestionList = doc.select("li");
            Elements checkbox= doc.getElementsByAttributeValue("type","checkbox");
            Elements station = doc.select(".headerStationColumn");

            //in this case there is no station and no suggestion ->it's an error
            if (checkbox.size()>0){

                result.setValid(false);
                result.setMessage("No stop found with that or similar name");

            }

            //in this case there are suggestions for the query
            if (suggestionList.size()>0){

                result.setSuggestion(true);
                int i=0;
                for (Element suggestion : suggestionList){
                    MVVSuggestion sugg= new MVVSuggestion(suggestion.select("a").attr("href"),suggestion.select("a").text());
                    result.getResultList().add(sugg);
                    i++;
                }

            }


            //in this case the station has been found
            if (station.size()>0){

                result.setDeparture(true);
                Elements lineList = doc.select(".lineColumn");
                Elements stationList = doc.select(".stationColumn");
                Elements inMinList = doc.select(".inMinColumn");

                for (int i = 0; i<lineList.size();i++){
                    MVVDeparture dep = new MVVDeparture(lineList.get(i).text(),stationList.get(i).text(),Integer.parseInt(inMinList.get(i).text()));
                    result.getResultList().add(dep);
                }

            }


        } catch (Throwable t) {
           result.setValid(false);
           result.setErrorCode("invalid_html_parsing");
           result.setMessage(t.getMessage());
           result.setException("InvalidHTMLParsingException");
        }

        return result;
    }


    @Override
    protected void onPostExecute(MVVObject s) {
        super.onPostExecute(s);

        if (!s.isValid()){
            delegate.showError(s);
        }else if (s.isDeparture()){
            delegate.showDepartureList(s);
        }else if (s.isSuggestion()){
            delegate.showSuggestionList(s);
        }


    }

}