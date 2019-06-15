package de.tum.`in`.tumcampusapp.component.ui.news


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import de.tum.`in`.tumcampusapp.component.ui.news.model.NewsSources

@Dao
interface NewsSourcesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(news: NewsSources)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(news: List<NewsSources>)

    @Query("SELECT * FROM news_sources WHERE id < 7 OR id > 13 OR id=:selectedNewspread")
    fun getNewsSources(selectedNewspread: String): List<NewsSources>

    @Query("SELECT * FROM news_sources WHERE id=:id")
    fun getNewsSource(id: Int): NewsSources

    @Query("DELETE FROM news_sources")
    fun flush()

}
