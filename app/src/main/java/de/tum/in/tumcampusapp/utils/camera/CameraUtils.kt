package de.tum.`in`.tumcampusapp.utils.camera

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import java.io.File

class CameraUtils(private val context: Context, root: CameraContract.View) {
    private var presenter: CameraContract.Presenter = CameraPresenter(context)
    private var thumbnailsAdapter: CameraThumbnailsAdapter

    init {
        val thumbnailSize = context.resources.getDimension(R.dimen.thumbnail_size).toInt()
        thumbnailsAdapter =
            CameraThumbnailsAdapter(
                presenter.imageElement,
                { onThumbnailRemoved(it) },
                thumbnailSize
            )
        presenter.attachView(root)
    }

    fun onNewImageTaken() {
        presenter.onNewImageTaken()
    }

    fun onNewImageSelected(path: Uri?) {
        presenter.onNewImageSelected(path)
    }

    fun showImageOptionsDialog() {
        val options = arrayOf("Take image", "gallery")
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Add picture")
            .setItems(options) { _, index -> presenter.onImageOptionSelected(index) }
            .setNegativeButton("cancel", null)
            .create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        alertDialog.show()
    }

    private fun onThumbnailRemoved(path: String) {
        val builder = AlertDialog.Builder(context)
        val view = View.inflate(context, R.layout.picture_dialog, null)

        val imageView = view.findViewById<ImageView>(R.id.imageView)        // todo fix
        imageView.setImageURI(Uri.fromFile(File(path)))

        builder.setView(view)
            .setNegativeButton("cancel", null)
            .setPositiveButton("Remove image") { _, _ -> removeThumbnail(path) }

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
    }

    private fun removeThumbnail(path: String) {
        presenter.removeImage(path)
    }

    fun onImageAdded(path: String) {
        thumbnailsAdapter.addImage(path)
    }

    fun onImageRemoved(position: Int) {
        thumbnailsAdapter.removeImage(position)
    }

    fun initRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        recyclerView.adapter = thumbnailsAdapter
    }

    fun getImagesAsArray(): Array<String> {
        return presenter.imageElement.toTypedArray()
    }

    fun processPermissionResult(permissions: Map<String, @JvmSuppressWildcards Boolean>) {
        permissions.entries.forEach {
            if (it.key == Manifest.permission.READ_EXTERNAL_STORAGE && it.value) {
                presenter.openGallery()
            } else if (it.key == Manifest.permission.CAMERA && it.value) {
                presenter.takePicture()
            } else {
                Log.d("CameraUtils", "Permission Denied")
            }
        }
    }

    fun getPaths(): List<String> {
        return thumbnailsAdapter.getPaths()
    }
}
