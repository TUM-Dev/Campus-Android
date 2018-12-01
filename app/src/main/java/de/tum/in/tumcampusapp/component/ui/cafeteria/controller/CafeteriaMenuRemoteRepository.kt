package de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller

import de.tum.`in`.tumcampusapp.api.cafeteria.CafeteriaAPIClient
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl.BYPASS_CACHE
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl.USE_CACHE
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaResponse
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaMenuLocalRepository
import de.tum.`in`.tumcampusapp.utils.ErrorHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class CafeteriaMenuRemoteRepository @Inject constructor(
        private val cafeteriaMenuManager: CafeteriaMenuManager,  // TODO(thellmund) Rename
        private val localRepository: CafeteriaMenuLocalRepository,
        private val apiClient: CafeteriaAPIClient
) {

    fun downloadMenus(force: Boolean) {
        val cacheControl = if (force) BYPASS_CACHE else USE_CACHE
        apiClient.getMenus(cacheControl).enqueue(object : Callback<CafeteriaResponse> {

            override fun onResponse(call: Call<CafeteriaResponse>,
                                    response: Response<CafeteriaResponse>) {
                val cafeteriaResponse = response.body() ?: return
                onDownloadSuccess(cafeteriaResponse)
            }

            override fun onFailure(call: Call<CafeteriaResponse>, t: Throwable) {
                ErrorHelper.logAndIgnore(t)
            }

        })
    }

    private fun onDownloadSuccess(response: CafeteriaResponse) {
        localRepository.clear()

        val allMenus = response.menus + response.sideDishes
        localRepository.store(allMenus)

        cafeteriaMenuManager.scheduleNotificationAlarms()
    }

}
