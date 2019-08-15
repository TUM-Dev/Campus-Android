package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.FavoriteDish

@Dao
interface FavoriteDishDao {

    @Query("SELECT * FROM favoriteDish WHERE tag = :tag")
    fun checkIfFavoriteDish(tag: String): List<FavoriteDish>

    @Insert
    fun insertFavouriteDish(favoriteDish: FavoriteDish)

    @Query("DELETE FROM favoriteDish WHERE cafeteriaId = :cafeteriaId AND dishName = :dishName")
    fun deleteFavoriteDish(cafeteriaId: Int, dishName: String)

    @Query("SELECT cafeteriaMenu.* FROM favoriteDish " +
            "INNER JOIN cafeteriaMenu ON cafeteriaMenu.cafeteriaId = favoriteDish.cafeteriaId " +
            "AND favoriteDish.dishName = cafeteriaMenu.name WHERE cafeteriaMenu.date = :dayMonthYear")
    fun getFavouritedCafeteriaMenuOnDate(dayMonthYear: String): List<CafeteriaMenu>

    @Query("DELETE FROM favoriteDish")
    fun removeCache()
}
