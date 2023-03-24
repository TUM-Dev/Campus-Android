package de.tum.`in`.tumcampusapp.utils.camera

import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView

/**
 * CameraInterface manages a recycler view with images. Images can be either either be selected
 * from the gallery or taken directly with the camera. New images are stored in the storage and can
 * be queried by their paths. All images which are added to the list from the gallery are copied.
 * In the rest of the workflow, only this copy is used.
 * Additionally, smaller versions of the images, thumbnails, are displayed in the recycler view.
 * If thumbnails are clicked in the recyclerView an dialog opens to confirm that the image
 * should be deleted.
 *
 *How to use:
 * init in the onCreate part of the parent Activity:
 *      init(binding.thumbnailRecyclerView, this as ComponentActivity)
 * (would also work in fragments, but the init function must then be overloaded to accept fragments no yet implemented)
 *
 * Afterwards, a new image can be requested
 *      requestNewImage()
 *
 * When the camera Interface is not needed anymore and the images are never accessed again:
 *      deleteImageCopies()
 *  (E.g. in an onDestroy method of the parent activity.)
 */
interface CameraInterface {
    /**
     * Initializes the internal logic of the cameraInterface.
     * Must be called before requesting the first image.
     */
    fun init(imageRecyclerView: RecyclerView, parent: ComponentActivity)

    /**
     * Opens a dialog to select whether the image should be taken from the camera/gallery.
     * It will request the necessary permissions. Once the user selected the image, it is
     * automatically displayed in the thumbnail adapter and can now be queried with getImagePaths().
     */
    fun requestNewImage()

    /**
     * Clears the list of currently stored images.
     * Images files are also removed from the storage.
     */
    fun deleteImageCopies()

    /**
     * Returns the list of the paths to all images.
     */
    fun getImagePaths(): Array<String>
}
