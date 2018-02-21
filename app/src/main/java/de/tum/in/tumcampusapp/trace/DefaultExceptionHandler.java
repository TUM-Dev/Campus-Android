package de.tum.in.tumcampusapp.trace;

import android.util.Log;

import com.google.common.base.Charsets;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Random;

public class DefaultExceptionHandler implements UncaughtExceptionHandler {

    private final UncaughtExceptionHandler mDefaultExceptionHandler;

    // constructor
    public DefaultExceptionHandler(UncaughtExceptionHandler handler) {
        mDefaultExceptionHandler = handler;
    }

    // Default exception handler
    @Override
    public void uncaughtException(Thread t, Throwable e) {

        // Write the stacktrace to a variable using a PrintWriter, result contains the final stacktrace
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);

        try {
            // Random number to avoid duplicate files
            Random generator = new Random();
            int random = generator.nextInt(9999999);

            // Embed version in stacktrace filename
            String filename = G.appVersion + '-' + Integer.toString(random);

            // Write the stacktrace to disk
            try (BufferedWriter bos = new BufferedWriter(getFileWriter(G.filesPath + '/' + filename + ExceptionHandler.STACKTRACE_ENDING))) {
                bos.write(result.toString());
                bos.flush();
            }

            //Write the current log to file
            try (BufferedWriter bos = new BufferedWriter(getFileWriter(G.filesPath + '/' + filename + ".stacktrace.log"))) {
                bos.write(Util.getLog());
                bos.flush();
            }

        } catch (IOException ebos) {
            // Nothing much we can do about this - the game is over
            Log.e(G.TAG, "Error saving exception stacktrace", e);
        }

        //call original handler
        mDefaultExceptionHandler.uncaughtException(t, e);
    }

    private static Writer getFileWriter(String path) throws FileNotFoundException {
        return new OutputStreamWriter(new FileOutputStream(path), Charsets.UTF_8.newEncoder());
    }
}