package de.tum.in.tumcampus.trace;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Random;

public class DefaultExceptionHandler implements UncaughtExceptionHandler {

    private final UncaughtExceptionHandler defaultExceptionHandler;

    // constructor
    public DefaultExceptionHandler(UncaughtExceptionHandler pDefaultExceptionHandler) {
        defaultExceptionHandler = pDefaultExceptionHandler;
    }

    // Default exception handler
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
            String filename = G.appVersion + "-" + Integer.toString(random);
            Log.d(G.tag, "Writing unhandled exception to: " + G.filesPath + "/" + filename + ".stacktrace");

            // Write the stacktrace to disk
            BufferedWriter bos = new BufferedWriter(new FileWriter(G.filesPath + "/" + filename + ".stacktrace"));
            bos.write(result.toString());
            bos.flush();

            // Close up everything
            bos.close();

        } catch (Exception ebos) {
            // Nothing much we can do about this - the game is over
            Log.e(G.tag, "Error saving exception stacktrace", e);
        }

        Log.d(G.tag, result.toString());

        //call original handler
        defaultExceptionHandler.uncaughtException(t, e);
    }
}