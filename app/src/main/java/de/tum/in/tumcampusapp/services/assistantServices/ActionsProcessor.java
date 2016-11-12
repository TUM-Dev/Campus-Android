package de.tum.in.tumcampusapp.services.assistantServices;

import android.content.Context;
import android.hardware.camera2.params.StreamConfigurationMap;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.auxiliary.luis.Action;
import de.tum.in.tumcampusapp.auxiliary.luis.DataType;
import de.tum.in.tumcampusapp.managers.TransportManager;
import de.tum.in.tumcampusapp.managers.LocationManager;
import de.tum.in.tumcampusapp.models.tumo.Employee;
import de.tum.in.tumcampusapp.models.tumo.Person;
import de.tum.in.tumcampusapp.models.tumo.PersonList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;

public class ActionsProcessor {

    public static String processAction(Context context, Action a){
        switch (a.getActionType()){
            case TRANSPORTATION_TIME:
                return processTransTimeAction(context, a);
            case TRANSPORTATION_LOCATION:
                return processTransLocationAction(context, a);
            case PROFESSOR_INFORMATION:
                return processProfInfoAction(context, a);
            default:
                return "ActionTypeError: "+ a.getActionType();
        }
    }

    @SuppressWarnings("deprecation")
    private static String processTransTimeAction(Context context, Action a){
        String type = a.getData(DataType.TRANSPORTATION_TYPE);
        String printType = type.equals("MVV-Regionalbus") ? "bus" : type ;
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

    private static String processProfInfoAction(Context context, Action a) {
        String r = "";
        String query = a.getData(DataType.PROFESSOR_NAME);
        String info = a.getData(DataType.PROFESSOR_INFORMATION);
        List<Person> persons = getPersons(context, query);
        for(Person p: persons){
            String name = p.getSurname() + " " + p.getName();
            r = r + name + "\n";
        }
        return r;
    }

    private static List<Person> getPersons(Context context, String query){
        TUMOnlineRequest<PersonList> pl = new TUMOnlineRequest<PersonList>(TUMOnlineConst.PERSON_SEARCH, context, true);
        pl.setParameter("pSuche", query);
        List<Person> persons = pl.fetch().get().getPersons();
        return persons;
    }
}
