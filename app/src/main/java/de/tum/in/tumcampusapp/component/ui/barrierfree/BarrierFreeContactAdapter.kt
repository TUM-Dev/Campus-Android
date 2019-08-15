package de.tum.`in`.tumcampusapp.component.ui.barrierfree

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter
import de.tum.`in`.tumcampusapp.component.tumui.person.PersonDetailsActivity
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Person
import de.tum.`in`.tumcampusapp.component.ui.barrierfree.model.BarrierFreeContact
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter

/**
 * An adapter used to display contact information in barrierfree page.
 */
class BarrierFreeContactAdapter(context: Context, infos: List<BarrierFreeContact>) :
        SimpleStickyListHeadersAdapter<BarrierFreeContact>(context, infos.toMutableList()), StickyListHeadersAdapter {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View
        if (convertView == null) {
            // Crate UI element
            view = inflater.inflate(R.layout.activity_barrier_free_contact_listview, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        // display information of current person
        val contact = itemList[position]

        if (!contact.isValid) {
            view.visibility = View.GONE
            return view
        }

        // set Information
        view.visibility = View.VISIBLE
        holder.setContent(contact)

        return view
    }

    // the layout of the list
    internal inner class ViewHolder(view: View) {
        var name: TextView = view.findViewById(R.id.barrierfreeContactNameTextView)
        var phone: TextView = view.findViewById(R.id.barrierfreeContactPhoneTextView)
        var email: TextView = view.findViewById(R.id.barrierfreeContactEmailTextView)
        var more: TextView = view.findViewById(R.id.barrierfreeContactMoreTextView)

        fun setContent(contact: BarrierFreeContact) {
            name.text = contact.name
            phone.setText(contact.telephone, TextView.BufferType.SPANNABLE)
            Linkify.addLinks(phone, Linkify.ALL)
            email.text = contact.email

            if (!contact.hasTumID) {
                more.visibility = View.GONE
            } else {
                // Jump to PersonDetail Activity
                more.visibility = View.VISIBLE
                more.text = context.getString(R.string.more_info)
                more.setOnClickListener {
                    val person = Person(name = contact.name, id = contact.tumID)
                    val bundle = Bundle().apply {
                        putSerializable("personObject", person)
                    }
                    val intent = Intent(context, PersonDetailsActivity::class.java).apply {
                        putExtras(bundle)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}