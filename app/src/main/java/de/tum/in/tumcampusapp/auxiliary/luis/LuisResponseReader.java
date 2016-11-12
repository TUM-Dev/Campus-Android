package de.tum.in.tumcampusapp.auxiliary.luis;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Created by Jimena Pose and Riccardo Padovani
public class LuisResponseReader {

    public List<Action> readResponse(JSONObject response) {
        List<Action> actions = new ArrayList<>();
        try {
            JSONArray entities = response.getJSONArray("entities");
            String intentName = response.getJSONObject("topScoringIntent").getString("intent");
            switch (LuisIntent.fromIntentName(intentName)) {
                case TRANSPORTATION:
                    return findTransportationActions(entities);
                case PROFESSOR:
                    return findProfessorActions(entities);
                case MENSA:
                    return findMensaActions(entities);
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            actions.add(new Action(ActionType.ERROR_BAD_INPUT));
        } catch (NullPointerException e) {
            e.printStackTrace();
            actions.add(new Action(ActionType.ERROR));
        }
        return actions;
    }

    private List<Action> findProfessorActions(JSONArray entities) throws JSONException {
        List<Action> professorActions = new ArrayList<>();
        String professorName = "";
        for (int i = 0; i < entities.length(); i++) {
            JSONObject entity = entities.getJSONObject(i);
            switch (getEntityType(entity)) {
                case PROFESSOR_INFORMATION:
                    Action infoAction = new Action(ActionType.PROFESSOR_INFORMATION);
                    infoAction.addData(DataType.PROFESSOR_INFORMATION, getEntityInput(entity));
                    professorActions.add(infoAction);
                    break;
                case PROFESSOR_NAME:
                    professorName = getEntityInput(entity);
                    break;
                default:
                    professorActions.add(new Action(ActionType.ERROR_ENTITIES));
            }
        }
        setDataForAllActions(professorActions, DataType.PROFESSOR_NAME, professorName);
        return professorActions;
    }

    private List<Action> findTransportationActions(JSONArray entities) throws JSONException {
        List<Action> transportationActions = new ArrayList<>();
        String transportationType = "all";
        for (int i = 0; i < entities.length(); i++) {
            JSONObject entity = entities.getJSONObject(i);
            switch (getEntityType(entity)) {
                case TRANSPORTATION_LOCATION:
                    Action locationAction = new Action(ActionType.TRANSPORTATION_LOCATION);
                    locationAction.addData(DataType.TRANSPORTATION_LOCATION, getEntityInput(entity));
                    transportationActions.add(locationAction);
                    break;
                case TRANSPORTATION_TIME:
                    Action timeAction = new Action(ActionType.TRANSPORTATION_TIME);
                    timeAction.addData(DataType.TRANSPORTATION_TIME, getEntityInput(entity));
                    transportationActions.add(timeAction);
                    break;
                case TRANSPORTATION_TYPE:
                    transportationType = getTransportDataMatch(getEntityInput(entity));
                    break;
                default:
                    transportationActions.add(new Action(ActionType.ERROR_ENTITIES));
            }
        }
        setDataForAllActions(transportationActions, DataType.TRANSPORTATION_TYPE, transportationType);
        return transportationActions;
    }

    private List<Action> findMensaActions(JSONArray entities) throws JSONException {
        List<Action> mensaActions = new ArrayList<>();
        for (int i = 0; i < entities.length(); i++) {
            JSONObject entity = entities.getJSONObject(i);
            switch (getEntityType(entity)) {
                case MENSA_LOCATION:
                    mensaActions.add(new Action(ActionType.MENSA_LOCATION));
                    break;
                case MENSA_TIME:
                    mensaActions.add(new Action(ActionType.MENSA_TIME));
                    break;
                case MENSA_MENU:
                    mensaActions.add(new Action(ActionType.MENSA_MENU));
                    break;
                default:
                    break;
            }
        }
        return mensaActions;
    }

    private EntityType getEntityType(JSONObject entity) throws JSONException {
        String entityName = entity.getString("type");
        return EntityType.fromEntityName(entityName);
    }

    private String getEntityInput(JSONObject entity) throws JSONException {
        return entity.getString("entity");
    }

    private String getTransportDataMatch(String transportType) {
        return transportType.equals("bus") ? "MVV-Regionalbus" : "U-Bahn";
    }

    private void setDataForAllActions(List<Action> actions, DataType dataName, String data) {
        if (data != null) {
            for (Action action : actions) {
                action.addData(dataName, data);
            }
        }
    }
}
