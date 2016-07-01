package de.tum.in.tumcampusapp.trace;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Random;

public class DefaultExceptionHandler implements UncaughtExceptionHandler {

    private final UncaughtExceptionHandler mDefaultExceptionHandler;

    // constructor
    public DefaultExceptionHandler(UncaughtExceptionHandler pDefaultExceptionHandler) {
        mDefaultExceptionHandler = pDefaultExceptionHandler;
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
            String filename = G.appVersion + "-" + Integer.toString(random);

            // Write the stacktrace to disk
            BufferedWriter bos = new BufferedWriter(new FileWriter(G.filesPath + "/" + filename + ExceptionHandler.STACKTRACE_ENDING));
            bos.write(result.toString());
            bos.flush();
            bos.close();

            //Write the current log to file
            bos = new BufferedWriter(new FileWriter(G.filesPath + "/" + filename + ".stacktrace.log"));
            bos.write(Util.getLog());
            bos.flush();
            bos.close();

        } catch (IOException ebos) {
            // Nothing much we can do about this - the game is over
            Log.e(G.tag, "Error saving exception stacktrace", e);
        }

        //call original handler
        mDefaultExceptionHandler.uncaughtException(t, e);
    }
}