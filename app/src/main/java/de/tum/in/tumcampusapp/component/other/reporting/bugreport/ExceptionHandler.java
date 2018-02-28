package de.tum.in.tumcampusapp.component.other.reporting.bugreport;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.base.Charsets;
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.tum.in.tumcampusapp.api.app.AuthenticationManager;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.other.reporting.bugreport.model.BugReport;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.FileUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.Completable;
import io.reactivex.CompletableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public final class ExceptionHandler {
    public static final String STACKTRACE_ENDING = ".stacktrace";
    public static final String LINE_SEPARATOR = "line.separator";
    // Stores loaded stack traces in memory. Each element is contains a full stacktrace
    private static List<String[]> sStackTraces;
    private static boolean sSetupCalled;

    private ExceptionHandler() {
        // ExceptionHandler is a utility class
    }

    /**
     * Setup the handler for unhandled exceptions, and submit stack
     * traces from a previous crash.
     *
     * @param context context
     */
    public static void setup(Context context) {
        // Make sure this is only called once.
        if (sSetupCalled) {
            return;
        }
        sSetupCalled = true;

        Log.i(G.TAG, "Registering default exceptions handler");

        // Files dir for storing the stack traces
        G.filesPath = context.getFilesDir()
                             .getAbsolutePath();

        // Device model
        G.phoneModel = Build.MODEL;

        // Android version
        G.androidVersion = Build.VERSION.RELEASE;

        //Get the device ID
        G.deviceId = AuthenticationManager.getDeviceID(context);

        // Get information about the Package
        PackageInfo pi = Util.getPackageInfo(context);
        if (pi != null) {
            G.appVersion = pi.versionName; // Version
            G.appPackage = pi.packageName; // Package name
            G.appVersionCode = pi.versionCode; //Version code e.g.: 45
        }

        // First, search for and load stack traces
        getStackTraces();

        // Second, install the exception handler
        installHandler();

        // Third, submit any traces we may have found
        submit(context);
    }

    private static CompletableTransformer handleLifecycle(Context context) {
        if (context instanceof LifecycleOwner) {
            return AndroidLifecycle.createLifecycleProvider((LifecycleOwner) context)
                                   .bindToLifecycle();
        }
        return observable -> observable;
    }

    /**
     * Submit stack traces.
     * This is public because in some cases you might want to manually ask the traces to be submitted, for example after asking the user's permission.
     */
    private static void submit(final Context context) {
        if (!sSetupCalled) {
            throw new IllegalStateException("you need to call setup() first");
        }

        // If traces exist, we need to submit them
        if (ExceptionHandler.hasStrackTraces()) {
            // Move the list of traces to a private variable. This ensures that subsequent calls to hasStackTraces()
            // while the submission thread is ongoing, will return false, or at least would refer to some new set of traces.
            //
            // Yes, it would not be a problem from our side to have two of these submission threads ongoing at the same time (although it wouldn't currently happen as no new
            // traces can be added to the list besides through crashing the process); however, the user's callback processor might not be written to deal with that scenario.
            final List<String[]> tracesNowSubmitting = sStackTraces;
            sStackTraces = null;

            Completable.fromAction(() -> ExceptionHandler.submitStackTraces(tracesNowSubmitting, context))
                       .compose(handleLifecycle(context))
                       .subscribeOn(Schedulers.io())
                       .observeOn(AndroidSchedulers.mainThread())
                       .subscribe(() -> Utils.logv(tracesNowSubmitting.size() + " stacktraces submitted!"));
        }
    }

    /**
     * Return true if there are stacktraces that need to be submitted.
     * <p>
     * Useful for example if you would like to ask the user's permission
     * before submitting. You can then use Processor.beginSubmit() to
     * stop the submission from occurring.
     */
    private static boolean hasStrackTraces() {
        return !getStackTraces().isEmpty();
    }

    /**
     * Search for stack trace files, read them into memory and delete
     * them from disk.
     * <p>
     * They are read into memory immediately so we can go ahead and
     * install the exception handler right away, and only then try
     * and submit the traces.
     */
    private static Collection<String[]> getStackTraces() {
        if (sStackTraces != null) {
            return sStackTraces;
        }

        Utils.logv("Looking for exceptions in: " + G.filesPath);

        // Find list of .stacktrace files
        File dir = new File(G.filesPath + '/');

        // Try to create the files folder if it doesn't exist
        if (!dir.exists()) {
            dir.mkdir();
        }

        //Look into the files folder to see if there are any "*.stacktrace" files.
        String[] list = dir.list((dir1, name) -> name.endsWith(STACKTRACE_ENDING));
        Utils.logv("Found " + list.length + " stacktrace(s)");

        //Try to read all of them
        try {
            sStackTraces = new ArrayList<>();
            for (String aList : list) {

                // Limit to a certain number of SUCCESSFULLY read traces
                if (sStackTraces.size() >= G.MAX_TRACES) {
                    break;
                }

                //Full File path
                String filePath = G.filesPath + '/' + aList;

                try {
                    // Read contents of stacktrace
                    StringBuilder stacktrace = new StringBuilder();
                    try (BufferedReader input = new BufferedReader(getFileReader(filePath))) {
                        String line;
                        while ((line = input.readLine()) != null) {
                            stacktrace.append(line);
                            stacktrace.append(System.getProperty(LINE_SEPARATOR));
                        }
                    }

                    //Create the array containing the trace and the log file
                    String[] a = {stacktrace.toString(), FileUtils.getStringFromFile(filePath + ".log")};
                    sStackTraces.add(a);

                } catch (IOException e) {
                    Log.e(G.TAG, "Failed to load stack trace", e);
                }
            }

            return sStackTraces;
        } finally {
            // Delete ALL the stack traces, even those not read (if there were too many), and do this within a finally clause so that even if something very unexpected went
            // wrong above, it hopefully won't happen again the next time around (because the offending files are gone).
            for (String aList : list) {
                try {
                    File file = new File(G.filesPath + '/' + aList);
                    file.delete();
                } catch (Exception e) {
                    Log.e(G.TAG, "Error deleting trace file: " + aList, e);
                }
            }
        }
    }

    /**
     * If any are present, submit them to the trace server.
     */
    private static void submitStackTraces(List<String[]> list, Context context) {
        //Check if we user gave permission to send these reports
        G.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!G.preferences.getBoolean(Const.BUG_REPORTS, G.BUG_REPORT_DEFAULT)) {
            return;
        }

        //If nothing passed we have nothing to submit
        if (list == null) {
            return;
        }

        //Otherwise do some hard work and submit all of them after eachother
        try {
            for (String[] array : list) {
                String stacktrace = array[0];

                // Transmit stack trace with PUT request
                TUMCabeClient client = TUMCabeClient.getInstance(context);
                BugReport r = BugReport.Companion.getBugReport(context, stacktrace, array[1]);
                client.putBugReport(r);
                // We don't care about the response, so we just hope it went well and on with it.
            }
        } catch (Exception e) {
            Log.e(G.TAG, "Error submitting trace", e);
        }
    }

    private static void installHandler() {
        UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();

        // don't register again if already registered
        if (!(currentHandler instanceof DefaultExceptionHandler)) {
            // Register our default exceptions handler
            Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(currentHandler));
        }
    }

    private static Reader getFileReader(String path) throws FileNotFoundException {
        return new InputStreamReader(new FileInputStream(path), Charsets.UTF_8.newDecoder());
    }
}
