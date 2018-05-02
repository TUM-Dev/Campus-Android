package de.tum.`in`.tumcampusapp.component.tumui.person

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems.AbstractContactItem
import de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems.MobilePhoneContactItem
import de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems.PhoneContactItem
import kotlinx.android.synthetic.main.person_contact_item.view.*

class PersonContactItemsAdapter(
        private val items: List<AbstractContactItem>
) : RecyclerView.Adapter<PersonContactItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.person_contact_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Figure out if this is the first item of its kind
        // If so, display the icon; otherwise, don't
        val clazzItems = items.filter { clazzItem ->
            if (item::class.java == MobilePhoneContactItem::class.java) {
                // If it's a mobile phone number, we consider it part of the PhoneContactItem items
                clazzItem::class.java == item::class.java || clazzItem::class.java == PhoneContactItem::class.java
            } else {
                clazzItem::class.java == item::class.java
            }
        }
        val isFirstOfItsKind = clazzItems.first() == item
        holder.bind(item, isFirstOfItsKind)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: AbstractContactItem, showIcon: Boolean = true) = with(itemView) {
            if (showIcon) {
                iconImageView.setImageResource(item.iconResourceId)
            } else {
                iconImageView.setImageResource(android.R.color.transparent)
            }

            labelTextView.text = item.label
            valueTextView.text = item.getFormattedValue()
            setOnClickListener {
                context.startActivity(item.getIntent())
            }
        }

    }

}