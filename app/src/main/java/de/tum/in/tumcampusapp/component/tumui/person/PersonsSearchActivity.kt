package de.tum.`in`.tumcampusapp.component.tumui.person

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineConst
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForSearchingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Person
import de.tum.`in`.tumcampusapp.component.tumui.person.model.PersonList
import de.tum.`in`.tumcampusapp.database.TcaDb
import kotlinx.android.synthetic.main.activity_persons.*

/**
 * Activity to search for employees.
 */
class PersonsSearchActivity : ActivityForSearchingTumOnline<PersonList>(
        TUMOnlineConst.PERSON_SEARCH,
        R.layout.activity_persons,
        PersonSearchSuggestionProvider.AUTHORITY, 3
), PersonSearchResultsItemListener {

    private val recentsDao = TcaDb.getInstance(this).recentsDao()

    private val recents: List<Person>
        get() {
            val recents = recentsDao.getAll(RecentsDao.PERSONS) ?: return emptyList()
            return recents.map { recent -> Person.fromRecent(recent) }
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layoutManager = LinearLayoutManager(this)

        personsRecyclerView.setHasFixedSize(true)
        personsRecyclerView.layoutManager = layoutManager

        val adapter = PersonSearchResultsAdapter(recents, this)
        if (adapter.itemCount == 0) {
            openSearch()
        }
        personsRecyclerView.adapter = adapter

        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        personsRecyclerView.addItemDecoration(itemDecoration)
    }

    override fun onItemSelected(person: Person) {
        val lastSearch = person.id + "$" + person.getFullName().trim { it <= ' ' }
        recentsDao.insert(Recent(lastSearch, RecentsDao.PERSONS))
        showPersonDetails(person)
    }

    override fun onStartSearch() {
        recentsHeader.visibility = View.VISIBLE
        val adapter = personsRecyclerView.adapter as? PersonSearchResultsAdapter
        adapter?.update(recents)
    }

    public override fun onStartSearch(query: String) {
        requestHandler.setParameter(P_SUCHE, query)
        requestFetch()
    }

    private fun proceedToPersonDetails(person: Person) {
        personsRecyclerView.adapter = null
        showPersonDetails(person)
    }

    private fun showPersonDetails(person: Person) {
        // Store selected person ID in bundle to get in in StaffDetails
        val bundle = Bundle().apply {
            putSerializable("personObject", person)
        }

        // Show detailed information in new activity
        val intent = Intent(this, PersonsDetailsActivity::class.java).apply {
            putExtras(bundle)
        }
        startActivity(intent)
    }

    /**
     * Handles the XML response from TUMOnline by de-serializing the information
     * to model entities.
     *
     * @param response The de-serialized data from TUMOnline.
     */
    public override fun onLoadFinished(response: PersonList) {
        recentsHeader.visibility = View.GONE

        if (response.persons.size == 1) {
            proceedToPersonDetails(response.persons[0])
        } else {
            val adapter = personsRecyclerView.adapter as? PersonSearchResultsAdapter
            adapter?.update(response.persons)
        }
    }

    companion object {
        private const val P_SUCHE = "pSuche"
    }

}
