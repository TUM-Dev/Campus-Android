package de.tum.in.tumcampus.auxiliary;

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

import com.nineoldandroids.animation.ValueAnimator;

import de.tum.in.tumcampus.R;

/**
 * Custom view that shows a departure.
 * Holds an icon of the subway public transfer line, the line name and an animated
 * automatically down counting departure time
 */
public class DepartureView extends LinearLayout {

    private final TextView mSymbolView;
    private final TextView mLineView;
    private final TextSwitcher mTimeSwitcher;
    private int mCountDown;
    private final Handler mHandler;

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

        mSymbolView = (TextView) findViewById(R.id.line_symbol);
        mLineView = (TextView) findViewById(R.id.line_name);
        mTimeSwitcher = (TextSwitcher) findViewById(R.id.line_switcher);

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
    public void setSymbol(String symbol) {
        MVVSymbolView d = new MVVSymbolView(symbol);
        mSymbolView.setTextColor(d.getTextColor());
        mSymbolView.setText(symbol);
        mSymbolView.setBackground(d);
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
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCountDown--;
                    updateDepartureTime();
                }
            }, 60000);
        }
    }

    private void animateOut() {
        ValueAnimator va = ValueAnimator.ofInt(getHeight(), 0);
        va.setDuration(500);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                if (getLayoutParams() != null) {
                    getLayoutParams().height = value;
                    requestLayout();
                    if (value == 0) {
                        setVisibility(View.GONE);
                    }
                }
            }
        });
        va.start();
    }
}