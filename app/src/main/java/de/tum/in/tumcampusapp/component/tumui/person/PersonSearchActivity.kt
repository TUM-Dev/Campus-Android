package de.tum.`in`.tumcampusapp.component.tumui.person

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForSearchingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Person
import de.tum.`in`.tumcampusapp.component.tumui.person.model.PersonList
import de.tum.`in`.tumcampusapp.database.TcaDb
import kotlinx.android.synthetic.main.activity_person_search.*

/**
 * Activity to search for employees.
 */
class PersonSearchActivity : ActivityForSearchingTumOnline<PersonList>(
        R.layout.activity_person_search,
        PersonSearchSuggestionProvider.AUTHORITY, 3
), PersonSearchResultsItemListener {

    private lateinit var recentsDao: RecentsDao

    private val recents: List<Person>
        get() {
            val recents = recentsDao.getAll(RecentsDao.PERSONS) ?: return emptyList()
            return recents.map { recent -> Person.fromRecent(recent) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recentsDao = TcaDb.getInstance(this).recentsDao()

        val layoutManager = LinearLayoutManager(this)

        personsRecyclerView.setHasFixedSize(true)
        personsRecyclerView.layoutManager = layoutManager

        disableRefresh()

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
        searchPerson(query)
    }

    private fun searchPerson(query: String) {
        val apiCall = apiClient.searchPerson(query)
        fetch(apiCall)
    }

    override fun onDownloadSuccessful(response: PersonList) {
        recentsHeader.visibility = View.GONE

        if (response.persons.size == 1) {
            showPersonDetails(response.persons.first())
        } else {
            val adapter = personsRecyclerView.adapter as? PersonSearchResultsAdapter
            adapter?.update(response.persons)
        }
    }

    private fun showPersonDetails(person: Person) {
        val intent = Intent(this, PersonDetailsActivity::class.java).apply {
            putExtra("personObject", person)
        }
        startActivity(intent)
    }

}
