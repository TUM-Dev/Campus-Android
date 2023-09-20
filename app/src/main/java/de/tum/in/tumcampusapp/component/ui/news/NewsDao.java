package de.tum.in.tumcampusapp.component.ui.news;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import de.tum.in.tumcampusapp.component.ui.news.model.News;

@Dao
public interface NewsDao {

    @Query("DELETE FROM news WHERE date < date('now','-3 month')")
    void cleanUpOldNews();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(News news);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<News> news);

    @Query("SELECT * FROM news " +
           "WHERE src IN (:ids) " +
           "AND (src < 7 OR src > 13 OR src=:selectedNewspread) " +
           "ORDER BY date DESC")
    List<News> getAll(Integer[] ids, int selectedNewspread);

    @Query("SELECT * FROM news WHERE date(date) > date() AND (src < 7 OR src > 13 OR src=:selectedNewspread)")
    List<News> getNewer(int selectedNewspread);

    @Nullable
    @Query("SELECT * FROM news ORDER BY id DESC LIMIT 1")
    News getLast();

    @Query("SELECT * FROM news WHERE src IN (:sources) ORDER BY date ASC")
    List<News> getBySources(Integer[] sources);

    @Query("SELECT * FROM news " +
           "WHERE src IN (:sources) " +
           //Show latest news item only
           "AND id IN (SELECT id FROM (SELECT id, src FROM news " +
                                      "WHERE datetime(date) <= datetime('now') " +
                                      "AND src != 2 " +
                                      "ORDER BY datetime(date) ASC) " +
                      "GROUP BY src " +
           //Special treatment for TU Kino, as we want to actually display the upcoming movie
                      "UNION SELECT id FROM (SELECT id, datetime(date) FROM news " +
                                            "WHERE datetime(date) > datetime('now') " +
                                            "AND src = 2 " +
                                            "ORDER BY datetime(date) ASC LIMIT 1))" +
           "ORDER BY date ASC")
    List<News> getBySourcesLatest(Integer[] sources);

    @Query("UPDATE news SET dismissed=:d WHERE id=:id")
    void setDismissed(String d, String id);

    @Query("UPDATE news SET dismissed=0")
    void restoreAllNews();

    @Query("DELETE FROM news")
    void flush();

    @Query("SELECT count(*) FROM events " +
            "WHERE events.news =:newsId ")
    int hasEventAssociated(String newsId);
}
