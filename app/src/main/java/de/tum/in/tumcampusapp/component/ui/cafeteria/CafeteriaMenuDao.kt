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
            "WHERE date > date('now','localtime') AND id = :cafeteriaId AND name = :dishName " +
            "ORDER BY date ASC")
    fun getNextDatesForDish(cafeteriaId: String, dishName: String): Flowable<List<String>>

    /**
     * @param cafeteriaId the cafeteria for which dishes should be fetched (auto generated integer primary key)
     * @param date the date for which dishes should be fetched.
     * @return Any CafeteriaMenu items matching the criterion specified by the parameters
     */
    @Query("SELECT id, slug, date, dishType, name, labels, calendarWeek FROM cafeteriaMenu " +
            "WHERE id = :cafeteriaId AND date = :date " +
            "GROUP BY dishType ORDER BY dishType DESC")
    fun getCafeteriaMenus(cafeteriaId: Int, date: DateTime): List<CafeteriaMenu>

    /**
     * Used for fetching the actual primary key 'id' from the table via the CafeteriaLocation enum and the cafeteriaId embedded in that enum.
     *
     * @param cafeteriaSlug the cafeteria for which dishes should be fetched as valid cafeteria slug e.g.: "mensa-garching"
     * @return the 'id' corresponding to the provided cafeteriaId
     */
    @Query("SELECT id FROM cafeteriaMenu WHERE slug = :cafeteriaSlug")
    fun getIdFrom(cafeteriaSlug: String): Int
}
