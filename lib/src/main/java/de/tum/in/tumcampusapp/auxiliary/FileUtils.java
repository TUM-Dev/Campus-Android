package de.tum.in.tumcampusapp.auxiliary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility functions to ease the work with files and file contents.
 */
public class FileUtils {

    /**
     * Delete all files and folder contained in a folder
     *
     * @param fileOrDirectory
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
     * @param is
     * @return
     * @throws Exception
     */
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    /**
     * Read a file directly to a string
     * Fails silently
     * @param filePath
     * @return
     */
    public static String getStringFromFile(String filePath) {
        try {
            File fl = new File(filePath);
            FileInputStream fin = new FileInputStream(fl);
            String ret = convertStreamToString(fin);
            fin.close();
            return ret;
        } catch (Exception e) {
            Utils.log(e);
            return "";
        }
    }
}
