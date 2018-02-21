package de.tum.in.tumcampusapp.component.ui.transportation;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
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
    private final boolean big;
    private final TextView mSymbolView;
    private final TextView mLineView;
    private final TextSwitcher mTimeSwitcher;
    private final Handler mHandler;
    private ValueAnimator mValueAnimator;
    private long mDepartureTime;

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
        this.big = big;

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
            if (big) {
                setBackgroundColor(d.getBackgroundColor());
                mLineView.setTextColor(Color.WHITE);
                for (int i = 0; i < mTimeSwitcher.getChildCount(); i++) {
                    TextView tw = (TextView) mTimeSwitcher.getChildAt(i);
                    tw.setTextColor(Color.WHITE);
                }
            } else {
                setBackgroundColor(0x20ffffff & d.getBackgroundColor());
            }
        } else {
            setBackgroundColor(d.getTextColor());
            mLineView.setTextColor(Color.BLACK);

            for (int i = 0; i < mTimeSwitcher.getChildCount(); i++) {
                TextView tw = (TextView) mTimeSwitcher.getChildAt(i);
                tw.setTextColor(Color.GRAY);
            }
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
     * @param departureTime Timestamp in milliseconds, when transport leaves
     */
    public void setTime(long departureTime) {
        mDepartureTime = departureTime;
        updateDepartureTime();
    }

    private void updateDepartureTime() {
        long departureOffset = (mDepartureTime - System.currentTimeMillis()) / 1000;
        String text = String.format("%2d:%02d", departureOffset / 60, departureOffset % 60);
        if (departureOffset > 0) {
            mTimeSwitcher.setCurrentText(text);
        } else {
            animateOut();
            return;
        }
        // Keep countDown approximately in sync.
        if (mHandler != null) {
            mHandler.postDelayed(this::updateDepartureTime, 1000);
        }
    }

    private void animateOut() {
        mValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
                                      .setDuration(500);
        mValueAnimator.addUpdateListener(new SlideOutAnimator());
        mValueAnimator.setInterpolator(new AccelerateInterpolator());
        mValueAnimator.start();
    }

    /**
     * Call this, when the DepartureView isn't needed anymore.
     */
    public void removeAllCallbacksAndMessages() {
        mHandler.removeCallbacksAndMessages(null);

        if (mValueAnimator != null) {
            mValueAnimator.cancel();
            mValueAnimator.removeAllUpdateListeners();
        }
    }

    private class SlideOutAnimator implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animator) {
            float value = (Float) animator.getAnimatedValue();
            if (getLayoutParams() != null) {
                setTranslationX(value * getWidth());
                getLayoutParams().height = (int) ((1.0f - value) * getHeight());
                setAlpha(1.0f - value);
                requestLayout();
                if (value >= 1.0f) {
                    setVisibility(View.GONE);
                }
            }
        }
    }
}