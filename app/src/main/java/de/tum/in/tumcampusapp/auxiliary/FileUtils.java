package de.tum.in.tumcampusapp.auxiliary;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.common.base.Charsets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility functions to ease the work with files and file contents.
 */
public final class FileUtils {

    /**
     * Delete all files and folder contained in a folder
     */
    public static void deleteRecursive(File fileOrDirectory) {
        // Check if current item is a dir, then we need to delete all files inside
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        // Delete the current item
        fileOrDirectory.delete();
    }

    /**
     * Convert a stream to a string
     */
    public static String convertStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            reader.close();
        }
        return sb.toString();
    }

    /**
     * Read a file directly to a string
     * Fails silently
     */
    public static String getStringFromFile(String filePath) {
        try {
            File fl = new File(filePath);
            FileInputStream fin = new FileInputStream(fl);
            String ret = convertStreamToString(fin);
            fin.close();
            return ret;
        } catch (IOException e) {
            Utils.log(e);
            return "";
        }
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public static void performFileSearch(Activity activity, int requestCode) {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            activity.startActivityForResult(intent, requestCode);
        }
    }

    private FileUtils() {
        // FileUtils is a Utility class
    }
}
