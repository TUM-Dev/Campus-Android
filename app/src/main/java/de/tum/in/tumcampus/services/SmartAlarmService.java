package de.tum.in.tumcampus.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.SmartAlarmInfo;

public class SmartAlarmService extends Service {

    private boolean stopVibrate = false;

    private Uri ringtoneURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    private MediaPlayer player;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Vibrates the phone and displays alarm dialog
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.log("SmartAlarm: show alarm dialog");

        // activate screen
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "SmartAlarm");
        wl.acquire();
        wl.release();

        final WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        // add dismiss button functionality
        @SuppressLint("InflateParams")
        final View mView = li.inflate(R.layout.smart_alarm_dialog, null);
        Button dismissBtn = (Button) mView.findViewById(R.id.dismiss);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
                mWindowManager.removeViewImmediate(mView);
            }
        });

        // display info in dialog
        SmartAlarmInfo sai = (SmartAlarmInfo) intent.getExtras().get(SmartAlarmReceiver.INFO);
        ((TextView) mView.findViewById(R.id.alarm_time)).setText(sai.getFormattedWakeupTime(getApplicationContext()));
        ((TextView) mView.findViewById(R.id.alarm_date)).setText(sai.getFormattedWakeupDate(getApplicationContext()));
        ((TextView) mView.findViewById(R.id.next_lecture_title)).setText(sai.getLectureTitle());

        if (sai.getFirstTransportType() != SmartAlarmInfo.TransportType.PRIVATE) {
            if (sai.getFirstTransportType() == SmartAlarmInfo.TransportType.FOOT) {
                String walkTo = getResources().getString(R.string.smart_alarm_walk_to);
                ((TextView) mView.findViewById(R.id.transport_destination)).setText(walkTo + " " + sai.getFirstTrainDst());
            } else {
                ((ImageView) mView.findViewById(R.id.transport_icon)).setImageResource(sai.getFirstTransportType().getIcon());
                ((TextView) mView.findViewById(R.id.transport_nr)).setText(sai.getFirstTrainLabel());
                ((TextView) mView.findViewById(R.id.transport_destination)).setText(sai.getFirstTrainDst());
            }

            ((TextView) mView.findViewById(R.id.transport_departure)).setText(sai.getFormattedDeparture(getApplicationContext()));
        }

        // show on lock screen
        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON ,
                PixelFormat.RGBA_8888);

        // finally show alarm view
        mWindowManager.addView(mView, mLayoutParams);
        mWindowManager.updateViewLayout(mView, mLayoutParams);

        // repeat vibrating the phone, 500ms on, 500ms off
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("smart_alarm_vibrate", false)) {
            ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(new long[]{0, 500, 500}, 0);
            stopVibrate = true;
        }

        // play ringtone
        String ringtone = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("smart_alarm_ringtone", "");
        if (!ringtone.equals("")) {
            ringtoneURI = Uri.parse(ringtone);
        }

        // loop ringtone
        try {
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setDataSource(getApplicationContext(), ringtoneURI);
            player.setLooping(true);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return START_NOT_STICKY;
    }

    /**
     * Stops the vibration and ringtone
     */
    @Override
    public void onDestroy() {
        if (stopVibrate) {
            ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).cancel();
        }

        if (player != null && player.isPlaying()) {
            player.stop();
            player.release();
        }
    }
}
