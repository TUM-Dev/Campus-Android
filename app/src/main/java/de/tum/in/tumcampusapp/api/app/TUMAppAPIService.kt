package de.tum.`in`.tumcampusapp.api.app

import de.tum.`in`.tumcampusapp.component.ui.news.model.NewsSources
import retrofit2.Call

public interface TUMAppAPIService {

    fun getNewsSources(): Call<List<NewsSources>>

}