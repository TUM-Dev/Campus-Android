package de.tum.`in`.tumcampusapp.component.tumui.person

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems.AbstractContactItem
import de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems.MobilePhoneContactItem
import de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems.PhoneContactItem
import de.tum.`in`.tumcampusapp.utils.Utils
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
        val contactItem = items[position]

        // Figure out if this is the first item of its kind
        // If so, display the icon; otherwise, don't
        val classItems = items.filter { item ->
            if (contactItem::class.java == MobilePhoneContactItem::class.java) {
                // If it's a mobile phone number, we consider it part of the PhoneContactItem items
                item::class.java == contactItem::class.java || item::class.java == PhoneContactItem::class.java
            } else {
                item::class.java == contactItem::class.java
            }
        }
        val isFirstOfItsKind = classItems.first() == contactItem
        holder.bind(contactItem, isFirstOfItsKind)
    }

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: AbstractContactItem, showIcon: Boolean = true) = with(itemView) {
            val resourceId = if (showIcon) item.iconResourceId else android.R.color.transparent
            iconImageView.setImageResource(resourceId)

            labelTextView.text = context.getString(item.labelResourceId)
            valueTextView.text = item.value

            setOnClickListener {
                item.getIntent(context)?.let { intent -> handleItemClick(context, intent) }
            }
        }

        private fun handleItemClick(context: Context, intent: Intent) {
            val canHandleIntent = intent.resolveActivity(context.packageManager) != null
            if (canHandleIntent) {
                context.startActivity(intent)
            } else {
                Utils.showToast(context, R.string.action_cant_be_performed)
            }
        }
    }
}