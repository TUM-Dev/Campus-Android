package de.tum.in.tumcampus.widgets.RemoteViewFactories;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.MVVDeparture;
import de.tum.in.tumcampus.models.MVVObject;
import de.tum.in.tumcampus.models.MVVSuggestion;
import de.tum.in.tumcampus.models.managers.MVVDelegate;
import de.tum.in.tumcampus.models.managers.MVVJsoupParser;
import de.tum.in.tumcampus.models.managers.RecentsManager;

public class MVVRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory, MVVDelegate {

    private Context applicationContext;
    private RecentsManager recentsManager;
    private List<MVVObject> mvvList;
    private AppWidgetManager appWidgetManager;
    private Intent intent;

    public MVVRemoteViewFactory(Context applicationContext, Intent intent) {
        this.applicationContext = applicationContext;
        this.intent = intent;
    }

    @Override
    public void onCreate() {
        Utils.log("WidgetMVV On Create called");
        recentsManager = new RecentsManager(applicationContext,RecentsManager.STATIONS);
        callRecentVisitedStation();
        appWidgetManager = AppWidgetManager.getInstance(applicationContext);

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        if (mvvList != null) {
            Utils.log("WidgetMVV size is: " + mvvList.size());
            return mvvList.size();
        }
        else{
            Utils.log("WidgetMVV size is: 0");

            return 0;
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Utils.log("WidgetMVV GetViewAt Pos: "+ position);
        RemoteViews rv = new RemoteViews(applicationContext.getPackageName(), R.layout.mvv_widget_item);
        MVVDeparture currentSearch = (MVVDeparture)mvvList.get(position);
        if(currentSearch !=null) {
            int icon_id = getImageResource(currentSearch);
            String number = currentSearch.getLine();
            String station = currentSearch.getDirection();
            String minutes = String.valueOf(currentSearch.getMin() + " min");

            rv.setImageViewResource(R.id.mvv_icon, icon_id);
            rv.setTextViewText(R.id.line_number, number);
            rv.setTextViewText(R.id.station, station);
            rv.setTextViewText(R.id.minutes, minutes);
            return rv;

        }
        return null;
    }



    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public boolean callRecentVisitedStation(){
        Utils.log("WidgetMVV callRecentVisitedStation");
        boolean isValid = true;
        try {
            Utils.log("WidgetMVV DB recent searches");
            Cursor stationCursor = recentsManager.getAllFromDb();
            Utils.log("WidgetMVV cursor created");
            String mostRecentSearches[] = stationCursor.getColumnNames();
            Utils.log("WidgetMVV Most recent in array");
            String mostRecentSearch = mostRecentSearches[0];
            String test = "Garching";//stationCursor.getString(stationCursor.get;

            Utils.log("WidgetMVV first search");
            Utils.log("WidgetMVV MostRecent is :" + test);

            (new MVVJsoupParser(this)).execute(new String[]{test});


        }catch (Exception e){
            Utils.showToast(applicationContext, "Sorry, something went wrong");
            e.printStackTrace();
            return isValid = false;
        }

        return isValid;
    }

    @Override
    public void showSuggestionList(MVVObject sug) {
        try {
            Utils.log("MVVWidget show sugestion");
            mvvList = sug.getResultList();
            final MVVSuggestion sugestion = (MVVSuggestion) mvvList.get(0);
            final MVVDelegate delegate = this;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ( new MVVJsoupParser(delegate) ).execute(sugestion.getName());
                }
            }).run();

            mvvList = null;
        }catch (Exception e){
            Utils.log(e);
            Utils.showToast(applicationContext,"Sorry something went wrong");
        }
    }

    @Override
    public void showDepartureList(MVVObject dep) {
        try {
            Utils.log("MVVWidget show dep");
            RemoteViews rv = new RemoteViews(applicationContext.getPackageName(), R.layout.mvv_widget_item);
            mvvList = dep.getResultList();
            int widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1);
            Utils.log("MVVWidget widget id is: " + widgetID);
            Utils.log("MVVWidget mvvList size is:" + mvvList.size());
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, R.id.mvv_widget_item);

        }catch (Exception e){
            Utils.log(e);
            Utils.showToast(applicationContext, "Sorry something went wrong");
        }
    }

    @Override
    public void showError(MVVObject object) {
        Utils.log("WidgetMVV error");
        Utils.log("WidgetMVV " + object.getMessage());
        Utils.showToast(applicationContext, object.getMessage());
    }


    /**
     * gets the drawable for related departure in mvv
     * because of efficiency used this method, retrieving resources by string name
     * is not efficient.
     *
     * @param departure MVVDeparture object
     * @return int, id of the icon related to this departure
     */
    public int getImageResource(MVVDeparture departure) {
        MVVDeparture.TransportationType type = departure.getTransportationType();
        Utils.log("MVVWidget type of transportation is " + type);
        switch (type) {
            case UBAHN:
                return R.drawable.mvv_ubahn;
            case SBAHN:
                return R.drawable.mvv_sbahn;
            case BUS_TRAM:
                return R.drawable.mvv_bustram;
        }
        return -1;
    }

}
