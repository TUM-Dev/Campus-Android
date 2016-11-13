package de.tum.in.tumcampusapp.auxiliary.luis;

// Created by Jimena Pose and Riccardo Padovani
enum EntityType {
    // Mensa
    MENSA_LOCATION("mensa::location"),
    MENSA_MENU("mensa::menu"),
    MENSA_TIME("mensa::time"),

    // Transportation
    TRANSPORTATION_LOCATION("transportation::transportation.location"),
    TRANSPORTATION_TYPE("transportation::transportation.type"),
    TRANSPORTATION_TIME("transportation::transportation.time"),

    // Professor
    PROFESSOR_NAME("professor::name"),
    PROFESSOR_INFORMATION("professor::information"),

    //Print
    PRINT_FILE("print::file");


    private String entityName;

    EntityType(String entityName) {
        this.entityName = entityName;
    }

    public String entityName() {
        return this.entityName;
    }

    static EntityType fromEntityName(String entityName) {
        for (EntityType entityType : EntityType.values()) {
            if (entityType.entityName().equals(entityName)) {
                return entityType;
            }
        }
        return null;
    }
}
