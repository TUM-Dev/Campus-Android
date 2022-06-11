package de.tum.`in`.tumcampusapp.component.ui.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResultType
import de.tum.`in`.tumcampusapp.utils.margin
import org.jetbrains.anko.textColorResource

data class ResultTypeData(
    val type: SearchResultType,
    val selectedType: SearchResultType,
)

class ResultTypesAdapter(
        private val onClick: (ResultTypeData) -> Unit
) : ListAdapter<ResultTypeData, ResultTypesAdapter.ResultTypeViewHolder>(ResultTypeCallback){

    class ResultTypeViewHolder(
            itemView: View,
            private val onClick: (ResultTypeData) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val constraintLayout: ConstraintLayout = this.itemView.findViewById(R.id.typeCtn)
        private val typeTextView: TextView = this.itemView.findViewById(R.id.typeTextView)

        private var currentType: ResultTypeData? = null

        init {
            itemView.setOnClickListener {
                currentType?.let {
                    onClick(it)
                }
            }
        }

        fun bind(typeData: ResultTypeData) {
            currentType = typeData
            val text = when (typeData.type) {
                SearchResultType.ALL -> R.string.all_results
                SearchResultType.PERSON -> R.string.people
                SearchResultType.LECTURE -> R.string.lectures
                SearchResultType.ROOM -> R.string.rooms
            }
            typeTextView.setText(text)

            if (typeData.type == typeData.selectedType) {
                typeTextView.textColorResource = R.color.white
                constraintLayout.setBackgroundResource(R.drawable.search_result_selected_type_background)
            }
            else {
                typeTextView.textColorResource = R.color.text_primary
                constraintLayout.setBackgroundResource(R.drawable.search_result_type_background)
            }

            if (typeData.type == SearchResultType.ALL) {
                constraintLayout.margin(left = 16F)
            } else {
                constraintLayout.margin(left = 8F)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultTypeViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.search_result_type, parent, false)
        return ResultTypeViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ResultTypeViewHolder, position: Int) {
        val resultType = getItem(position)
        holder.bind(resultType)
    }
}

object ResultTypeCallback : DiffUtil.ItemCallback<ResultTypeData>() {
    override fun areItemsTheSame(oldItem: ResultTypeData, newItem: ResultTypeData): Boolean {
        return oldItem.selectedType == newItem.selectedType && oldItem.type == newItem.type
    }

    override fun areContentsTheSame(oldItem: ResultTypeData, newItem: ResultTypeData): Boolean {
        return oldItem.selectedType == newItem.selectedType && oldItem.type == newItem.type
    }
}

