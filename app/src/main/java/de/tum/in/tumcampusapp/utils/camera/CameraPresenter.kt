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
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.utils.Utils
import java.io.File
import java.io.IOException
import javax.inject.Inject


class CameraPresenter @Inject constructor(
    private val context: Context
) : CameraInterface {

    private lateinit var parentFragment: ComponentActivity
    private var currentPhotoPath: String? = null
    override val imageElement: MutableList<String> = mutableListOf()

    override fun takePicture() {
        // Create the file where the photo should go
        var photoFile: File? = null
        try {
            try {
                photoFile = ImageUtils.createImageFile(context)
                currentPhotoPath = photoFile.absolutePath
            } catch (e: IOException) {
                Utils.log(e)
            }

            if (photoFile == null) {
                return
            }
            val activityLauncher =
                parentFragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    onNewImageTaken()
                }
            val takePictureIntent = getPictureIntent(photoFile)
            activityLauncher.launch(takePictureIntent)


        } catch (e: ActivityNotFoundException) {
            photoFile?.delete()
        }
    }

    private fun getPictureIntent(photoFile: File): Intent {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val authority = "de.tum.in.tumcampusapp.fileprovider"
        val photoURI = FileProvider.getUriForFile(context, authority, photoFile)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        return takePictureIntent
    }

    override fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT


        try {

            val activityLauncher = parentFragment.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                onNewImageSelected(result)
            }
            activityLauncher.launch(Intent.createChooser(intent, "Select file"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.error_unknown, Toast.LENGTH_SHORT).show()
        }
    }

    override fun removeImage(path: String) {
        val index = imageElement.indexOf(path)
        imageElement.remove(path)
        File(path).delete()
        onImageRemoved(index)
    }

    /**
     * @return true if user has given permission before
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun checkPermission(permission: String): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(context, permission)
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            //if (view != null) {
            //    view!!.getRequestPermissionLauncher().launch(arrayOf(permission))
                // getActivity(context).register
                //getActivity(context).
              //  val mMultipleActivityResultLauncher = applicationContext.registerForActivityResult(
              //      RequestMultiplePermissions()
              //  ) { isGranted -> }

         parentFragment.registerForActivityResult(
                RequestMultiplePermissions()
            ) { permissions ->
                processPermissionResult(permissions)
            }.launch(arrayOf(permission))
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
            onImageAdded(it)
        }
    }

    override fun onNewImageSelected(result: ActivityResult) {
        val filePath = ImageUtils.rescaleBitmap(context, result.data?.data ?: return) ?: return
        imageElement.add(filePath)
        onImageAdded(filePath)
    }

    override fun showImageOptionsDialog() {
        val options = arrayOf("Take image", "gallery")
        val alertDialog = AlertDialog.Builder(parentFragment)
            .setTitle("Add picture")
            .setItems(options) { _, index -> onImageOptionSelected(index) }
            .setNegativeButton("cancel", null)
            .create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        alertDialog.show()
    }

    override fun detachView() {
        clearPictures()
    }

    override fun init(imageRecyclerView: RecyclerView, fragment: ComponentActivity) {
        imageRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        imageRecyclerView.adapter = thumbnailsAdapter
        this.parentFragment = fragment
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

    private var thumbnailsAdapter: CameraThumbnailsAdapter

    init {
        val thumbnailSize = context.resources.getDimension(R.dimen.thumbnail_size).toInt()
        thumbnailsAdapter =
            CameraThumbnailsAdapter(
                imageElement,
                { onThumbnailRemoved(it) },
                thumbnailSize
            )
    }

    private fun onThumbnailRemoved(path: String) {
        val builder = AlertDialog.Builder(context)
        val view = View.inflate(context, R.layout.picture_dialog, null)

        val imageView =
            view.findViewById<ImageView>(R.id.feedback_big_image)
        imageView.setImageURI(Uri.fromFile(File(path)))

        builder.setView(view)
            .setNegativeButton("cancel", null)
            .setPositiveButton("Remove image") { _, _ -> removeImage(path) }

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
    }

    fun onImageAdded(path: String) {
        thumbnailsAdapter.addImage(path)
    }

    fun onImageRemoved(position: Int) {
        thumbnailsAdapter.removeImage(position)
    }

    fun processPermissionResult(permissions: Map<String, @JvmSuppressWildcards Boolean>) {
        permissions.entries.forEach {
            if (it.key == Manifest.permission.READ_EXTERNAL_STORAGE && it.value) {
                openGallery()
            } else if (it.key == Manifest.permission.CAMERA && it.value) {
                takePicture()
            } else {
                Log.d("CameraUtils", "Permission Denied")
            }
        }
    }
}