package de.tum.in.tumcampusapp.database.dataAccessObjects;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import de.tum.in.tumcampusapp.models.tumcabe.News;

@Dao
public interface NewsDao {

    @Query("DELETE FROM news WHERE date < date('now','-3 month')")
    void cleanUp();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(News news);

    @Query("SELECT n.id AS _id, n.src, n.title, " +
           "n.link, n.image, n.date, n.created, s.icon, s.title AS source, n.dismissed, " +
           "(julianday('now') - julianday(date)) AS diff " +
           "FROM news n, news_sources s " +
           "WHERE n.src=s.id " +
           "AND s.id IN (:ids) " +
           "AND (s.id < 7 OR s.id > 13 OR s.id=:selectedNewspread) " +
           "ORDER BY date DESC")
    Cursor getAll(String ids, String selectedNewspread);
}
