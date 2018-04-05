package de.tum.`in`.tumcampusapp.component.ui.curricula

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView

import java.io.IOException
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground
import de.tum.`in`.tumcampusapp.component.ui.curricula.model.Curriculum
import de.tum.`in`.tumcampusapp.utils.NetUtils
import kotlinx.android.synthetic.main.activity_curricula.*
import se.emilsjolander.stickylistheaders.StickyListHeadersListView

/**
 * Activity to fetch and display the curricula of different study programs.
 */
class CurriculaActivity : ActivityForLoadingInBackground<Void, List<Curriculum>>(R.layout.activity_curricula), OnItemClickListener {

    private val options = HashMap<String, String>()
    private var curriculumList = ArrayList<Curriculum>()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity_curricula_list_view.setOnItemClickListener(this)

        // Fetch all curricula from webservice via parent async class
        this.startLoading()
    }

    override fun onLoadInBackground(vararg arg: Void): List<Curriculum> {
        try {
            return TUMCabeClient.getInstance(this)
                    .allCurriculas
        } catch (e: IOException) {
            return emptyList()
        }

    }

    override fun onLoadFinished(curricula: List<Curriculum>) {
        if (curricula.isEmpty()) {
            if (NetUtils.isConnected(this)) {
                showErrorLayout()
            } else {
                showNoInternetLayout()
            }
            return
        }

        options.clear()
        for (curriculum in curricula) {
            curriculumList.add(curriculum)
            options[curriculum.name] = curriculum.url
        }

        curriculumList.sort()
        activity_curricula_list_view.adapter = CurriculumAdapter(this, curriculumList)

        showLoadingEnded()
    }

    /**
     * Handle click on curricula item
     *
     * @param parent Containing listView
     * @param view   Item view
     * @param pos    Index of item
     * @param id     Id of item
     */
    override fun onItemClick(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        val curriculumName = (view as TextView).text.toString()

        // Puts URL and name into an intent and starts the detail view
        val intent = Intent(this, CurriculaDetailsActivity::class.java)
        intent.putExtra(URL, options[curriculumName])
        intent.putExtra(NAME, curriculumName)
        this.startActivity(intent)
    }

    companion object {
        val NAME = "name"
        val URL = "url"
    }
}
