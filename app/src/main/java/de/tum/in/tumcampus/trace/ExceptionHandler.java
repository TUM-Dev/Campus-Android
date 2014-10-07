package de.tum.in.tumcampus.trace;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;

public class ExceptionHandler {

    // Stores loaded stack traces in memory. Each element is contains a full stacktrace
    private static ArrayList<String> sStackTraces = null;

    private static ActivityAsyncTask<Processor, Object, Object, Object> sTask;
    private static boolean sVerbose = false;
    private static int sMinDelay = 0;
    private static Integer sTimeout = null;
    private static boolean sSetupCalled = false;

    public static interface Processor {
        boolean beginSubmit();

        void submitDone();

        void handlerInstalled();
    }

    /**
     * Setup the handler for unhandled exceptions, and submit stack
     * traces from a previous crash.
     *
     * @param context
     * @param processor
     */
    public static boolean setup(Context context, final Processor processor) {

        // Make sure this is only called once.
        if (sSetupCalled) {

            // Tell the task that it now has a new context.
            if (sTask != null && !sTask.postProcessingDone()) {

                // We don't want to force the user to call our notifyContextGone() if he doesn't care about that functionality anyway, so in order to avoid the
                // InvalidStateException, ensure first that we are disconnected.
                sTask.connectTo(null);
                sTask.connectTo(processor);
            }
            return false;
        }
        sSetupCalled = true;

        Log.i(G.tag, "Registering default exceptions handler");

        //Remeber the context
        G.context = context;

        // Files dir for storing the stack traces
        G.filesPath = context.getFilesDir().getAbsolutePath();

        // Device model
        G.phoneModel = android.os.Build.MODEL;

        // Android version
        G.androidVersion = android.os.Build.VERSION.RELEASE;

        //Get the device ID
        G.deviceId = NetUtils.getDeviceID(context);

        // Get information about the Package
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            G.appVersion = pi.versionName; // Version
            G.appPackage = pi.packageName; // Package name

        } catch (NameNotFoundException e) {
            Log.e(G.tag, "Error collecting trace information", e);
        }

        if (sVerbose) {
            Log.i(G.tag, "TRACE_VERSION: " + G.traceVersion);
            Log.d(G.tag, "appVersion: " + G.appVersion);
            Log.d(G.tag, "appPackage: " + G.appPackage);
            Log.d(G.tag, "filesPath: " + G.filesPath);
            Log.d(G.tag, "URL: " + G.URL);
        }

        // First, search for and load stack traces
        getStackTraces();

        // Second, install the exception handler
        installHandler();
        processor.handlerInstalled();

