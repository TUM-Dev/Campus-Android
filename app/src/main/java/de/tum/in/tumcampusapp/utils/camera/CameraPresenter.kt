package de.tum.`in`.tumcampusapp.utils.camera

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import de.tum.`in`.tumcampusapp.utils.Utils
import java.io.File
import java.io.IOException
import javax.inject.Inject

class CameraPresenter @Inject constructor(
    private val context: Context
) : CameraContract.Presenter {

    private var currentPhotoPath: String? = null
    private var view: CameraContract.View? = null
    override val imageElement: MutableList<String> = mutableListOf()

    override fun attachView(view: CameraContract.View) {
        this.view = view
    }

    override fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Create the file where the photo should go
        var photoFile: File? = null
        try {
            photoFile = ImageUtils.createImageFile(context)
            currentPhotoPath = photoFile.absolutePath
        } catch (e: IOException) {
            Utils.log(e)
        }

        if (photoFile == null) {
            return
        }

        val authority = "de.tum.in.tumcampusapp.fileprovider"
        val photoURI = FileProvider.getUriForFile(context, authority, photoFile)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        try {
            view?.openCamera(takePictureIntent)
        } catch (e: ActivityNotFoundException) {
            photoFile.delete()
        }
    }

    override fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        val chooser = Intent.createChooser(intent, "Select file")
        view?.openGallery(chooser)
    }

    override fun removeImage(path: String) {
        val index = imageElement.indexOf(path)
        imageElement.remove(path)
        File(path).delete()
        view?.onImageRemoved(index)
    }

    /**
     * @return true if user has given permission before
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun checkPermission(permission: String): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(context, permission)
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            view?.showPermissionRequestDialog(permission)
            return false
        }
        return true
    }

    override fun onImageOptionSelected(option: Int) {
        if (option == 0) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkPermission(Manifest.permission.CAMERA)) {
                takePicture()
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                openGallery()
            }
        }
    }


    override fun onNewImageTaken() {
        currentPhotoPath?.let {
            ImageUtils.rescaleBitmap(context, it)
            imageElement.add(it)
            view?.onImageAdded(it)
        }
    }
    override fun onNewImageSelected(uri: Uri?) {
        val filePath = ImageUtils.rescaleBitmap(context, uri ?: return) ?: return
        imageElement.add(filePath)
        view?.onImageAdded(filePath)
    }

    override fun detachView() {
        clearPictures()
        view = null
    }

    private fun clearPictures() {
        for (path in imageElement) {
            File(path).delete()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.e("debug", "onSaveInstanceState: called")
        //     outState.putParcelable(CA, imageElement)
    }

    companion object {
        const val FEEDBACK_IMG_COMPRESSION_QUALITY = 50
    }
}