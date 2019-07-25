package de.tum.`in`.tumcampusapp.component.ui.barrierfree

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground
import de.tum.`in`.tumcampusapp.component.ui.barrierfree.model.BarrierFreeMoreInfo
import de.tum.`in`.tumcampusapp.utils.Utils
import se.emilsjolander.stickylistheaders.StickyListHeadersListView
import java.io.IOException

class BarrierFreeMoreInfoActivity : ActivityForLoadingInBackground<Void, List<BarrierFreeMoreInfo>>(R.layout.activity_barrier_free_list_info), AdapterView.OnItemClickListener {

    lateinit var listView: StickyListHeadersListView
    private var infoList: List<BarrierFreeMoreInfo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listView = findViewById(R.id.activity_barrier_info_list_view)

        startLoading()
    }

    override fun onLoadFinished(result: List<BarrierFreeMoreInfo>?) {
        showLoadingEnded()
        if (result == null || result.isEmpty()) {
            showErrorLayout()
            return
        }

        infoList = result
        listView.adapter = BarrierFreeMoreInfoAdapter(this, result)
        listView.setOnItemClickListener(this)
    }

    override fun onLoadInBackground(vararg arg: Void): List<BarrierFreeMoreInfo>? {
        showLoadingStart()
        return try {
            TUMCabeClient.getInstance(this).moreInfoList
        } catch (e: IOException) {
            Utils.log(e)
            null
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        infoList?.let {
            val url = it[position].url
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }
}
