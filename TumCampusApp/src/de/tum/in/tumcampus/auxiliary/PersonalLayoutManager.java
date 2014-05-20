package de.tum.in.tumcampus.auxiliary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.View;
import de.tum.in.tumcampus.R;

@SuppressLint("ResourceAsColor")
public class PersonalLayoutManager {

	public static ColorMatrixColorFilter getColorFilter(Activity activity) {
		int colorKey = getColorKey(activity);

		float r = Color.red(colorKey) / 255f;
		float g = Color.green(colorKey) / 255f;
		float b = Color.blue(colorKey) / 255f;

		ColorMatrix cm = new ColorMatrix(new float[] { r, 0, 0, 0, 0, 0, g, 0,
				0, 0, 0, 0, b, 0, 0, 0, 0, 0, 0.75f, 0, });
		ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
		return cf;
	}

	public static int getColorKey(Activity activity) {
		switch (getColorSchemeFromPreferences(activity)) {
		case 0:
			return activity.getResources().getColor(R.color.sections_blue);
		case 1:
			return activity.getResources().getColor(R.color.sections_red);
		case 2:
			return activity.getResources().getColor(R.color.sections_green);
		case 3:
			return activity.getResources().getColor(R.color.sections_gray);
		default:
			return 0;
		}
	}

	public static int getColorSchemeFromPreferences(Activity activity) {
		SharedPreferences prefs;
		int color;
		String colorAsString;

		prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		colorAsString = prefs.getString("color_scheme", "0");

		try {
			color = Integer.valueOf(colorAsString);
		} catch (Exception e) {
			color = 0;
		}
		return color;
	}

	public static void replaceColor(int fromColor, int targetColor, Bitmap image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = new int[width * height];
		image.getPixels(pixels, 0, width, 0, 0, width, height);

		for (int x = 0; x < pixels.length; ++x) {
			pixels[x] = (pixels[x] == fromColor) ? targetColor : pixels[x];
		}

		image = Bitmap.createBitmap(width, height, image.getConfig());
		image.setPixels(pixels, 0, width, 0, 0, width, height);
	}

	public static void setColorForId(Activity activity, int id) {
		int colorKey = getColorKey(activity);
		activity.findViewById(id).setBackgroundColor(colorKey);

	}

	public static void setColorForId(Activity activity, View rootView, int id) {
		int colorKey = getColorKey(activity);
		rootView.findViewById(id).setBackgroundColor(colorKey);
	}

	public static void setColorForView(Activity activity, View view) {
		int colorKey = getColorKey(activity);
		view.setBackgroundColor(colorKey);
	}

	public static void setDrawableColorForId(Activity activity, int id) {
		Drawable drawable = activity.getResources().getDrawable(id);
		drawable.setColorFilter(getColorFilter(activity));
	}
}
