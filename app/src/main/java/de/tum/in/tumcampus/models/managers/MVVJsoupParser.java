package de.tum.in.tumcampus.models.managers;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.MVVDeparture;
import de.tum.in.tumcampus.models.MVVObject;
import de.tum.in.tumcampus.models.MVVSuggestion;

/**
 * Created by enricogiga on 16/06/2015.
 * helper class to parse contents from MVGLive with JSOUP
 */


public class MVVJsoupParser extends AsyncTask<String, Void, MVVObject> {
    MVVDelegate delegate;
    private final String baseURL1 = "http://www.mvg-live.de/ims/dfiStaticAuswahl.svc?haltestelle=";
    private final String baseURL2 = "&ubahn=checked&bus=checked&tram=checked&sbahn=checked";

    public MVVJsoupParser(MVVDelegate delegate) {
        this.delegate = delegate;

    }

    private String prepareURL(String query) {

        // if query is already a url no need for preparation
        if (query.indexOf("http://www.mvg-live.de") >= 0)
            return query;
        try {
            query = fixDeutschUrl(query);
            Utils.log(" d query " + query);

            query = URLEncoder.encode(query, "UTF-8");
            Utils.log("encoded query " + query);

            /* url encode will put %25
            instead of % (but we already encoded german
            characters by using %)
             */
            query = query.replace("%25", "%");
            return baseURL1 + query + baseURL2;
        } catch (UnsupportedEncodingException e) {
            Utils.log("could not url encode : " + query);
            return null;
        }
    }

    private String fixDeutschUrl(String query) {
        query = query.replace(",", "");
        query = query.replace("ä", "%E4");
        query = query.replace("ö", "%EF");
        query = query.replace("ü", "%FC");
        query = query.replace("ß", "%DF");
        return query;
    }

    @Override
    protected MVVObject doInBackground(String... strings) {
        MVVObject result = new MVVObject();
        StringBuffer buffer = new StringBuffer();

        try {
            String requestedUrl = prepareURL(strings[0]);
            Utils.log("requesting this url: " + requestedUrl);
            Log.d("JSoup", "Connecting to [" + requestedUrl + "]");
            Document doc = Jsoup.connect(requestedUrl).get();
            Log.d("JSoup", "Connected to [" + requestedUrl + "]");

            Elements suggestionList = doc.select("li");
            Elements checkbox = doc.getElementsByAttributeValue("type", "checkbox");
            Elements station = doc.select(".headerStationColumn");

            //in this case there is no station and no suggestion ->it's an error
            if (checkbox.size() > 0) {

                result.setValid(false);
                result.setMessage("No stop found with that or similar name");

            }

            //in this case there are suggestions for the query
            if (suggestionList.size() > 0) {

                result.setSuggestion(true);
                int i = 0;
                for (Element suggestion : suggestionList) {
                    MVVSuggestion sugg = new MVVSuggestion(suggestion.select("a").attr("href"), suggestion.select("a").text());
                    result.getResultList().add(sugg);
                    i++;
                }

            }


            //in this case the station has been found
            if (station.size() > 0) {

                result.setDeparture(true);
                Elements lineList = doc.select(".lineColumn");
                Elements stationList = doc.select(".stationColumn");
                Elements inMinList = doc.select(".inMinColumn");

                // there is just on item in each List
                Elements headerStation = doc.select(".headerStationColumn");
                Elements serverTime = doc.select(".serverTimeColumn");

                result.setDepartureHeader(headerStation.get(0).text());
                result.setDepartureServerTime(serverTime.get(0).text());

                for (int i = 0; i < lineList.size(); i++) {
                    MVVDeparture dep = new MVVDeparture(lineList.get(i).text(), stationList.get(i).text(), Integer.parseInt(inMinList.get(i).text()));
                    result.getResultList().add(dep);
                }

            }


        } catch (Throwable t) {
            Utils.log("Jsoup Error :" + t.getMessage());
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

        if (!s.isValid()) {
            delegate.showError(s);
        } else if (s.isDeparture()) {
            delegate.showDepartureList(s);
        } else if (s.isSuggestion()) {
            delegate.showSuggestionList(s);
        }


    }

}