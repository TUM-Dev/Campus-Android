package de.tum.`in`.tumcampusapp.utils.camera

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackPresenter.Companion.PERMISSION_CAMERA
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackPresenter.Companion.PERMISSION_FILES
import de.tum.`in`.tumcampusapp.component.tumui.feedback.RequestPermissionUtils
import de.tum.`in`.tumcampusapp.component.tumui.feedback.model.FeedbackResult
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.ImageUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.security.AccessController.checkPermission

class CameraPresenter(
    private val context: Context
) : CameraContract.Presenter {

    private var currentPhotoPath: String? = null
    private var view: CameraContract.View? = null
    override val imageElement: MutableList<String> = mutableListOf()
    private lateinit var permissionUtils: RequestPermissionUtils
    override fun attachView(view: CameraContract.View) {
        this.view = view
        permissionUtils=RequestPermissionUtils(context,view)
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
        val index = feedback.picturePaths.indexOf(path)
        feedback.picturePaths.remove(path)
        File(path).delete()
        view?.onImageRemoved(index)
    }



    override fun onImageOptionSelected(option: Int) {
        if (option == 0) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissionUtils.checkPermission(Manifest.permission.CAMERA)) {
                takePicture()
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissionUtils.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                openGallery()
            }
        }
    }




    override fun onNewImageTaken() {
        currentPhotoPath?.let {
            ImageUtils.rescaleBitmap(context, it)
            feedback.picturePaths.add(it)
            view?.onImageAdded(it)
        }
    }

    override fun onNewImageSelected(uri: Uri?) {
        val filePath = ImageUtils.rescaleBitmap(context, uri ?: return) ?: return
        feedback.picturePaths.add(filePath)
        view?.onImageAdded(filePath)
    }

    override fun detachView() {
        clearPictures()
    }

    private fun clearPictures() {
        for (path in feedback.picturePaths) {
            File(path).delete()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(Const.FEEDBACK, feedback)
    }

}