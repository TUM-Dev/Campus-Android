package de.tum.`in`.tumcampusapp.component.ui.search

import android.app.SearchManager
import android.os.Bundle
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity

class SearchActivity : BaseActivity(
    R.layout.activity_search
) {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var query = ""
        if (savedInstanceState == null) {
            val bundle = intent.extras
            if (bundle != null) {
                query = bundle.getString(SearchManager.QUERY) as String
            }
        } else {
            query = savedInstanceState.getSerializable(SearchManager.QUERY) as String
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.contentFrame, SearchFragment.newInstance(query))
            .commit()
    }
}
