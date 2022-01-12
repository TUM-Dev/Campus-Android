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
            "WHERE date > date('now','localtime') AND cafeteriaId=:cafeteriaId AND name=:dishName " +
            "ORDER BY date ASC")
    fun getNextDatesForDish(cafeteriaId: String, dishName: String): Flowable<List<String>>

    /**
     * The strftime is used for both the stored and passed date to ensure both calls with "YY-MM-DD" and
     * "YY-MM-DD hh:mm:ss" or other valid datetime formats will succeed in finding a dishes if there are any.
     *
     * @param cafeteriaId the cafeteria for which dishes should be fetched as string e.g.: "mensa-garching"
     * @param date the date for which dishes should be fetched.
     * @return Any CafeteriaMenu items matching the criterion specified by the parameters
     */
    @Query("SELECT id, cafeteriaId, date , dishType, name, labels, calendarWeek FROM cafeteriaMenu " +
            "WHERE cafeteriaId = :cafeteriaId AND strftime('%Y-%m-%d', date) = strftime('%Y-%m-%d', :date) " +
            "GROUP BY dishType ORDER BY dishType DESC")
    fun getCafeteriaMenus(cafeteriaId: String, date: DateTime): List<CafeteriaMenu>
}
