package de.tum.in.tumcampusapp.services.assistantServices;

import android.content.Context;
import android.hardware.camera2.params.StreamConfigurationMap;

import de.tum.in.tumcampusapp.auxiliary.luis.Action;
import de.tum.in.tumcampusapp.auxiliary.luis.DataType;
import de.tum.in.tumcampusapp.managers.TransportManager;
import de.tum.in.tumcampusapp.managers.LocationManager;

public class ActionsProcessor {

    public static String processAction(Context context, Action a){
        switch (a.getActionType()){
            case TRANSPORTATION_TIME:
                return processTransTimeAction(context, a);
            case TRANSPORTATION_LOCATION:
                return processTransLocationAction(context, a);
            default:
                return "Didn't catch that, please repeat";
        }
    }

    @SuppressWarnings("deprecation")
    private static String processTransTimeAction(Context context, Action a){
        String type = a.getData(DataType.TRANSPORTATION_TYPE);
        String printType = type.equals("MVV-Regionalbus") ? "Bus" : type ;
        String time = a.getData(DataType.TRANSPORTATION_TIME);
        LocationManager locMan = new LocationManager(context);
        String currentStation = locMan.getStationForAssistent();
        TransportManager.DepartureDetailed departure = null;
        if(time.equals("next")){
            departure = TransportManager.getNextDeparture(context, currentStation, type);
        }else if(time.equals("last")) {
            departure = TransportManager.getLastDeparture(context, currentStation, type);
        }
        if(departure != null) {
            return ("The " + time + " " + printType + " will depart in " +
                    printCountdown(departure.countDown) +
                    " , at " + String.format("%02d", departure.date.getHours()) +
                    ":" + String.format("%02d", departure.date.getMinutes()) + ".");
        }
        return "Error parsing MVG data.";
    }

    private static String printCountdown(int c){
        int h = c/60;
        int m = c%60;
        if(h < 1){
            return m + " minutes";
        }else{
            return String.format("%02d", h) + " hours " + String.format("%02d", m) + " minutes";
        }
    }

    private static String processTransLocationAction(Context context, Action a){
        String type = a.getData(DataType.TRANSPORTATION_TYPE);
        String printType = type.equals("MVV-Regionalbus") ? "bus" : type ;
        LocationManager locMan = new LocationManager(context);
        String location = locMan.getStationForAssistent();
        if(location != null) {
            return "The nearest " + printType + " station is " + location + ".";
        }
        return "Error parsing MVG data.";
    }
}
