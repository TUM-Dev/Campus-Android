package de.tum.in.tumcampus.auxiliary;

import java.io.File;

/**
 * Utility functions to ease the work with files and file contents.
 */
public class FileUtils {

    /**
     * Delete all files and folder contained in a folder
     * @param fileOrDirectory File or Directory to delete
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
}
