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

public class DepartureView extends LinearLayout {

    private static final long UPDATE_INTERVAL = 10000;
    private final TextView mSymbolView;
    private final TextView mLineView;
    private final TextSwitcher mTimeSwitcher;
    private long mDeparture;
    private Handler mHandler;
    private long mLastDiffMin;

    public DepartureView(Context context) {
        this(context, false);
    }

    public DepartureView(Context context, boolean big) {
        super(context);

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(big)
            inflater.inflate(R.layout.departure_line_big, this, true);
        else
            inflater.inflate(R.layout.departure_line_small, this, true);

        mSymbolView = (TextView) findViewById(R.id.line_symbol);
        mLineView = (TextView) findViewById(R.id.line_name);
        mTimeSwitcher = (TextSwitcher)findViewById(R.id.line_switcher);

        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(getContext(),android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);

        // set the animation type of textSwitcher
        mTimeSwitcher.setInAnimation(in);
        mTimeSwitcher.setOutAnimation(out);

        mLastDiffMin = -1;
        mHandler = new Handler();
    }

    @SuppressWarnings("deprecation")
    public void setSymbol(String symbol) {
        MVVSymbolView d = new MVVSymbolView(getContext(), symbol);
        mSymbolView.setTextColor(d.getTextColor());
        mSymbolView.setText(symbol);
        mSymbolView.setBackgroundDrawable(d);
    }

    public void setLine(String line) {
        mLineView.setText(line);
    }

    public void setTime(long departure) {
        mDeparture = departure;
        updateDepartureTime();
    }

    private void updateDepartureTime() {
        long diff = mDeparture-System.currentTimeMillis();
        int diffMin = (int)Math.floor(diff/60000.0);
        String text = diffMin+" min";
        if(mLastDiffMin==-1 && diffMin>=0) {
            mTimeSwitcher.setCurrentText(text);
        } else if(diffMin<=-1) {
            animateOut();
            return;
        } else if(mLastDiffMin!=diffMin) {
            mTimeSwitcher.setText(text);
        }
        mLastDiffMin = diffMin;
        if(mHandler != null && diffMin>-1){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateDepartureTime();
                }
            }, UPDATE_INTERVAL);
        }
    }

    private void animateOut() {
        ValueAnimator va = ValueAnimator.ofInt(getHeight(),0);
        va.setDuration(500);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                if(getLayoutParams()!=null) {
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

    public void stopTimer(){
        if(mHandler != null){
            mHandler = null;
        }
    }
}