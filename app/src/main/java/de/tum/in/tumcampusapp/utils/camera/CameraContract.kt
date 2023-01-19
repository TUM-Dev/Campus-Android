package de.tum.`in`.tumcampusapp.utils.camera

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

    interface CameraInterface {
        val imageElement: MutableList<String>
        fun removeImage(path: String)
        fun onImageOptionSelected(option: Int)
        fun onNewImageTaken()
        fun onNewImageSelected(result: ActivityResult)
        fun takePicture()
        fun openGallery()
        fun onSaveInstanceState(outState: Bundle)
        fun detachView()
        fun showImageOptionsDialog()
        fun init(imageRecyclerView: RecyclerView, fragment: ComponentActivity)
    }
