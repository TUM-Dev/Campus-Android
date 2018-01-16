package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.models.cafeteria.CafeteriaMenu;

@Dao
public interface CafeteriaMenuDao {

    @Query("DELETE FROM cafeteriaMenu")
    void removeCache();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CafeteriaMenu cafeteriaMenu);

    @Query("SELECT strftime('%d-%m-%Y', date) FROM cafeteriaMenu " +
           "WHERE date > date('now','localtime') AND cafeteriaId=:cafeteriaId AND name=:dishName " +
           "ORDER BY date ASC")
    List<String> getNextDatesForDish(int cafeteriaId, String dishName);

    @Query("SELECT DISTINCT date FROM cafeteriaMenu WHERE date >= date('now','localtime') ORDER BY date")
    List<String> getAllDates();

    @Query("SELECT id, cafeteriaId, date, typeShort, typeLong, 0 AS typeNr, group_concat(name, '\n') AS name FROM cafeteriaMenu " +
           "WHERE cafeteriaId = :cafeteriaId AND date = :date " +
           "GROUP BY typeLong ORDER BY typeShort=\"tg\" DESC, typeShort ASC, typeNr")
    List<CafeteriaMenu> getTypeNameFromDbCard(int cafeteriaId, String date);
}
