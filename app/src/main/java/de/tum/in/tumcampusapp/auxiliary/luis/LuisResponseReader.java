package de.tum.in.tumcampusapp.auxiliary.luis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Created by Jimena Pose and Riccardo Padovani
public class LuisResponseReader {

    public Action readResponse(JSONObject response) {
        try {
            JSONArray entities = response.getJSONArray("entities");
            String intentName = response.getJSONObject("topScoringIntent").getString("intent");
            switch (LuisIntent.fromIntentName(intentName)) {
                case TRANSPORTATION:
                    return findTransportationAction(entities);
                case PROFESSOR:
                    return findProfessorAction(entities);
                case MENSA:
                    return findMensaAction(entities);
                default:
                    break;
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return Action.ERROR_BAD_INPUT;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return Action.ERROR;
        }
    }

    private Action findProfessorAction(JSONArray entities) throws JSONException {
        for (int i = 0; i < entities.length(); i++) {
            JSONObject entity = entities.getJSONObject(i);
            switch (getEntityType(entity)) {
                case PROFESSOR_INFORMATION:
                    Action professorInformation = Action.TRANSPORTATION_LOCATION;
                    professorInformation.setEntityInput(getEntityInput(entity));
                    return professorInformation;
                case PROFESSOR_NAME:
                    break;
                default:
                    return Action.ERROR_ENTITIES;
            }
        }
        return Action.ERROR_ENTITIES;
    }

    private Action findTransportationAction(JSONArray entities) throws JSONException {
        for (int i = 0; i < entities.length(); i++) {
            JSONObject entity = entities.getJSONObject(i);
            switch (getEntityType(entity)) {
                case TRANSPORTATION_LOCATION:
                    return Action.TRANSPORTATION_LOCATION;
                case TRANSPORTATION_TIME:
                    return Action.TRANSPORTATION_TIME;
                case TRANSPORTATION_TRAINS:
                    break;
                default:
                    return Action.ERROR_ENTITIES;
            }
        }
        return Action.ERROR_ENTITIES;
    }

    private Action findMensaAction(JSONArray entities) throws JSONException {
        for (int i = 0; i < entities.length(); i++) {
            JSONObject entity = entities.getJSONObject(i);
            switch (getEntityType(entity)) {
                case MENSA_LOCATION:
                    return Action.MENSA_LOCATION;
                case MENSA_TIME:
                    return Action.MENSA_TIME;
                case MENSA_MENU:
                    return Action.MENSA_MENU;
                default:
                    break;
            }
        }
        return Action.ERROR_ENTITIES;
    }

    private EntityType getEntityType(JSONObject entity) throws JSONException {
        String entityName = entity.getString("type");
        return EntityType.fromEntityName(entityName);
    }

    private String getEntityInput(JSONObject entity) throws JSONException {
        return entity.getString("entity");
    }
}