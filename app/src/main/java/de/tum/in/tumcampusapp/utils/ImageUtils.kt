package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * Helper class to create image files and rescale Bitmaps.
 */
object ImageUtils {

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp = DateTimeFormat.forPattern("yyyyMMdd_HHmmss")
                .withLocale(Locale.GERMANY)
                .print(DateTime.now())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        )
    }

    /**
     * Destination is a new image file
     *
     * @return absolute path of the new destination file
     */
    fun rescaleBitmap(context: Context, src: Uri): String? {
        return try {
            val destination = createImageFile(context)
            rescaleBitmap(context, src, destination)
            destination.absolutePath
        } catch (e: IOException) {
            Utils.log(e)
            null
        }
    }

    /**
     * @param filePath source and destination
     */
    fun rescaleBitmap(context: Context, filePath: String) {
        rescaleBitmap(context, Uri.fromFile(File(filePath)), File(filePath))
    }

    /**
     * Scales down the image and writes it to the destination file
     */
    private fun rescaleBitmap(context: Context, src: Uri, destination: File) {
        try {
            var bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, src)
            val out = ByteArrayOutputStream()
            Utils.log("img before: ${bitmap.width} x ${bitmap.height}")
            bitmap = getResizedBitmap(bitmap)
            Utils.log("img after: ${bitmap.width} x ${bitmap.height}")
            bitmap.compress(Bitmap.CompressFormat.JPEG, Const.FEEDBACK_IMG_COMPRESSION_QUALITY, out)
            FileOutputStream(destination).apply {
                write(out.toByteArray())
                close()
            }
            out.close()
        } catch (e: IOException) {
            Utils.log(e)
        }
    }

    /**
     * Scales the bitmap down if it's bigger than maxSize
     *
     * @return a resized copy of the bitmap
     */
    private fun getResizedBitmap(image: Bitmap, maxSize: Int = 1000): Bitmap {
        var width = image.width
        var height = image.height
        if (width < maxSize && height < maxSize) {
            return image
        }

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }
}
