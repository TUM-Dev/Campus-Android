package de.tum.`in`.tumcampusapp.database.migrations

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration
import android.database.SQLException

/**
 * Make our schema room compliant - so we have to recreate all the tables, just to be sure
 */
class Migration2to3 : Migration(2,3) {
    override fun migrate(database: SupportSQLiteDatabase) {

        //############################## Kino migrations ##############################
        // Create the new table
        database.execSQL("DROP TABLE IF EXISTS  kino_new")
        database.execSQL("CREATE TABLE kino_new (" +
                "id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, year TEXT NOT NULL, runtime TEXT NOT NULL," +
                "genre TEXT NOT NULL, director TEXT NOT NULL, actors TEXT NOT NULL, rating TEXT NOT NULL, " +
                "description TEXT NOT NULL, cover TEXT NOT NULL, trailer TEXT NULL, date TEXT NOT NULL, created TEXT NOT NULL," +
                "link TEXT NOT NULL)")
        // Copy the data
        database.execSQL(
                "INSERT INTO kino_new (id, title, year, runtime, genre, director, actors, rating, description, " +
                        "cover, trailer, date, created, link) " +
                        "SELECT id, title, year, runtime, genre, director, actors, rating, description, cover, trailer, date, created, link FROM kino")
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  kino")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE kino_new RENAME TO kino")


        //############################## ChatNotification migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  chat_message")
        database.execSQL("CREATE TABLE chat_message (" +
                "_id INTEGER NOT NULL PRIMARY KEY," +
                "previous INTEGER NOT NULL, " +
                "room INTEGER NOT NULL, " +
                "value TEXT NULL, " +
                "timestamp TEXT NULL, "+
                "signature TEXT NULL," +
                "member TEXT NULL, " +
                "read INTEGER NOT NULL, " +
                "sending INTEGER NOT NULL, " +
                "msg_id INTEGER NOT NULL" +
                ")")


