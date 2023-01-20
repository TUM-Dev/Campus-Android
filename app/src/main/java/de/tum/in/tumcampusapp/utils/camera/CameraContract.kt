package de.tum.`in`.tumcampusapp.utils.camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView

interface CameraInterface {
    fun init(imageRecyclerView: RecyclerView, parent: ComponentActivity)
    fun requestNewImage()
  //  fun removeImage(path: String)
    fun onSaveInstanceState(outState: Bundle)
    fun clearImages()
    fun getImagePaths(): List<String>
}
