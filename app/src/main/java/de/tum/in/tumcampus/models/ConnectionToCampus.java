package de.tum.in.tumcampus.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class ConnectionToCampus implements Serializable {
    private static final long serialVersionUID = -8083639170468397879L;

    private static final String FOOTWAY = "FOOTWAY";

    public static final String JSON_ARRIVAL = "arrival";
    public static final String JSON_CONNECTION_PART_LIST = "connectionPartList";
    public static final String JSON_CONNECTION_PART_TYPE = "connectionPartType";
    public static final String JSON_DEPARTURE = "departure";
    public static final String JSON_DESTINATION = "destination";
    public static final String JSON_FROM = "from";
    public static final String JSON_ID = "id";
    public static final String JSON_LABEL = "label";
    public static final String JSON_LATITUDE = "latitude";
    public static final String JSON_LONGITUDE = "longitude";
    public static final String JSON_PRODUCT = "product";
    public static final String JSON_TO = "to";

    public enum TrainType {
        SBAHN, UBAHN, BUS, TRAM, FOOT;

        public static TrainType toTrainType(String s) {
            switch (s.toLowerCase()) {
                case "s":
                    return SBAHN;

                case "u":
                    return UBAHN;

                case "t":
                    return TRAM;

                case "b":
                    return BUS;

                default:
                    return FOOT;
            }
        }
    }

    private long departure;
    private long arrival;
    private int fromStation;
    private Geo toPosition;

    private TrainType firstTrainType;

    private String firstTrainLabel;
    private String firstTrainDst;
    public ConnectionToCampus(JSONObject routesObject, long desiredArrival) throws JSONException {
        if (routesObject == null) throw new IllegalArgumentException("Route mustn't be null");

        JSONArray routes = routesObject.getJSONArray("connectionList");
        JSONObject route = null;

        // get latest route to campus that arrives before desired time
        for (int i = 0; i < routes.length(); i++) {
            if (routes.getJSONObject(i).getLong("arrival") > desiredArrival) {
                if (i == 0) {
                    route = routes.getJSONObject(i);
                } else {
                    route = routes.getJSONObject(i - 1);
                }
                break;
            }
        }

        // choose latest route if all connections arrive before desired time
        if (route == null) {
            route = routes.getJSONObject(routes.length() - 1);
        }

        departure = route.getLong(JSON_DEPARTURE);
        arrival = route.getLong(JSON_ARRIVAL);

        fromStation = route.getJSONObject(JSON_FROM).getInt(JSON_ID);
        toPosition = new Geo(route.getJSONObject(JSON_TO).getDouble(JSON_LATITUDE), route.getJSONObject(JSON_TO).getDouble(JSON_LONGITUDE));

        // get info about first part of the route
        JSONObject firstConnectionPart = route.getJSONArray(JSON_CONNECTION_PART_LIST).getJSONObject(0);
        if (firstConnectionPart.get(JSON_CONNECTION_PART_TYPE).equals(FOOTWAY)) {
            firstTrainType = TrainType.FOOT;
            firstTrainLabel = "";
        }
        else {
            firstTrainType = TrainType.toTrainType(firstConnectionPart.getString(JSON_PRODUCT));
            firstTrainLabel = firstConnectionPart.getString(JSON_LABEL);
        }
        firstTrainDst = firstConnectionPart.getString(JSON_DESTINATION);
    }

    public long getArrival() {
        return arrival;
    }

    public long getDeparture() {
        return departure;
    }

    public String getFirstTrainDst() {
        return firstTrainDst;
    }

    public String getFirstTrainLabel() {
        return firstTrainLabel;
    }

    public TrainType getFirstTrainType() {
        return firstTrainType;
    }

    public int getFromStation() {
        return fromStation;
    }

    public Geo getToPosition() {
        return toPosition;
    }
}
