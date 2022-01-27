package de.tum.`in`.tumcampusapp.component.tumui.person

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Group

class PersonGroupsAdapter(private val items: List<Group>) : RecyclerView.Adapter<PersonGroupsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.person_group_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(group: Group) = with(itemView) {
            val iconImageView = findViewById<ImageView>(R.id.iconImageView)
            val functionTextView = findViewById<TextView>(R.id.functionTextView)
            val orgTextView = findViewById<TextView>(R.id.orgTextView)

            iconImageView.visibility = if (adapterPosition == 0) View.VISIBLE else View.INVISIBLE
            functionTextView.text = group.title
            orgTextView.text = group.org
        }
    }
}