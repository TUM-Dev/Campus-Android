package de.tum.`in`.tumcampusapp.component.ui.cafeteria.rating

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import de.tum.`in`.tumcampusapp.R

class ShowRatingAdapter(private var itemsList: List<ShowRatingAverage>) :
    RecyclerView.Adapter<ShowRatingAdapter.ViewHolder>() {


    private lateinit var context: Context

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var chipGroup: ChipGroup = view.findViewById(R.id.chipGroup)
        var ratingCommentTextView: TextView = view.findViewById(R.id.ratingCommentTextView)
        var showRatingImageViewHolder: CardView = view.findViewById(R.id.showRatingImageViewHolder)
        var singleRatingPointsRatingBar: RatingBar =
            view.findViewById(R.id.singleRatingPointsRatingBar)

        //  var ratingResultNumberTextView: TextView = view.findViewById(R.id.ratingResultNumberTextView)
        var listItemHolderCardView: LinearLayout = view.findViewById(R.id.listItemHolderCardView)

    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_rating, parent, false)
        context = parent.context
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemsList[position]

         if (item.img) {
            holder.showRatingImageViewHolder.visibility = View.VISIBLE
        }
        if (item.comment.length > 0) {
            holder.ratingCommentTextView.visibility = View.VISIBLE
            holder.ratingCommentTextView.text = item.comment
        }

        if (item.points == 1) {
            holder.listItemHolderCardView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.rating_1
                )
            )
        } else if (item.points == 2) {
            holder.listItemHolderCardView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.rating_2
                )
            )
        } else if (item.points == 3) {
            holder.listItemHolderCardView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.rating_3
                )
            )
        } else if (item.points == 4) {
            holder.listItemHolderCardView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.rating_4
                )
            )
        } else {
            holder.listItemHolderCardView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.rating_5
                )
            )
        }

        holder.singleRatingPointsRatingBar.rating = item.points.toFloat()
        setupChip(holder.chipGroup, item.RatingTagsResults)
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    private fun setupChip(chipGroup: ChipGroup, ratingTagsResults: List<ShowTagRatingAverage>) {

        for (tag in ratingTagsResults) {
            val chip = createChip(tag)

            chipGroup.addView(chip)
        }
    }

    private fun createChip(label: ShowTagRatingAverage): Chip {
        val chip = Chip(context)

        var colorHelper = R.color.tum_light_gray
        /*if (label.points == 2.0) {
            colorHelper = R.color.rating_2_0
        } else if (label.points == 3.0) {
            colorHelper = R.color.rating_3_0
        } else if (label.points == 4.0) {
            colorHelper = R.color.rating_4_0
        } else if (label.points == 5.0) {
            colorHelper = R.color.rating_5_0
        }*/

        chip.chipBackgroundColor = ColorStateList.valueOf(
            ContextCompat.getColor(
                context,
                colorHelper
            )
        )
        chip.text = label.tagLabel

        chip.setOnClickListener {
            Toast.makeText(context, "Rating: "+label.points, Toast.LENGTH_SHORT).show()
        }

        return chip
    }
}