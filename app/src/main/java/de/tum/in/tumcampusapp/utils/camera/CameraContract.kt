package de.tum.`in`.tumcampusapp.utils.camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView

/**
 * Camera Presenter offers the functionality to select images either in teh gallery or take a new photo with the camera and show them as small thumbnails in a recycler view.
 *
 * How to use:
 * call init from an activity
 * request a new photo -> thumbnail is displayed in the recyclerview
 *
 */
interface CameraInterface {
    fun init(imageRecyclerView: RecyclerView, parent: ComponentActivity)
    fun requestNewImage()
    fun clearImages()
    fun getImagePaths(): Array<String>
}
