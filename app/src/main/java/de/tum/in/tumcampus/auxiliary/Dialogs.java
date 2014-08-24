package de.tum.in.tumcampus.auxiliary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import de.tum.in.tumcampus.R;

/**
 * Helper class to access predefined alert dialogs.
 * 
 * @author Vincenz Doelle
 */
public class Dialogs {

	@SuppressWarnings("deprecation")
	public static void showAndroidVersionTooLowAlert(Context context) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle(R.string.android_version_too_low);
		alertDialog.setMessage(context
				.getString(R.string.android_version_too_low_message));
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
			}
		});
		alertDialog.show();
	}

	/**
	 * Shows a dialog asking to switch to another activity in order perform some
	 * actions there.
	 * 
	 * @param context
	 *            The current context.
	 * @param parent
	 *            The parent activity.
	 * @param msg
	 *            The message to be displayed.
	 * @param intent
	 *            The target intent if the user chooses "YES"
	 */
	public static void showIntentSwitchDialog(Context context,
			final Activity parent, String msg, final Intent intent) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(msg)
				.setCancelable(false)
				.setPositiveButton(
						context.getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								parent.startActivity(intent);
							}
						})
				.setNegativeButton(context.getString(R.string.no),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								parent.finish();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
