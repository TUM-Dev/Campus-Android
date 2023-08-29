package de.tum.`in`.tumcampusapp.utils.camera

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.utils.ThemedAlertDialogBuilder
import de.tum.`in`.tumcampusapp.utils.Utils
import java.io.File
import java.io.IOException
import javax.inject.Inject

class CameraManager @Inject constructor(
    private val context: Context
) : CameraInterface {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoLauncher: ActivityResultLauncher<Intent>

    private lateinit var parent: ComponentActivity
    private var currentPhotoPath: String? = null
    private val imageElement: MutableList<String> = mutableListOf()

    private var thumbnailsAdapter: CameraThumbnailsAdapter

    override fun init(imageRecyclerView: RecyclerView, parent: ComponentActivity) {
        imageRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        imageRecyclerView.adapter = thumbnailsAdapter
        this.parent = parent
        initLaunchers()
    }

    /**
     * Launchers must be initialized in the onCreate section of the lifecycle.
     * Will be launched later once they are needed.
     */
    private fun initLaunchers() {
        this.permissionLauncher = parent.registerForActivityResult(
            RequestMultiplePermissions()
        ) { permissions ->
            processPermissionResult(permissions)
        }
        this.galleryLauncher = parent.registerForActivityResult(
            StartActivityForResult()
        ) { result ->
            onNewImageSelected(result)
        }
        this.photoLauncher =
            parent.registerForActivityResult(StartActivityForResult()) {
                onNewImageTaken()
            }
    }

    private fun processPermissionResult(permissions: Map<String, Boolean>) {
        permissions.entries.forEach {
            if (it.key == Manifest.permission.READ_EXTERNAL_STORAGE && it.value) {
                openGallery()
            } else if (it.key == Manifest.permission.CAMERA && it.value) {
                takePicture()
            } else {
                Utils.log("Permission to take a photo/choose it from storage denied")
            }
        }
    }

    /**
     * Primarily opens the dialog to select whether the image should be taken from the gallery
     * or the camera. Forwards after one option was selected.
     */
    override fun requestNewImage() {
        val options = arrayOf(
            parent.getString(R.string.feedback_take_picture),
            parent.getString(R.string.gallery)
        )
        ThemedAlertDialogBuilder(parent).setTitle(R.string.feedback_add_picture)
            .setItems(options) { _, index -> onImageOptionSelected(index) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun onImageOptionSelected(option: Int) {
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

    /**
     * @return true if user has given permission before
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun checkPermission(permission: String): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(context, permission)
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            permissionLauncher.launch(arrayOf(permission))
            return false
        }
        return true
    }

    private fun takePicture() {
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

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val authority = "de.tum.in.tumcampusapp.fileprovider"
            val photoURI = FileProvider.getUriForFile(context, authority, photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            photoLauncher.launch(takePictureIntent)
        } catch (e: ActivityNotFoundException) {
            photoFile?.delete()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"

        try {
            galleryLauncher.launch(Intent.createChooser(intent, "Select file"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.error_unknown, Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteImage(path: String) {
        val index = imageElement.indexOf(path)
        imageElement.remove(path)
        File(path).delete()
        thumbnailsAdapter.removeImage(index)
    }

    private fun onNewImageTaken() {
        currentPhotoPath?.let {
            if (ImageUtils.rescaleBitmap(parent, it)) {
                imageElement.add(it)
                thumbnailsAdapter.addImage(it)
            }
        }
    }

    private fun onNewImageSelected(result: ActivityResult) {
        val filePath = ImageUtils.rescaleBitmap(context, result.data?.data ?: return) ?: return
        imageElement.add(filePath)
        thumbnailsAdapter.addImage(filePath)
    }

    override fun deleteImageCopies() {
        for (path in imageElement) {
            File(path).delete()
        }
    }

    override fun getImagePaths(): Array<String> {
        return imageElement.toTypedArray()
    }

    private fun showThumbnailDeletedDialog(path: String) {
        val builder = ThemedAlertDialogBuilder(parent)

        val view = View.inflate(parent, R.layout.picture_dialog, null)

        val imageView =
            view.findViewById<ImageView>(R.id.feedback_big_image)
        imageView.setImageURI(Uri.fromFile(File(path)))

        builder.setView(view)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.feedback_remove_image) { _, _ -> deleteImage(path) }

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
    }

    init {
        val thumbnailSize = context.resources.getDimension(R.dimen.thumbnail_size).toInt()
        thumbnailsAdapter =
            CameraThumbnailsAdapter(
                imageElement,
                { showThumbnailDeletedDialog(it) },
                thumbnailSize
            )
    }
}
