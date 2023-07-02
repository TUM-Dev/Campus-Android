package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import io.reactivex.Flowable
import org.joda.time.DateTime

@Dao
interface CafeteriaMenuDao {

    @get:Query("SELECT DISTINCT date FROM cafeteriaMenu WHERE date >= date('now','localtime') ORDER BY date")
    val allDates: List<DateTime>

    @Query("DELETE FROM cafeteriaMenu")
    fun removeCache()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cafeteriaMenus: List<CafeteriaMenu>)

    @Query("SELECT strftime('%d-%m-%Y', date) FROM cafeteriaMenu " +
            "WHERE date > date('now','localtime') AND cafeteriaId = :cafeteriaId AND name = :dishName " +
            "ORDER BY date ASC")
    fun getNextDatesForDish(cafeteriaId: Int, dishName: String): Flowable<List<String>>

    /**
     * @param cafeteriaId the cafeteria for which dishes should be fetched (auto generated integer primary key)
     * @param date the date for which dishes should be fetched.
     * @return Any CafeteriaMenu items matching the criterion specified by the parameters
     */
    @Query("SELECT menuId, cafeteriaId, slug, date, dishType, name, labels, calendarWeek, dishPrices FROM cafeteriaMenu " +
            "WHERE cafeteriaId = :cafeteriaId AND strftime('%d-%m-%Y', date) = strftime('%d-%m-%Y', :date)")
    fun getCafeteriaMenus(cafeteriaId: Int, date: DateTime): List<CafeteriaMenu>

    /**
     * @param cafeteriaId the cafeteria for which dishes should be fetched (auto generated integer primary key)
     * @param date the date for which dishes should be fetched.
     * @return The number of menus available in the local database for the provided params
     */
    @Query("SELECT COUNT(*) AS menus FROM CafeteriaMenu " +
    "WHERE cafeteriaId = :cafeteriaId AND strftime('%d-%m-%Y', date) = strftime('%d-%m-%Y', :date)")
    fun hasMenusFor(cafeteriaId: Int, date: DateTime): Int
}
