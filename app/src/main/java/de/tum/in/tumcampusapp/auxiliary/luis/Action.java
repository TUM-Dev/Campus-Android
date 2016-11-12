package de.tum.in.tumcampusapp.auxiliary.luis;

// Created by Jimena Pose and Riccardo Padovani
public enum Action {
    MENSA_MENU,
    MENSA_LOCATION,
    MENSA_TIME,

    TRANSPORTATION_LOCATION,
    TRANSPORTATION_TIME,

    // Errors
    ERROR_BAD_INPUT,
    ERROR_ENTITIES,
    ERROR;

    private String entityInput;

    public void setEntityInput(String entityInput) {
        this.entityInput = entityInput;
    }

    public String getEntityInput() {
        return this.entityInput;
    }
}