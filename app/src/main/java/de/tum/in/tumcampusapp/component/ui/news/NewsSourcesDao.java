package de.tum.in.tumcampusapp.component.ui.news;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;

@Dao
public interface NewsSourcesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NewsSources news);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<NewsSources> news);

    @Query("SELECT * FROM news_sources WHERE id < 7 OR id > 13 OR id=:selectedNewspread")
    List<NewsSources> getNewsSources(String selectedNewspread);

    @Query("SELECT * FROM news_sources WHERE id=:id")
    NewsSources getNewsSource(int id);

    @Query("DELETE FROM news_sources")
    void flush();
}
