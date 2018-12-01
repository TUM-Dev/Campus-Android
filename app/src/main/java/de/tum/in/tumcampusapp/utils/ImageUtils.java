package de.tum.in.tumcampusapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Helper class to create image files and rescale Bitmaps.
 */
public final class ImageUtils {

    private ImageUtils() {
    }

    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = DateTimeFormat.forPattern("yyyyMMdd_HHmmss")
                .withLocale(Locale.GERMANY)
                .print(DateTime.now());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    /**
     * destination is a new image file
     *
     * @return absolute path of the new destination file
     */
    public static String rescaleBitmap(Context context, Uri src) {
        try {
            File destination = createImageFile(context);
            rescaleBitmap(context, src, destination);
            return destination.getAbsolutePath();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    /**
     * @param filePath source and destination
     */
    public static void rescaleBitmap(Context context, String filePath) {
        rescaleBitmap(context, Uri.fromFile(new File(filePath)), new File(filePath));
    }

    /**
     * scales down the image and writes it to the destination file
     */
    private static void rescaleBitmap(Context context, Uri src, File destination) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), src);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Utils.log("img before: " + bitmap.getWidth() + " x " + bitmap.getHeight());
            bitmap = getResizedBitmap(bitmap, 1000);
            Utils.log("img after: " + bitmap.getWidth() + " x " + bitmap.getHeight());
            bitmap.compress(Bitmap.CompressFormat.JPEG, Const.FEEDBACK_IMG_COMPRESSION_QUALITY, out);
            FileOutputStream fileOut = new FileOutputStream(destination);
            fileOut.write(out.toByteArray());
            fileOut.close();
            out.close();
        } catch (IOException e) {
            Utils.log(e);
        }
    }

    /**
     * scales the bitmap down if it's bigger than maxSize
     *
     * @return a resized copy of the bitmap
     */
    private static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width < maxSize && height < maxSize) {
            return image;
        }

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

}
