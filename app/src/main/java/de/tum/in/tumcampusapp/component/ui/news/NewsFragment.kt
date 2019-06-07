package de.tum.`in`.tumcampusapp.component.ui.news

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl.USE_CACHE
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForDownloadingExternal
import de.tum.`in`.tumcampusapp.component.ui.news.di.NewsModule
import de.tum.`in`.tumcampusapp.di.injector
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.fragment_news.newsRecyclerView
import java.lang.Math.round
import javax.inject.Inject

class NewsFragment : FragmentForDownloadingExternal(
    R.layout.fragment_news,
    R.string.news
) {

    @Inject
    lateinit var newsController: NewsController

    @Inject
    lateinit var newsDownloadAction: DownloadWorker.Action

    override val method: DownloadWorker.Action?
        get() = newsDownloadAction

    private var firstVisibleItemPosition: Int? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        injector
            .newsComponent()
            .newsModule(NewsModule())
            .build()
            .inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        requestDownload(USE_CACHE)
    }

    private fun initRecyclerView() {
        val spacing = round(resources.getDimension(R.dimen.material_card_view_padding))
        newsRecyclerView.addItemDecoration(EqualSpacingItemDecoration(spacing))
    }

    override fun onStart() {
        super.onStart()

        // Gets all news from database
        val news = newsController.getAllFromDb(requireContext())
        if (news.isEmpty()) {
            if (NetUtils.isConnected(requireContext())) {
                showErrorLayout()
            } else {
                showNoInternetLayout()
            }
            return
        }

        val adapter = NewsAdapter(requireContext(), news)
        newsRecyclerView.adapter = adapter

        // Restore previous state (including selected item index and scroll position)
        val firstVisiblePosition = firstVisibleItemPosition ?: newsController.todayIndex
        newsRecyclerView.scrollToPosition(firstVisiblePosition)

        showLoadingEnded()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_activity_news, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_disable_sources -> {
                showNewsSourcesDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showNewsSourcesDialog() {
        // Populate the settings dialog from the NewsController sources
        val newsSources = newsController.newsSources
        val items = newsSources.map { it.title }.toTypedArray()
        val checked = newsSources.map {
            Utils.getSettingBool(requireContext(), "news_source_${it.id}", true)
        }.toTypedArray().toBooleanArray()

        AlertDialog.Builder(requireContext())
            .setMultiChoiceItems(items, checked, this::onNewsSourceToggled)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
            }
            .show()
    }

    private fun onNewsSourceToggled(dialog: DialogInterface, index: Int, isChecked: Boolean) {
        val newsSources = newsController.newsSources

        if (index < newsSources.size) {
            val key = "news_source_" + newsSources[index].id
            Utils.setSetting(requireContext(), key, isChecked)

            val layoutManager = newsRecyclerView.layoutManager as LinearLayoutManager
            firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            requestDownload(USE_CACHE)
        }
    }

    companion object {
        fun newInstance() = NewsFragment()
    }

}