        //############################## News migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  news_new")
        database.execSQL("CREATE TABLE news_new (" +
                "id TEXT NOT NULL PRIMARY KEY, date TEXT NOT NULL, image TEXT NOT NULL, src TEXT NOT NULL, created TEXT NOT NULL,  " +
                "link TEXT NOT NULL, dismissed INTEGER NOT NULL, title TEXT NOT NULL" +
                ")")
        // Copy the data
        database.execSQL(
                "INSERT INTO news_new (id, date, image, src, created, link, dismissed, title) SELECT id, date, image, src, created, link, dismissed, title FROM news")
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  news")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE news_new RENAME TO news")


        //############################## News_sources migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  news_sources_new")
        database.execSQL("CREATE TABLE news_sources_new (" +
                "id INTEGER NOT NULL PRIMARY KEY, title TEXT NOT NULL, icon TEXT NOT NULL" +
                ")")
        // Copy the data
        database.execSQL(
                "INSERT INTO news_sources_new (id, title, icon) SELECT id, title, icon FROM news_sources")
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  news_sources")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE news_sources_new RENAME TO news_sources")


        //############################## Calendar migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  calendar_new")
        database.execSQL("CREATE TABLE calendar_new (" +
                "nr TEXT NOT NULL PRIMARY KEY, description TEXT NOT NULL, location TEXT NOT NULL, title TEXT NOT NULL, " +
                "dtend TEXT NOT NULL, url TEXT NOT NULL, dtstart TEXT NOT NULL, status TEXT NOT NULL" +
                ")")
        // Copy the data
        database.execSQL(
                "INSERT INTO calendar_new (nr, description, location, title, dtend, url, dtstart, status) SELECT nr, description, location, title, dtend, url, dtstart, status FROM calendar")
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  calendar")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE calendar_new RENAME TO calendar")


        //############################## room_locations migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  room_locations_new")
        database.execSQL("CREATE TABLE room_locations_new (" +
                "title TEXT NOT NULL PRIMARY KEY, longitude TEXT NOT NULL, latitude TEXT NOT NULL" +
                ")")
        // Copy the data
        database.execSQL(
                "INSERT INTO room_locations_new (title, longitude, latitude) SELECT title, longitude, latitude FROM room_locations")
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  room_locations")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE room_locations_new RENAME TO room_locations")


        //############################## widgets migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  widgets_timetable_blacklist_new")
        database.execSQL("CREATE TABLE widgets_timetable_blacklist_new (" +
                "widget_id INTEGER NOT NULL, lecture_title TEXT NOT NULL, PRIMARY KEY (widget_id, lecture_title)" +
                ")")
        // Copy the data
        database.execSQL(
                "INSERT INTO widgets_timetable_blacklist_new (widget_id, lecture_title) SELECT widget_id, lecture_title FROM widgets_timetable_blacklist")
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  widgets_timetable_blacklist")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE widgets_timetable_blacklist_new RENAME TO widgets_timetable_blacklist")


        //############################## wifi migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  wifi_measurement_new")
        database.execSQL("CREATE TABLE wifi_measurement_new (" +
                "date TEXT NOT NULL PRIMARY KEY, bssid TEXT NOT NULL, latitude REAL NOT NULL, ssid TEXT NOT NULL, " +
                "dBm INTEGER NOT NULL, accuracyInMeters REAL NOT NULL, longitude REAL NOT NULL" +
                ")")
        // Copy the data
        try {
            database.execSQL(
                    "INSERT INTO wifi_measurement_new (date, bssid, latitude, ssid, dBm, accuracyInMeters, longitude) SELECT date, bssid, latitude, ssid, dBm, accuracyInMeters, longitude FROM wifi_measurement")
        } catch (ignore: SQLException) {
        }
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  wifi_measurement")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE wifi_measurement_new RENAME TO wifi_measurement")


        //############################## recent migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  Recent_new")
        database.execSQL("CREATE TABLE Recent_new (" +
                "name TEXT NOT NULL PRIMARY KEY, type INTEGER NOT NULL" +
                ")")
        // Copy the data
        try {
            database.execSQL(
                    "INSERT INTO Recent_new (name, type) SELECT name, type FROM Recent")
        } catch (ignore: SQLException) {
        }
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  Recent")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE Recent_new RENAME TO Recent")


        //############################## faculty migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  faculties_new")
        database.execSQL("CREATE TABLE faculties_new (" +
                "faculty TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL" +
                ")")
        // Copy the data
        try {
            database.execSQL("INSERT INTO faculties_new (faculty, name) SELECT faculty, name FROM faculties")
        } catch (ignore: SQLException) {
        }
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  faculties")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE faculties_new RENAME TO faculties")


        //############################## open_questions migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  openQuestions_new")
        database.execSQL("CREATE TABLE openQuestions_new (" +
                "question INTEGER NOT NULL PRIMARY KEY, answerid INTEGER NOT NULL, synced INTEGER NOT NULL, " +
                "answered INTEGER NOT NULL, created TEXT NOT NULL, end TEXT NOT NULL, value TEXT NOT NULL" +
                ")")
        // Copy the data
        try {
            database.execSQL(
                    "INSERT INTO openQuestions_new (question, answerid, synced, answered, created, end, value) SELECT question, answerid, synced, answered, created, end, value FROM openQuestions")
        } catch (ignore: SQLException) {
        }
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  openQuestions")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE openQuestions_new RENAME TO openQuestions")


        //############################## own_questions migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  ownQuestions_new")
        database.execSQL("CREATE TABLE ownQuestions_new (" +
                "question INTEGER NOT NULL PRIMARY KEY,no INTEGER NOT NULL, deleted INTEGER NOT NULL, synced INTEGER NOT NULL, " +
                "targetFac TEXT NOT NULL, created TEXT NOT NULL, yes INTEGER NOT NULL, end TEXT NOT NULL, value TEXT NOT NULL" +
                ")")
        // Copy the data
        try {
            database.execSQL(
                    "INSERT INTO ownQuestions_new (question, no, deleted, synced, targetFac, created, yes, end, value) SELECT question, no, deleted, synced, targetFac, created, yes, end, value FROM ownQuestions")
        } catch (ignore: SQLException) {
        }
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  ownQuestions")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE ownQuestions_new RENAME TO ownQuestions")


        //############################## study_room_groups migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  study_room_groups_new")
        database.execSQL("CREATE TABLE study_room_groups_new (" +
                "id INTEGER NOT NULL PRIMARY KEY, details TEXT NOT NULL, name TEXT NOT NULL" +
                ")")
        // Copy the data
        try {
            database.execSQL("INSERT INTO study_room_groups_new (id, details, name) SELECT id, details, name FROM study_room_groups")
        } catch (ignore: SQLException) {
        }
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  study_room_groups")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE study_room_groups_new RENAME TO study_room_groups")


        //############################## study_room_groups migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  study_rooms_new")
        database.execSQL("CREATE TABLE study_rooms_new (" +
                "id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL, location TEXT NOT NULL, code TEXT NOT NULL, " +
                "group_id INTEGER NOT NULL, occupied_till TEXT NOT NULL" +
                ")")
        // Copy the data
        try {
            database.execSQL("INSERT INTO study_rooms_new (id, name, location, code, group_id, occupied_till) SELECT id, name, location, code, group_id, occupied_till FROM study_rooms")
        } catch (ignore: SQLException) {
        }
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  study_rooms")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE study_rooms_new RENAME TO study_rooms")

        //############################## notification migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  notification_new")
        database.execSQL("CREATE TABLE notification_new (" +
                "notification INTEGER NOT NULL PRIMARY KEY, signature TEXT NOT NULL, created TEXT NOT NULL, description TEXT NOT NULL, " +
                "location TEXT NOT NULL, type INTEGER NOT NULL, title TEXT NOT NULL" +
                ")")
        // Copy the data
        try {
            database.execSQL(
                    "INSERT INTO notification_new (notification, signature, created, description, location, type, title) SELECT notification, signature, created, description, location, type, title FROM notification")
        } catch (ignore: SQLException) {
        }
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  notification")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE notification_new RENAME TO notification")


        //############################## transport_favorites migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  transport_favorites_new")
        database.execSQL("CREATE TABLE transport_favorites_new (" +
                "id INTEGER NOT NULL PRIMARY KEY, symbol TEXT NOT NULL" +
                ")")
        database.execSQL("CREATE UNIQUE INDEX index_transport_favorites_symbol_new on transport_favorites_new (symbol)")
        // Copy the data
        try {
            database.execSQL("INSERT INTO transport_favorites_new (id, symbol) SELECT id, symbol FROM transport_favorites")
        } catch (ignore: SQLException) {
        }
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  transport_favorites")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE transport_favorites_new RENAME TO transport_favorites")


        //############################## widgets_transport migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  widgets_transport_new")
        database.execSQL("CREATE TABLE widgets_transport_new (" +
                "id INTEGER NOT NULL PRIMARY KEY, station TEXT NOT NULL, location INTEGER NOT NULL, " +
                "reload INTEGER NOT NULL, station_id TEXT NOT NULL" +
                ")")
        // Copy the data
        try {
            database.execSQL(
                    "INSERT INTO widgets_transport_new (id, station, location, reload, station_id) SELECT id, station, location, reload, station_id FROM widgets_transport")
        } catch (ignore: SQLException) {
        }
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  widgets_transport")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE widgets_transport_new RENAME TO widgets_transport")


        //############################## chat_room migrations ##############################
        database.execSQL("DROP TABLE IF EXISTS  chat_room_new")
        database.execSQL("CREATE TABLE chat_room_new (" +
                "_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "semester_id TEXT NOT NULL, " +
                "contributor TEXT NOT NULL, " +
                "joined INTEGER NOT NULL, " +
                "members INTEGER NOT NULL, " +
                "semester TEXT NOT NULL, " +
                "room INTEGER NOT NULL, " +
                "PRIMARY KEY(name, _id)" +
                ")")

        // Copy the data
        try {
            database.execSQL(
                    "INSERT INTO chat_room_new (_id, name, semester_id, contributor, joined, members, semester, room) SELECT _id, name, semester_id, contributor, joined, members, semester, room FROM chat_room")
        } catch (ignore: SQLException) {
        }
        // Remove the old table
        database.execSQL("DROP TABLE IF EXISTS  chat_room")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE chat_room_new RENAME TO chat_room")

    }

}