        // Third, submit any traces we may have found
        return submit(processor);
    }

    /**
     * Setup the handler for unhandled exceptions, and submit stack
     * traces from a previous crash.
     * <p/>
     * Simplified version that uses a default processor.
     *
     * @param context
     */
    public static boolean setup(Context context) {
        return setup(context, new Processor() {
            public boolean beginSubmit() {
                return true;
            }

            public void submitDone() {
            }

            public void handlerInstalled() {
            }
        });
    }

    /**
     * If your "Processor" depends on a specific context/activity, call
     * this method at the appropriate time, for example in your activity
     * "onDestroy". This will ensure that we'll hold off executing
     * "submitDone" or "handlerInstalled" until setup() is called again
     * with a new context.
     */
    public static void notifyContextGone() {
        if (sTask == null) {
            return;
        }

        sTask.connectTo(null);
    }

    /**
     * Submit stack traces.
     * This is public because in some cases you might want to manually ask the traces to be submitted, for example after asking the user's permission.
     */
    public static boolean submit(final Processor processor) {
        if (!sSetupCalled)
            throw new RuntimeException("you need to call setup() first");

        boolean stackTracesFound = hasStrackTraces();

        // If traces exist, we need to submit them
        if (stackTracesFound) {
            boolean proceed = processor.beginSubmit();
            if (proceed) {
                // Move the list of traces to a private variable. This ensures that subsequent calls to hasStackTraces()
                // while the submission thread is ongoing, will return false, or at least would refer to some new set of traces.
                //
                // Yes, it would not be a problem from our side to have two of these submission threads ongoing at the same time (although it wouldn't currently happen as no new
                // traces can be added to the list besides through crashing the process); however, the user's callback processor might not be written to deal with that scenario.
                final ArrayList<String> tracesNowSubmitting = sStackTraces;
                sStackTraces = null;

                sTask = new ActivityAsyncTask<Processor, Object, Object, Object>(processor) {

                    private long mTimeStarted;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        mTimeStarted = System.currentTimeMillis();
                    }

                    @Override
                    protected Object doInBackground(Object... params) {
                        submitStackTraces(tracesNowSubmitting);

                        long rest = sMinDelay - (System.currentTimeMillis() - mTimeStarted);
                        if (rest > 0) {
                            try {
                                Thread.sleep(rest);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onCancelled() {
                        super.onCancelled();
                    }

                    @Override
                    protected void processPostExecute(Object result) {
                        mWrapped.submitDone();
                    }
                };
                sTask.execute();
            }
        }

        return stackTracesFound;
    }

    /**
     * Version of submit() that doesn't take a processor.
     */
    public static boolean submit() {
        return submit(new Processor() {
            public boolean beginSubmit() {
                return true;
            }

            public void submitDone() {
            }

            public void handlerInstalled() {
            }
        });
    }

    /**
     * Return true if there are stacktraces that need to be submitted.
     * <p/>
     * Useful for example if you would like to ask the user's permission
     * before submitting. You can then use Processor.beginSubmit() to
     * stop the submission from occurring.
     */
    public static boolean hasStrackTraces() {
        return (getStackTraces().size() > 0);
    }

    /**
     * Delete loaded stack traces from memory. Normally, this will
     * happen automatically after submission, but if you don't submit,
     * this is for you.
     */
    public static void clear() {
        sStackTraces = null;
    }

    /**
     * Search for stack trace files, read them into memory and delete
     * them from disk.
     * <p/>
     * They are read into memory immediately so we can go ahead and
     * install the exception handler right away, and only then try
     * and submit the traces.
     */
    private static ArrayList<String> getStackTraces() {
        if (sStackTraces != null) {
            return sStackTraces;
        }

        Log.d(G.tag, "Looking for exceptions in: " + G.filesPath);

        // Find list of .stacktrace files
        File dir = new File(G.filesPath + "/");

        // Try to create the files folder if it doesn't exist
        if (!dir.exists()) {
            dir.mkdir();
        }

        //Look into the files folder to see if there are any "*.stacktrace" files.
        String[] list = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".stacktrace");
            }
        });
        Log.d(G.tag, "Found " + list.length + " stacktrace(s)");

        //Try to read all of them
        try {

            sStackTraces = new ArrayList<String>();
            for (int i = 0; i < list.length; i++) {

                // Limit to a certain number of SUCCESSFULLY read traces
                if (sStackTraces.size() >= G.MAX_TRACES) {
                    break;
                }

                //Full File path
                String filePath = G.filesPath + "/" + list[i];

                try {
                    // Read contents of stacktrace
                    StringBuilder stacktrace = new StringBuilder();
                    BufferedReader input = new BufferedReader(new FileReader(filePath));
                    try {
                        String line = null;
                        while ((line = input.readLine()) != null) {
                            stacktrace.append(line);
                            stacktrace.append(System.getProperty("line.separator"));
                        }
                    } finally {
                        input.close();
                    }
                    sStackTraces.add(stacktrace.toString());

                } catch (FileNotFoundException e) {
                    Log.e(G.tag, "Failed to load stack trace", e);
                } catch (IOException e) {
                    Log.e(G.tag, "Failed to load stack trace", e);
                }
            }

            return sStackTraces;
        } finally {
            // Delete ALL the stack traces, even those not read (if there were too many), and do this within a finally clause so that even if something very unexpected went
            // wrong above, it hopefully won't happen again the next time around (because the offending files are gone).
            for (int i = 0; i < list.length; i++) {
                try {
                    File file = new File(G.filesPath + "/" + list[i]);
                    file.delete();
                } catch (Exception e) {
                    Log.e(G.tag, "Error deleting trace file: " + list[i], e);
                }
            }
        }
    }

    /**
     * If any are present, submit them to the trace server.
     */
    private static void submitStackTraces(ArrayList<String> list) {
        //Check if we user gave permission to send these reports
        G.preferences = PreferenceManager.getDefaultSharedPreferences(G.context);
        if (G.preferences.getBoolean(Const.BUG_REPORTS, G.bugReportDefault) == false) {
            return;
        }


        //If nothing passed we have nothing to submit
        if (list == null) {
            return;
        }

        //Otherwise do some hard work and submit all of them after eachother
        try {
            String[] screenProperties = Util.ScreenProperties();

            for (int i = 0; i < list.size(); i++) {
                String stacktrace = list.get(i);

                Log.d(G.tag, "Transmitting stack trace: " + stacktrace);

                // Transmit stack trace with PUT request
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpParams params = httpClient.getParams();
                HttpProtocolParams.setUseExpectContinue(params, false);
                if (sTimeout != null) {
                    HttpConnectionParams.setConnectionTimeout(params, sTimeout);
                    HttpConnectionParams.setSoTimeout(params, sTimeout);
                }
                HttpPut request = new HttpPut(G.URL);
                request.addHeader("X-DEVICE-ID", G.deviceId); // Add our device identifier

                List<NameValuePair> nvps = new ArrayList<NameValuePair>();

                //Add some Device infos
                nvps.add(new BasicNameValuePair("packageName", G.appPackage));
                nvps.add(new BasicNameValuePair("packageVersion", G.appVersion));
                nvps.add(new BasicNameValuePair("phoneModel", G.phoneModel));
                nvps.add(new BasicNameValuePair("androidVersion", G.androidVersion));

                nvps.add(new BasicNameValuePair("networkWifi", Util.isWifiOn()));
                nvps.add(new BasicNameValuePair("networkMobile", Util.isMobileNetworkOn()));
                nvps.add(new BasicNameValuePair("gps", Util.isGPSOn()));

                nvps.add(new BasicNameValuePair("screenWidth", screenProperties[0]));
                nvps.add(new BasicNameValuePair("screenHeight", screenProperties[1]));
                nvps.add(new BasicNameValuePair("screenOrientation", screenProperties[2]));
                nvps.add(new BasicNameValuePair("screenDpi", screenProperties[3] + ":" + screenProperties[4]));

                //Add the stacktrace
                nvps.add(new BasicNameValuePair("stacktrace", stacktrace));
                nvps.add(new BasicNameValuePair("log", Util.getLog()));
                request.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

                // We don't care about the response, so we just hope it went well and on with it.
                httpClient.execute(request);
            }
        } catch (Exception e) {
            Log.e(G.tag, "Error submitting trace", e);
        }
    }

    private static void installHandler() {
        UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (currentHandler != null && sVerbose) {
            Log.d(G.tag, "current handler class=" + currentHandler.getClass().getName());
        }

        // don't register again if already registered
        if (!(currentHandler instanceof DefaultExceptionHandler)) {
            // Register our default exceptions handler
            Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(currentHandler));
        }
    }
}
