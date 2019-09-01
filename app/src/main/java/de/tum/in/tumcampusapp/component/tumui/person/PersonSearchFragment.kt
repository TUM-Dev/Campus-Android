package de.tum.`in`.tumcampusapp.component.tumui.person

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForSearchingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Person
import de.tum.`in`.tumcampusapp.component.tumui.person.model.PersonList
import de.tum.`in`.tumcampusapp.database.TcaDb
import kotlinx.android.synthetic.main.fragment_person_search.personsRecyclerView
import kotlinx.android.synthetic.main.fragment_person_search.recentsHeader

class PersonSearchFragment : FragmentForSearchingTumOnline<PersonList>(
    R.layout.fragment_person_search,
    R.string.person_search,
    PersonSearchSuggestionProvider.AUTHORITY,
    minLength = 3
) {

    private lateinit var recentsDao: RecentsDao

    private val recents: List<Person>
        get() {
            val recents = recentsDao.getAll(RecentsDao.PERSONS) ?: return emptyList()
            return recents.map { recent -> Person.fromRecent(recent) }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recentsDao = TcaDb.getInstance(requireContext()).recentsDao()

        personsRecyclerView.setHasFixedSize(true)
        disableRefresh()

        val adapter = PersonSearchResultsAdapter(recents, this::onItemClick)
        if (adapter.itemCount == 0) {
            openSearch()
        }
        personsRecyclerView.adapter = adapter

        val itemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        personsRecyclerView.addItemDecoration(itemDecoration)
    }

    private fun onItemClick(person: Person) {
        val lastSearch = person.id + "$" + person.getFullName().trim { it <= ' ' }
        recentsDao.insert(Recent(lastSearch, RecentsDao.PERSONS))
        showPersonDetails(person)
    }

    override fun onStartSearch() {
        recentsHeader.visibility = View.VISIBLE
        val adapter = personsRecyclerView.adapter as? PersonSearchResultsAdapter
        adapter?.update(recents)
    }

    override fun onStartSearch(query: String?) {
        query?.let { searchPerson(it) }
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
        val intent = Intent(requireContext(), PersonDetailsActivity::class.java).apply {
            putExtra("personObject", person)
        }
        startActivity(intent)
    }

    companion object {
        fun newInstance() = PersonSearchFragment()
    }
}
