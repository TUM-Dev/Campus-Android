package de.tum.`in`.tumcampusapp.component.tumui.feedback

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.collection.ArrayMap
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R

class FeedbackThumbnailsAdapter internal constructor(
        private var paths: List<String>,
        private val onRemoveImage: (path: String) -> Unit,
        private val thumbnailSize: Int) : RecyclerView.Adapter<FeedbackThumbnailsAdapter.ViewHolder>() {

    private val pathsToThumbnails = ArrayMap<String, Bitmap>()

    init {
        for (path in paths) {
            pathsToThumbnails[path] = createThumbnail(path, thumbnailSize)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.feedback_thumb, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = paths[position]
        val thumbnail = pathsToThumbnails[path]
        holder.bind(path, thumbnail, onRemoveImage)
    }

    fun update(paths: List<String>) {
        this.paths = paths
        notifyDataSetChanged()
    }

    internal fun addImage(path: String) {
        val thumbnail = createThumbnail(path, thumbnailSize)
        pathsToThumbnails[path] = thumbnail

        val index = paths.indexOf(path)
        notifyItemInserted(index)
    }

    internal fun removeImage(position: Int) {
        notifyItemRemoved(position)
    }

    override fun getItemCount() = paths.size

    private fun createThumbnail(path: String, thumbnailSize: Int): Bitmap {
        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, bmOptions)
        val width = bmOptions.outWidth
        val height = bmOptions.outHeight

        // Determine how much to scale down the image
        val scaleFactor = Math.min(width / thumbnailSize, height / thumbnailSize)

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor

        return BitmapFactory.decodeFile(path, bmOptions)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView = itemView as ImageView

        fun bind(path: String, thumbnail: Bitmap?, onRemoveImage: (path: String) -> Unit) {
            imageView.apply {
                setImageBitmap(thumbnail)
                tag = adapterPosition
                setOnClickListener { onRemoveImage(path) }
            }
        }

    }

}
