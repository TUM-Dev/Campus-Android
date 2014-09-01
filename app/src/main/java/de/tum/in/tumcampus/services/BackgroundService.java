package de.tum.in.tumcampus.services;

import android.app.IntentService;
import android.content.Intent;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;

/** Service used to sync data in background */
public class BackgroundService extends IntentService {

	public static final String BACKGROUND_SERVICE = "BackgroundService";

	public BackgroundService() {
		super(BACKGROUND_SERVICE);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.log("BackgroundService has started");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Utils.log("BackgroundService has stopped");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
        // Download all from external
        Intent service = new Intent(this, DownloadService.class);
        service.putExtra(Const.ACTION_EXTRA, Const.DOWNLOAD_ALL_FROM_EXTERNAL);
        service.putExtra(Const.FORCE_DOWNLOAD, false);
        service.putExtra(Const.APP_LAUNCHES, intent.getBooleanExtra(Const.APP_LAUNCHES,false));
        startService(service);
	}


    /**
     * This method should fetch the grade in order to get updates grades. It is not implemented,
     *  since the grade web service is under change and redevelops the grade interface.
     */
	/*private void fetchGrades() {
		// fetching xml from tum online
		TUMOnlineRequest requestHandler = new TUMOnlineRequest(Const.NOTEN,
				getApplicationContext());
		String rawResponse = requestHandler.fetch();
		Serializer serializer = new Persister();
		ExamList examList = null;
		// Deserializes XML response
		try {
			examList = serializer.read(ExamList.class, rawResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// generating notification
		generateNotification(examList);
	}

	private void generateNotification(ExamList examList) {

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		int gradeCount = settings.getInt(Const.Grade_Count, 0);
		// Log.d("Grade Count", "" + gradeCount);
		int newSize = examList.getExams().size();
		if (gradeCount != 0) {
			if (newSize > gradeCount) {
				Editor editor = settings.edit();
				editor.putInt(Const.Grade_Count, newSize);
				editor.commit();
				int icon = R.drawable.ic_notification;
				// Generating Notification
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
						this)
						.setSmallIcon(icon)
						.setContentTitle(getString(R.string.notification_title))
						.setContentText(
								getString(R.string.notification_content));
				// Creates an explicit intent for an Activity in your app
				Intent resultIntent = new Intent(this, GradesActivity.class);

				// The stack builder object will contain an artificial back
				// stack for the
				// started Activity.
				// This ensures that navigating backward from the Activity leads
				// out of
				// your application to the Home screen.
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
				// Adds the back stack for the Intent (but not the Intent
				// itself)
				stackBuilder.addParentStack(GradesActivity.class);
				// Adds the Intent that starts the Activity to the top of the
				// stack
				stackBuilder.addNextIntent(resultIntent);
				PendingIntent resultPendingIntent = stackBuilder
						.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
				mBuilder.setContentIntent(resultPendingIntent);
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				int mId = 0;
				// mId allows you to update the notification later on.
				mNotificationManager.notify(mId, mBuilder.build());
			}
		} else {
			Editor editor = settings.edit();
			editor.putInt(Const.Grade_Count, newSize);
			editor.commit();
		}
	}*/
}