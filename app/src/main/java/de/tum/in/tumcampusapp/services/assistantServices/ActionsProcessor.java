package de.tum.in.tumcampusapp.services.assistantServices;

import android.content.Context;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Environment;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.UCentralClient;
import de.tum.in.tumcampusapp.auxiliary.DateUtils;
import de.tum.in.tumcampusapp.auxiliary.FileUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.auxiliary.luis.Action;
import de.tum.in.tumcampusapp.auxiliary.luis.DataType;
import de.tum.in.tumcampusapp.managers.CafeteriaManager;
import de.tum.in.tumcampusapp.managers.TransportManager;
import de.tum.in.tumcampusapp.managers.LocationManager;
import de.tum.in.tumcampusapp.models.cafeteria.Cafeteria;
import de.tum.in.tumcampusapp.models.cafeteria.CafeteriaMenu;
import de.tum.in.tumcampusapp.models.tumo.Employee;
import de.tum.in.tumcampusapp.models.tumo.Person;
import de.tum.in.tumcampusapp.models.tumo.PersonList;
import de.tum.in.tumcampusapp.models.tumo.Room;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static de.tum.in.tumcampusapp.R.string.room;

public class ActionsProcessor {

    public static String processAction(Context context, Action a){
        switch (a.getActionType()){
            case TRANSPORTATION_TIME:
                return processTransTimeAction(context, a);
            case TRANSPORTATION_LOCATION:
                return processTransLocationAction(context, a);
            case PROFESSOR_INFORMATION:
                return processProfInfoAction(context, a);
            case MENSA_MENU:
                return processMensaMenu(context);
            case MENSA_LOCATION:
                return processMensaLocation(context);
            case MENSA_TIME:
                return processMensaTime(context, a);
            case PRINT:
                return processPrintRequest(context, a);
            default:
                return "ActionTypeError: "+ a.getActionType();
        }
    }

    private static String processMensaMenu(Context context) {
        CafeteriaManager cafeteriaManager = new CafeteriaManager(context);
        Map<String, List<CafeteriaMenu>> cafeteria = cafeteriaManager.getBestMatchMensaInfo(context);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, List<CafeteriaMenu>> cafeteriaEntry: cafeteria.entrySet()) {
            builder.append("The menu for " + cafeteriaEntry.getKey() + ":\n");
            for (CafeteriaMenu menu : cafeteriaEntry.getValue()) {
                builder.append(menu.name.replaceAll("\\(.*\\)", "") + "\n");
                // TODO add menu price if we have time
            }
        }
        return builder.toString();
    }

    private static String processMensaLocation(Context context) {
        CafeteriaManager cafeteriaManager = new CafeteriaManager(context);
        Cafeteria cafeteria = cafeteriaManager.getBestMatchMensa(context);
        return "The nearest cafeteria is at " + cafeteria.address
            + " and it's called " + cafeteria.name;
    }

    private static String processMensaTime(Context context, Action action) {
        CafeteriaManager cafeteriaManager = new CafeteriaManager(context);
        Map<String, List<CafeteriaMenu>> cafeteria = cafeteriaManager.getBestMatchMensaInfo(context);
        CafeteriaMenu cafeteriaMenu = cafeteria.values().iterator().next().get(0);
        // TODO fix the opening and closing hours if we have time
        //String date = Utils.getDateTimeString(cafeteriaMenu.date);
        DateTime nowDateTime = new DateTime();
        String actionOutput = "";
        if (action.getData(DataType.MENSA_TIME).contains("open")) {
            if (Integer.valueOf(nowDateTime.getHourOfDay()) < 15) {
                actionOutput = "The cafeteria is open";
            } else {
                actionOutput = "The cafeteria opens tomorrow";
            }
        } else {
            if (Integer.valueOf(nowDateTime.getHourOfDay()) < 15) {
                actionOutput = "The cafeteria closes around 15hs";
            } else {
                actionOutput = "The cafeteria is closed";
            }
        }
        return actionOutput;

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
        List<Employee> employees = getEmployees(context, query);
        if(employees != null) {
            r = "Results:";
            for (Employee e : employees) {
                String title = e.getTitle().isEmpty() ? "" : e.getTitle() + " ";
                String name = e.getTitle() + e.getSurname() + " " + e.getName();
                r = r + "\n\t\t" + name;
                switch (info) {
                    case "email":
                    case "e-mail":
                    case "mail":
                        r = r + "\n\t\t\t\t" + "Email:";
                        r = r + "\t" + e.getEmail();
                        break;
                    case "room":
                        r = r + "\n\t\t\t\t" + "Rooms:";
                        if (e.getRooms() != null) {
                            for (Room room : e.getRooms()) {
                                r = r + "\n\t\t\t\t " + room.getLocation();
                            }
                        } else {
                            r = r + "\t" + "None found.";
                        }
                        break;
                    default:
                        break;
                }
            }
            return r;
        }
        return "Could not complete request. Try refining your search.";
    }

    private static List<Person> getPersons(Context context, String query){
        TUMOnlineRequest<PersonList> pl = new TUMOnlineRequest<PersonList>(TUMOnlineConst.PERSON_SEARCH, context, true);
        pl.setParameter("pSuche", query);
        List<Person> persons = pl.fetch().get().getPersons();
        return persons;
    }

    private static Employee getEmployee(Context context, String id){
        TUMOnlineRequest<Employee> request = new TUMOnlineRequest<>(TUMOnlineConst.PERSON_DETAILS, context, true);
        request.setParameter("pIdentNr", id);
        Employee employee = request.fetch().get();
        return employee;
    }

    private static List<Employee> getEmployees(Context context, String query){
        List<Person> persons = getPersons(context, query);
        if(persons != null && persons.size() < 15) {
            List<Employee> employees = new ArrayList<Employee>();
            for (Person p : persons) {
                employees.add(getEmployee(context, p.getId()));
            }
            return employees;
        }
        return null;
    }

    private static String processPrintRequest(Context context, Action a){
        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/";
        String filename = a.getData(DataType.FILE).replaceAll(" ", "");
        String url = downloadPath + filename;
        Utils.log("URL:" + url);
        File f = new File(url);
        return sendFileToPrinter(context, f);
    }

    private static String sendFileToPrinter(final Context context, final File f) {
        String user = Utils.getInternalSettingString(context, context.getResources().getString(R.string.mi_login), "");
        String pass = Utils.getInternalSettingString(context, context.getResources().getString(R.string.mi_pass), "");

        if (user.equals("") || pass.equals("")) {
            return context.getResources().getString(R.string.error_mi_wrong);
        }

        UCentralClient.getInstance(context).login(user, pass);
        UCentralClient.getInstance(context).printFile(f);
        UCentralClient.getInstance(context).logout();
        return "Print request sent.";
    }
}
