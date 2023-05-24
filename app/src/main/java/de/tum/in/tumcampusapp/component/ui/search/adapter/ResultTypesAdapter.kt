package de.tum.`in`.tumcampusapp.component.ui.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResultType

data class ResultTypeData(
    val type: SearchResultType,
    val selected: Boolean
)

class ResultTypesAdapter(
    private val onClick: (ResultTypeData) -> Unit
) : ListAdapter<ResultTypeData, ResultTypesAdapter.ResultTypeViewHolder>(ResultTypeCallback) {

    class ResultTypeViewHolder(
        itemView: View,
        private val onClick: (ResultTypeData) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val chip: Chip = itemView.findViewById(R.id.chip)

        private var currentType: ResultTypeData? = null

        init {
            chip.setOnClickListener {
                currentType?.let {
                    onClick(it)
                }
            }
        }

        fun bind(typeData: ResultTypeData) {
            currentType = typeData
            val textId = when (typeData.type) {
                SearchResultType.ALL -> R.string.all_results
                SearchResultType.PERSON -> R.string.people
                SearchResultType.LECTURE -> R.string.lectures
                SearchResultType.NAVIGA_ROOM -> R.string.rooms_id
                SearchResultType.BUILDING -> R.string.buildings
            }

            chip.text = chip.context.resources.getText(textId)
            setIsSelected(typeData.selected)
        }

        fun setIsSelected(selected: Boolean) {
            if (selected) {
                chip.isChecked = true
                chip.isCheckable = false
            } else {
                chip.isCheckable = true
                chip.isChecked = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultTypeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_result_chip, parent, false)
        return ResultTypeViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ResultTypeViewHolder, position: Int) {
        val resultType = getItem(position)
        holder.bind(resultType)
    }

    // When a payload (indicating a new selection state for an existing view) is given, the old view can be updated and
    // reused instead of creating a new one. This eliminates flicker.
    override fun onBindViewHolder(holder: ResultTypeViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            holder.setIsSelected(payloads[0] as Boolean)
        }
    }
}

object ResultTypeCallback : DiffUtil.ItemCallback<ResultTypeData>() {
    // On submitting a new list, when old and new items of the same name differ only in their
    // "selected"-state, the new state is added as a payload, indicating a possible update.
    override fun getChangePayload(oldItem: ResultTypeData, newItem: ResultTypeData): Any {
        return newItem.selected
    }

    override fun areItemsTheSame(oldItem: ResultTypeData, newItem: ResultTypeData): Boolean {
        return oldItem.type == newItem.type
    }

    override fun areContentsTheSame(oldItem: ResultTypeData, newItem: ResultTypeData): Boolean {
        return oldItem.selected == newItem.selected && oldItem.type == newItem.type
    }
}
