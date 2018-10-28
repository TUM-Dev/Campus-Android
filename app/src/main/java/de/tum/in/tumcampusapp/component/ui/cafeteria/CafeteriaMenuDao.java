package de.tum.in.tumcampusapp.component.ui.cafeteria;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.joda.time.DateTime;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import io.reactivex.Flowable;

@Dao
public interface CafeteriaMenuDao {

    @Query("DELETE FROM cafeteriaMenu")
    void removeCache();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CafeteriaMenu... cafeteriaMenu);

    @Query("SELECT strftime('%d-%m-%Y', date) FROM cafeteriaMenu " +
           "WHERE date > date('now','localtime') AND cafeteriaId=:cafeteriaId AND name=:dishName " +
           "ORDER BY date ASC")
    Flowable<List<String>> getNextDatesForDish(int cafeteriaId, String dishName);

    @Query("SELECT DISTINCT date FROM cafeteriaMenu WHERE date >= date('now','localtime') ORDER BY date")
    List<DateTime> getAllDates();

    @Query("SELECT id, cafeteriaId, date, typeShort, typeLong, 0 AS typeNr, group_concat(name, '\n') AS name FROM cafeteriaMenu " +
            "WHERE cafeteriaId = :cafeteriaId AND date = :date " +
            "GROUP BY typeLong ORDER BY typeShort=\"tg\" DESC, typeShort ASC, typeNr")
    List<CafeteriaMenu> getTypeNameFromDbCard(int cafeteriaId, DateTime date);
}
