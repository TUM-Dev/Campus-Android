package de.tum.in.tumcampusapp.managers;

import android.content.Context;

import java.util.List;

import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dao.RecentsDao;
import de.tum.in.tumcampusapp.models.dbEntities.Recent;

/**
 * Transport Manager, handles database stuff, internet connections
 */
public class RecentsManager {
    public static final int STATIONS = 1;
    public static final int ROOMS = 2;
    public static final int PERSONS = 3;

    private final int type;
    private RecentsDao dao;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public RecentsManager(Context context, int type) {
        this.type = type;

        dao = TcaDb.getInstance(context)
                   .recentsDao();
    }

    /**
     * Get all recents from the database
     *
     * @return Database cursor (name, _id)
     */
    public List<Recent> getAllFromDb() {
        return dao.getAll(type);
    }

    /**
     * Replace or Insert a item into the database
     *
     * @param name Recent name
     */
    public void replaceIntoDb(String name) {
        if (name.isEmpty()) {
            return;
        }
        dao.insert(new Recent(name, type));
    }
}