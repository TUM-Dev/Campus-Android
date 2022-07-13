package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.search.SearchFragment.Companion.BACK_FROM_PERSON_BY_ROOM_CLICK

class RoomContactItem(
    text: String,
    private val roomNumber: String
) : AbstractContactItem(R.string.room, text, R.drawable.ic_outline_business_24px) {

    override fun onClick(context: Context) {
        context as FragmentActivity
        val intent = Intent()
        intent.putExtra(SearchManager.QUERY, roomNumber)
        context.setResult(BACK_FROM_PERSON_BY_ROOM_CLICK, intent)
        context.onBackPressed()
    }
}