package de.tum.`in`.tumcampusapp.component.ui.barrierfree

import android.os.Bundle
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground
import de.tum.`in`.tumcampusapp.component.ui.barrierfree.model.BarrierFreeContact
import de.tum.`in`.tumcampusapp.utils.Utils
import se.emilsjolander.stickylistheaders.StickyListHeadersListView
import java.io.IOException
import java.util.*

class BarrierFreeContactActivity : ActivityForLoadingInBackground<Void,
        List<BarrierFreeContact>>(R.layout.activity_barrier_free_list_info) {

    lateinit var listView: StickyListHeadersListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listView = findViewById(R.id.activity_barrier_info_list_view)
        startLoading()
    }

    override fun onLoadInBackground(vararg arg: Void): List<BarrierFreeContact> {
        showLoadingStart()
        return try {
            TUMCabeClient.getInstance(this).barrierfreeContactList
        } catch (e: IOException) {
            Utils.log(e)
            ArrayList()
        }
    }

    override fun onLoadFinished(result: List<BarrierFreeContact>?) {
        showLoadingEnded()
        if (result == null || result.isEmpty()) {
            showErrorLayout()
            return
        }
        listView.adapter = BarrierFreeContactAdapter(this, result)
    }
}
