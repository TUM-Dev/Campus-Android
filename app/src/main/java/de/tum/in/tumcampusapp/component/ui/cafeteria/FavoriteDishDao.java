package de.tum.in.tumcampusapp.component.ui.cafeteria;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.FavoriteDish;

@Dao
public interface FavoriteDishDao {
    @Insert
    void insertFavouriteDish(FavoriteDish favoriteDish);

    @Query("SELECT strftime('%d-%m-%Y', date) " +
           "FROM favoriteDish WHERE date > date('now','localtime') AND cafeteriaId=:cafeteriaId AND dishName = :dishName")
    List<String> getFavoriteDishNextDates(int cafeteriaId, String dishName);

    @Query("SELECT * FROM favoriteDish WHERE tag = :tag")
    List<FavoriteDish> checkIfFavoriteDish(String tag);

    @Query("SELECT MAX(id) FROM favoriteDish WHERE cafeteriaId = :cafeteriaId AND dishName = :dishName")
    int getLastInsertedDishId(int cafeteriaId, String dishName);

    @Query("SELECT id FROM favoriteDish WHERE cafeteriaId = :cafeteriaId AND dishName = :dishName")
    List<Integer> getFavoriteDishAllIds(int cafeteriaId, String dishName);

    @Query("DELETE FROM favoriteDish WHERE cafeteriaId = :cafeteriaId AND dishName = :dishName")
    void deleteFavoriteDish(int cafeteriaId, String dishName);

    @Query("SELECT * FROM favoriteDish WHERE date = date('now','localtime')")
    List<FavoriteDish> getFavoriteDishToday();

    @Query("SELECT cafeteriaMenu.date FROM favoriteDish " +
           "INNER JOIN cafeteriaMenu ON cafeteriaMenu.cafeteriaId = favoriteDish.cafeteriaId " +
           "AND favoriteDish.dishName = cafeteriaMenu.name")
    List<String> getFavouriteDishDates();

    @Query("SELECT cafeteriaMenu.* FROM favoriteDish " +
           "INNER JOIN cafeteriaMenu ON cafeteriaMenu.cafeteriaId = favoriteDish.cafeteriaId " +
           "AND favoriteDish.dishName = cafeteriaMenu.name WHERE cafeteriaMenu.date = :dayMonthYear")
    List<CafeteriaMenu> getFavouritedCafeteriaMenuOnDate(String dayMonthYear);


    @Query("DELETE FROM favoriteDish")
    void removeCache();
}
