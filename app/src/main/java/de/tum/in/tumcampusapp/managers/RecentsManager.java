package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;

import java.util.List;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.entities.Recent;
import de.tum.in.tumcampusapp.entities.Recent_;
import de.tum.in.tumcampusapp.entities.TcaBoxes;
import io.objectbox.Box;

/**
 * Transport Manager, handles database stuff, internet connections
 */
public class RecentsManager extends AbstractManager {
    public static final int STATIONS = 1;
    public static final int ROOMS = 2;
    public static final int PERSONS = 3;

    private Box<Recent> recentBox;

    /**
     * Typ to search for
     */
    private final Integer typ;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public RecentsManager(Context context, Integer typ) {
        super(context);
        this.typ = typ;

        recentBox = TcaBoxes.getBoxStore().boxFor(Recent.class);
    }

    /**
     * Get all recents from the database
     *
     * @return Database cursor (name, _id)
     */
    public Cursor getAllFromDb() {
        List<Recent > all = recentBox.query().equal(Recent_.typ, typ).build().find();
        MatrixCursor mc = new MatrixCursor(new String[]{Const.NAME_COLUMN, Const.ID_COLUMN});
        for (Recent e : all) {
            mc.addRow(new String[]{e.getName(), "0"});
        }
        return mc;
    }

    public Recent getOne(String name) {
        return recentBox.query().equal(Recent_.typ, typ).equal(Recent_.name, name).build().findFirst();
    }

    /**
     * Replace or Insert a item into the database
     *
     * @param name Recent name
     */
    public void replaceIntoDb(String name) {
        if (name.isEmpty() || getOne(name) != null) { //We don't want doubles in the database
            return;
        }
        recentBox.put(new Recent(typ, name));
    }
}