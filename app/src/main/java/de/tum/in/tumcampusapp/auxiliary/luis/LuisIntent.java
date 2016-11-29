package de.tum.in.tumcampusapp.auxiliary.luis;

// Created by Jimena Pose and Riccardo Padovani
public enum LuisIntent {
    TRANSPORTATION("transportation"),
    PROFESSOR("professor"),
    MENSA("Mensa"),
    PRINT("print"),
    NONE("None");

    private String intentName;

    LuisIntent(String intentName) {
        this.intentName = intentName;
    }

    public String intentName() {
        return this.intentName;
    }

    static LuisIntent fromIntentName(String intentName) {
        for (LuisIntent intent : LuisIntent.values()) {
            if (intent.intentName().equals(intentName)) {
                return intent;
            }
        }
        return null;
    }
}