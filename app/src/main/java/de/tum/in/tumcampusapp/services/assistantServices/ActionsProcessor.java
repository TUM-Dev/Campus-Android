package de.tum.in.tumcampusapp.services.assistantServices;

import android.content.Context;

import de.tum.in.tumcampusapp.auxiliary.luis.Action;
import de.tum.in.tumcampusapp.auxiliary.luis.DataType;
import de.tum.in.tumcampusapp.managers.TransportManager;

public class ActionsProcessor {

    public static String processAction(Context context, Action a){
        switch (a.getActionType()){
            case TRANSPORTATION_TIME:
                return processTransTimeAction(context, a);
            default:
                return "Sorry I didn't understand you, could you please ask again?";
        }
    }

    @SuppressWarnings("deprecation")
    private static String processTransTimeAction(Context context, Action a){
        String type = a.getData(DataType.TRANSPORTATION_TYPE);
        String time = a.getData(DataType.TRANSPORTATION_TIME);
        String currentStation = "Garching-Forschungszentrum"; //todo replace with LocationManager
        TransportManager.DepartureDetailed departure = null;
        if(time.equals("next")){
            departure = TransportManager.getNextDeparture(context, currentStation, type);
        }else if(time.equals("last")) {
            departure = TransportManager.getLastDeparture(context, currentStation, type);
        }
        if(departure != null) {
            return ("The " + time + " train will depart in " +
                    printCountdown(departure.countDown) +
                    " , at " + departure.date.getHours() + ":" + departure.date.getMinutes() + ".");
        }
        return "Error parsing MVG data.";
    }

    private static String printCountdown(int c){
        int h = c/60;
        int m = c%60;
        if(h < 1){
            return m + " minutes";
        }else{
            return h + " hours " + m + " minutes";
        }
    }
}
