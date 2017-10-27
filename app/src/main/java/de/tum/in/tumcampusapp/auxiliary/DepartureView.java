package de.tum.in.tumcampusapp.auxiliary;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;

import de.tum.in.tumcampusapp.R;

/**
 * Custom view that shows a departure.
 * Holds an icon of the subway public transfer line, the line name and an animated
 * automatically down counting departure time
 */
public class DepartureView extends LinearLayout {

    private final TextView mSymbolView;
    private final TextView mLineView;
    private final TextSwitcher mTimeSwitcher;
    private final Handler mHandler;
    private final ValueAnimator mValueAnimator;
    private int mCountDown;

    /**
     * Standard constructor for DepartureView
     * Uses a thin departure line
     *
     * @param context Context
     */
    public DepartureView(Context context) {
        this(context, false);
    }

    /**
     * Constructor for DepartureView
     *
     * @param context Context
     * @param big     Whether the departure should use a thin or a big line
     */
    public DepartureView(Context context, boolean big) {
        super(context);

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (big) {
            inflater.inflate(R.layout.departure_line_big, this, true);
        } else {
            inflater.inflate(R.layout.departure_line_small, this, true);
        }

        mSymbolView = findViewById(R.id.line_symbol);
        mLineView = findViewById(R.id.line_name);
        mTimeSwitcher = findViewById(R.id.line_switcher);

        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);

        // set the animation type of textSwitcher
        mTimeSwitcher.setInAnimation(in);
        mTimeSwitcher.setOutAnimation(out);

        mHandler = new Handler();

        // Set up the ValueAnimator for animateOut()
        mValueAnimator = ValueAnimator.ofInt(getHeight(), 0)
                                      .setDuration(500);
        mValueAnimator.addUpdateListener(animation -> {
            int value = (Integer) animation.getAnimatedValue();
            if (getLayoutParams() != null) {
                getLayoutParams().height = value;
                requestLayout();
                if (value == 0) {
                    setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Sets the line symbol name
     *
     * @param symbol Symbol e.g. U6, S1, T14
     */
    @SuppressWarnings("deprecation")
    public void setSymbol(String symbol, boolean highlight) {
        MVVSymbolView d = new MVVSymbolView(symbol);
        mSymbolView.setTextColor(d.getTextColor());
        mSymbolView.setText(symbol);
        mSymbolView.setBackgroundDrawable(d);

        if (highlight) {
            setBackgroundColor(0x20ffffff & d.getBackgroundColor());
        } else {
            setBackgroundColor(d.getTextColor());
        }
    }

    public String getSymbol() {
        return mSymbolView.getText()
                          .toString();
    }

    /**
     * Sets the line name
     *
     * @param line Line name e.g. Klinikum Großhadern
     */
    public void setLine(CharSequence line) {
        mLineView.setText(line);
    }

    /**
     * Sets the departure time
     *
     * @param countDown Minutes, until this line leaves
     */
    public void setTime(int countDown) {
        mCountDown = countDown;
        updateDepartureTime();
    }

    private void updateDepartureTime() {
        String text = mCountDown + " min";
        if (mCountDown >= 0) {
            mTimeSwitcher.setCurrentText(text);
        } else {
            animateOut();
            return;
        }
        // Keep countDown approximately in sync.
        if (mHandler != null) {
            mHandler.postDelayed(() -> {
                mCountDown--;
                updateDepartureTime();
            }, 60000);
        }
    }

    private void animateOut() {
        mValueAnimator.start();
    }

    /**
     * Call this, when the DepartureView isn't needed anymore.
     */
    public void removeAllCallbacksAndMessages() {
        mHandler.removeCallbacksAndMessages(null);
        mValueAnimator.cancel();
        mValueAnimator.removeAllUpdateListeners();
    }
}