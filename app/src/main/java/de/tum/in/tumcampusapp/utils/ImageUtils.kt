package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

object ImageUtils {

    @JvmStatic
    fun getImageUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "de.tum.in.tumcampusapp.fileprovider", file)
    }

    @JvmStatic
    fun createImageFile(context: Context): File? {
        // Create an image file name
        val timeStamp = DateTimeFormat.forPattern("yyyyMMdd_HHmmss")
                .withLocale(Locale.GERMANY)
                .print(DateTime.now())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        //mCurrentPhotoPath = image.absolutePath
        return tryOrNull {
            File.createTempFile(imageFileName, ".jpg", storageDir)
        }
    }

    @JvmStatic
    fun rescaleBitmap(context: Context, src: Uri?, destination: File) {
        try {
            var bitmap = MediaStore.Images.Media.getBitmap(context.applicationContext.contentResolver, src)
            val out = ByteArrayOutputStream()
            bitmap = getResizedBitmap(bitmap, 1000)
            bitmap.compress(Bitmap.CompressFormat.JPEG, Const.FEEDBACK_IMG_COMPRESSION_QUALITY, out)
            val fileOut = FileOutputStream(destination)
            fileOut.write(out.toByteArray())
            fileOut.close()
            out.close()
        } catch (e: IOException) {
            Utils.log(e)
        }
    }

    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
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
