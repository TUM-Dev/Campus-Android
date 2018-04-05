package de.tum.in.tumcampusapp.component.other.generic.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;

/**
 * The configuration screen for the {@link CardsWidget CardsWidget} AppWidget.
 */
@SuppressLint("Registered")
public class CardsWidgetConfigureActivity extends Activity {

    public static final String PREFS_NAME = "de.tum.in.tumcampusapp.component.nonui.generic.widget.CardsWidget";
    public static final String PREF_PREFIX_KEY = "appwidget_";
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private CheckBox mCafeteriaCheck;
    private CheckBox mChatCheck;
    private CheckBox mEduroamCheck;
    private CheckBox mMVVCheck;
    private CheckBox mNewsCheck;
    private CheckBox mLectureCheck;
    private CheckBox mTutionFeesCheck;
    final private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Context context = CardsWidgetConfigureActivity.this;

            // When the button is clicked, store the settingsPrefix locally
            saveTitlePref(context, mAppWidgetId, mCafeteriaCheck.isChecked(), mChatCheck.isChecked(),
                          mEduroamCheck.isChecked(), mMVVCheck.isChecked(), mNewsCheck.isChecked(),
                          mLectureCheck.isChecked(), mTutionFeesCheck.isChecked());

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            CardsWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public CardsWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    private static void saveTitlePref(Context context, int appWidgetId, boolean showCafeteria,
                                      boolean showChat, boolean showEduroam, boolean showMVV,
                                      boolean showNews, boolean showLectures, boolean showTutionFees) {
        final String prefix = PREF_PREFIX_KEY + appWidgetId;
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0)
                                                .edit();
        prefs.putBoolean(prefix + CardManager.CARD_CAFETERIA, showCafeteria);
        prefs.putBoolean(prefix + CardManager.CARD_CHAT, showChat);
        prefs.putBoolean(prefix + CardManager.CARD_EDUROAM, showEduroam);
        prefs.putBoolean(prefix + CardManager.CARD_MVV, showMVV);
        prefs.putBoolean(prefix + CardManager.CARD_NEWS, showNews);
        prefs.putBoolean(prefix + CardManager.CARD_NEXT_LECTURE, showLectures);
        prefs.putBoolean(prefix + CardManager.CARD_TUITION_FEE, showTutionFees);
        prefs.apply();
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        final String prefix = PREF_PREFIX_KEY + appWidgetId;
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0)
                                                .edit();
        prefs.remove(prefix + CardManager.CARD_CAFETERIA);
        prefs.remove(prefix + CardManager.CARD_CHAT);
        prefs.remove(prefix + CardManager.CARD_EDUROAM);
        prefs.remove(prefix + CardManager.CARD_MVV);
        prefs.remove(prefix + CardManager.CARD_NEWS);
        prefs.remove(prefix + CardManager.CARD_NEXT_LECTURE);
        prefs.remove(prefix + CardManager.CARD_TUITION_FEE);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.cards_widget_configure);

        mCafeteriaCheck = findViewById(R.id.chk_cafeteria);
        mChatCheck = findViewById(R.id.chk_chatmessages);
        mEduroamCheck = findViewById(R.id.chk_eduroam);
        mMVVCheck = findViewById(R.id.chk_mvv);
        mNewsCheck = findViewById(R.id.chk_newspread);
        mLectureCheck = findViewById(R.id.chk_lecture);
        mTutionFeesCheck = findViewById(R.id.chk_tutionFee);

        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }
}

