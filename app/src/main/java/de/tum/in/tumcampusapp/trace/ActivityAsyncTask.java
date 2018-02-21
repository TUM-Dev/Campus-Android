package de.tum.in.tumcampusapp.trace;

import android.os.AsyncTask;

/**
 * Copied from android-autostarts:
 * http://github.com/miracle2k/android-autostarts
 * <p/>
 * Android's ASyncTask is very useful, but using a background thread
 * can be though in the context of the Activity lifecycle, at least
 * if you'd prefer your background task to not restart with an
 * orientation change.
 * <p/>
 * This version of ASyncTask tries to alleviate those pains. The idea
 * is that you "retain" an instance of this task on orientation change.
 * The task can be "connected" to exactly one or zero objects at any
 * time. On orientation change, the old instance disconnects itself,
 * then the new one connects.
 * <p/>
 * The object you connect to typically is something that references
 * back to your activity (might be the activity itself), and which your
 * task needs to do it's job (for example, where it posts it's results
 * to).
 * <p/>
 * This class ensures that whenever a new object connects while the task
 * is still active, the preExecute() handler is run again, and if the
 * task finished while no object was connected, the processPostExecute()
 * handler is run the next time an activity connects.
 * <p/>
 * Note "processPostExecute()", which is a replacement for the
 * "postExcute()" method of ASyncTask which you should use instead.
 * <p/>
 * TODO: there is a small chance at race conditions here when checking
 * for mWrapped and setting mPostProcessingDone. We should fix those
 * by using a lock.
 */
public abstract class ActivityAsyncTask<Q, R, S, T> extends AsyncTask<R, S, T> {

    protected volatile Q mWrapped;
    private volatile boolean mPostProcessingDone;
    private T mResult;

    public ActivityAsyncTask(Q initialConnect) {
        super();
        mPostProcessingDone = false;
        connectTo(initialConnect);
    }

    /**
     * Q to the given object, or "null" to disconnect.
     * <p/>
     * Raises an exception if we are already connected.
     */
    public final void connectTo(Q wrappedObject) {
        if (mWrapped != null && wrappedObject != null) {
            throw new IllegalStateException();
        }

        mWrapped = wrappedObject;

        if (mWrapped != null) {
            // Set the task up with the new activity.
            if (getStatus() == Status.RUNNING) {
                onPreExecute();
            }

            // If we were unable to do the full post processing because of
            // no object being available, do so now.
            else if (getStatus() == Status.FINISHED && !mPostProcessingDone) {
                mPostProcessingDone = true;
                processPostExecute(mResult);
                mResult = null;  // Be sure to free reference now.
            }
        }
    }

    /**
     * Return whether post processing has been completed.
     */
    public boolean postProcessingDone() {
        return mPostProcessingDone;
    }

    @Override
    protected void onPostExecute(T result) {
        super.onPostExecute(result);

        // We need to make sure we only go on if an activity is
        // attached. Since it's possible that, say, an orientation
        // change happens while we are running, it can happen that
        // there isn't one. If so, processPostExecute() will be
        // run the next time one is attached.
        if (mWrapped == null) {
            // Remember result for the next connect.
            mResult = result;
        } else {
            mPostProcessingDone = true;
            processPostExecute(result);
        }
    }

    /**
     * You should override this rather than onPostExecute()
     * to ensure your handler will be called even if at the
     * time of a finish the task is not connected.
     */
    protected abstract void processPostExecute(T result);
}