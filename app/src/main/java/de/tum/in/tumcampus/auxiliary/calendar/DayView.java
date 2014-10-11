/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tum.in.tumcampus.auxiliary.calendar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.widget.EdgeEffectCompat;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.style.StyleSpan;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.OverScroller;
import android.widget.ViewSwitcher;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.RoomFinderDetailsActivity;
import de.tum.in.tumcampus.auxiliary.calendar.CalendarController.EventType;
import de.tum.in.tumcampus.auxiliary.calendar.CalendarController.ViewType;

/**
 * View for multi-day view. So far only 1 and 7 day have been tested.
 */
public class DayView extends View implements View.OnCreateContextMenuListener,
        ScaleGestureDetector.OnScaleGestureListener {

    private static float mScale = 0; // Used for supporting different screen densities
    // duration of the scroll to go to a specified time
    private static final int GOTO_SCROLL_DURATION = 200;
    // duration for events' cross-fade animation
    private static final int EVENTS_CROSS_FADE_DURATION = 400;
    // duration to show the event clicked
    private static final int CLICK_DISPLAY_DURATION = 50;

    private static final int MENU_AGENDA = 2;
    private static final int MENU_DAY = 3;
    private static final int MENU_EVENT_VIEW = 5;

    private static int DEFAULT_CELL_HEIGHT = 32;
    private static int MAX_CELL_HEIGHT = 150;
    private static int MIN_Y_SPAN = 100;

    private boolean mOnFlingCalled;
    private boolean mStartingScroll = false;
    protected boolean mPaused = true;
    private Handler mHandler;

    protected Context mContext;

    private static int mHorizontalSnapBackThreshold = 128;

    private final ContinueScroll mContinueScroll = new ContinueScroll();

    // Make this visible within the package for more informative debugging
    Time mBaseDate;
    private Time mCurrentTime;
    //Update the current time line every five minutes if the window is left open that long
    private static final int UPDATE_CURRENT_TIME_DELAY = 300000;
    private final UpdateCurrentTime mUpdateCurrentTime = new UpdateCurrentTime();
    private int mTodayJulianDay;

    private final Typeface mBold = Typeface.DEFAULT_BOLD;
    private int mFirstJulianDay;
    private int mLoadedFirstJulianDay = -1;
    private int mLastJulianDay;

    private int mMonthLength;
    private int mFirstVisibleDate;
    private int mFirstVisibleDayOfWeek;
    private int[] mEarliestStartHour;    // indexed by the week day offset
    private boolean[] mHasAllDayEvent;   // indexed by the week day offset

    private Event mClickedEvent;           // The event the user clicked on
    private Event mSavedClickedEvent;
    private static int mOnDownDelay;
    private long mDownTouchTime;

    private int mEventsAlpha = 255;
    private ObjectAnimator mEventsCrossFadeAnimation;

    private final Runnable mTZUpdater = new Runnable() {
        @Override
        public void run() {
            String tz = DayUtils.getTimeZone(mContext, this);
            mBaseDate.timezone = tz;
            mBaseDate.normalize(true);
            mCurrentTime.switchTimezone(tz);
            invalidate();
        }
    };

    // Sets the "clicked" color from the clicked event
    private final Runnable mSetClick = new Runnable() {
        @Override
        public void run() {
            mClickedEvent = mSavedClickedEvent;
            mSavedClickedEvent = null;
            DayView.this.invalidate();
        }
    };

    // Clears the "clicked" color from the clicked event and launch the event
    private final Runnable mClearClick = new Runnable() {
        @Override
        public void run() {
            if (mClickedEvent != null) {
                Intent i = new Intent(getContext(), RoomFinderDetailsActivity.class);
                i.putExtra(RoomFinderDetailsActivity.EXTRA_LOCATION, mClickedEvent.location);
                getContext().startActivity(i);
            }
            mClickedEvent = null;
            DayView.this.invalidate();
        }
    };

    private final TodayAnimatorListener mTodayAnimatorListener = new TodayAnimatorListener();
    public static int mLeftBoundary = 0;
    public static int mRightBoundary = Integer.MAX_VALUE;

    class TodayAnimatorListener extends AnimatorListenerAdapter {
        private volatile Animator mAnimator = null;
        private volatile boolean mFadingIn = false;

        @Override
        public void onAnimationEnd(Animator animation) {
            synchronized (this) {
                if (mAnimator != animation) {
                    animation.removeAllListeners();
                    animation.cancel();
                    return;
                }
                if (mFadingIn) {
                    if (mTodayAnimator != null) {
                        mTodayAnimator.removeAllListeners();
                        mTodayAnimator.cancel();
                    }
                    mTodayAnimator = ObjectAnimator
                            .ofInt(DayView.this, "animateTodayAlpha", 255, 0);
                    mAnimator = mTodayAnimator;
                    mFadingIn = false;
                    mTodayAnimator.addListener(this);
                    mTodayAnimator.setDuration(600);
                    mTodayAnimator.start();
                } else {
                    mAnimateToday = false;
                    mAnimateTodayAlpha = 0;
                    mAnimator.removeAllListeners();
                    mAnimator = null;
                    mTodayAnimator = null;
                    invalidate();
                }
            }
        }

        public void setAnimator(Animator animation) {
            mAnimator = animation;
        }

        public void setFadingIn(boolean fadingIn) {
            mFadingIn = fadingIn;
        }

    }

    AnimatorListenerAdapter mAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
            mScrolling = true;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mScrolling = false;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mScrolling = false;
            resetSelectedHour();
            invalidate();
        }
    };

    /**
     * This variable helps to avoid unnecessarily reloading events by keeping
     * track of the start millis parameter used for the most recent loading
     * of events.  If the next reload matches this, then the events are not
     * reloaded.  To force a reload, set this to zero (this is set to zero
     * in the method clearCachedEvents()).
     */
    private long mLastReloadMillis;

    private ArrayList<Event> mEvents = new ArrayList<Event>();
    private StaticLayout[] mLayouts = null;
    private int mSelectionDay;        // Julian day
    private int mSelectionHour;

    /**
     * Width of a day or non-conflicting event
     */
    private int mCellWidth;

    // Pre-allocate these objects and re-use them
    private final Rect mRect = new Rect();
    private final Rect mDestRect = new Rect();
    private final Rect mSelectionRect = new Rect();

    private final Paint mPaint = new Paint();
    private final Paint mEventTextPaint = new Paint();
    private final Paint mSelectionPaint = new Paint();
    private float[] mLines;

    private int mFirstDayOfWeek; // First day of the week

    // The number of milliseconds to show the popup window

    private boolean mRemeasure = true;

    private final EventLoader mEventLoader;
    protected final EventGeometry mEventGeometry;

    private static float GRID_LINE_LEFT_MARGIN = 0;
    private static final float GRID_LINE_INNER_WIDTH = 1;

    private static final int DAY_GAP = 1;
    private static final int HOUR_GAP = 1;

    private static int HOURS_TOP_MARGIN = 2;
    private static int HOURS_LEFT_MARGIN = 2;
    private static int HOURS_RIGHT_MARGIN = 4;
    private static int HOURS_MARGIN = HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN;

    private static int CURRENT_TIME_LINE_SIDE_BUFFER = 4;
    private static int CURRENT_TIME_LINE_TOP_OFFSET = 2;

    /* package */ static final int MINUTES_PER_HOUR = 60;
    /* package */ static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * 24;
    /* package */ static final int MILLIS_PER_HOUR = (3600 * 1000);

    // More events text will transition between invisible and this alpha
    private static int DAY_HEADER_ONE_DAY_LEFT_MARGIN = 0;
    private static int DAY_HEADER_ONE_DAY_RIGHT_MARGIN = 5;
    private static int DAY_HEADER_ONE_DAY_BOTTOM_MARGIN = 6;
    private static int DAY_HEADER_RIGHT_MARGIN = 4;
    private static int DAY_HEADER_BOTTOM_MARGIN = 3;
    private static float DAY_HEADER_FONT_SIZE = 14;
    private static float DATE_HEADER_FONT_SIZE = 32;
    private static float EVENT_TEXT_FONT_SIZE = 12;
    private static float HOURS_TEXT_SIZE = 12;
    private static float AMPM_TEXT_SIZE = 9;
    private static int MIN_HOURS_WIDTH = 96;
    private static int MIN_CELL_WIDTH_FOR_TEXT = 20;
    private static final int MAX_EVENT_TEXT_LEN = 500;
    // smallest height to draw an event with
    private static float MIN_EVENT_HEIGHT = 24.0F; // in pixels
    private static int EVENT_RECT_TOP_MARGIN = 1;
    private static int EVENT_RECT_BOTTOM_MARGIN = 0;
    private static int EVENT_RECT_LEFT_MARGIN = 1;
    private static int EVENT_RECT_RIGHT_MARGIN = 0;
    private static int EVENT_RECT_STROKE_WIDTH = 2;
    private static int EVENT_TEXT_TOP_MARGIN = 2;
    private static int EVENT_TEXT_BOTTOM_MARGIN = 2;
    private static int EVENT_TEXT_LEFT_MARGIN = 6;
    private static int EVENT_TEXT_RIGHT_MARGIN = 6;

    private static final int mClickedColor = 0x8033B5E5;
    private static final int mEventTextColor = 0xffFFFFFF;

    private static final int mWeek_saturdayColor = 0x80333333;
    private static final int mWeek_sundayColor = 0x80333333;
    private static final int mCalendarDateBannerTextColor = 0xff666666;
    private static final int mCalendarAmPmLabel = 0xff999999;
    private static final int mCalendarGridLineInnerHorizontalColor = 0xFFCCCCCC;
    private static final int mCalendarGridLineInnerVerticalColor = 0xFFCCCCCC;
    private static int mFutureBgColor;
    private static final int mFutureBgColorRes = 0xffffffff;
    private static final int mBgColor = 0xffEEEEEE;
    private static final int mCalendarHourLabelColor = 0xff666666;

    private float mAnimationDistance = 0;
    private int mViewStartX;
    private int mViewStartY;
    private int mMaxViewStartY;
    private int mViewHeight;
    private int mViewWidth;
    private int mGridAreaHeight = -1;
    public static int mCellHeight = 0; // shared among all DayViews
    private static int mMinCellHeight = 32;
    private int mScrollStartY;
    private int mPreviousDirection;
    private static int mScaledPagingTouchSlop = 0;

    /**
     * Vertical distance or span between the two touch points at the start of a
     * scaling gesture
     */
    private float mStartingSpanY = 0;
    /**
     * Height of 1 hour in pixels at the start of a scaling gesture
     */
    private int mCellHeightBeforeScaleGesture;
    /**
     * The hour at the center two touch points
     */
    private float mGestureCenterHour = 0;

    private boolean mRecalCenterHour = false;

    /**
     * Flag to decide whether to handle the up event. Cases where up events
     * should be ignored are 1) right after a scale gesture and 2) finger was
     * down before app launch
     */
    private boolean mHandleActionUp = true;

    private int mHoursTextHeight;
    /**
     * The height of the day names/numbers
     */
    private static int DAY_HEADER_HEIGHT;

    protected int mNumDays = 7;
    private int mNumHours = 10;

    /**
     * Width of the time line (list of hours) to the left.
     */
    private int mHoursWidth;
    private int mDateStrWidth;
    private int mDateStrWidthLong;
    /**
     * Top of the scrollable region i.e. below date labels and all day events
     */
    private int mFirstCell;
    /**
     * First fully visible hour
     */
    private int mFirstHour = -1;
    /**
     * Distance between the mFirstCell and the top of first fully visible hour.
     */
    private int mFirstHourOffset;
    private String[] mHourStrs;
    private String[] mDayStrsLong;
    private String[] mDayStrs;
    private String[] mDayStrs2Letter;
    private boolean mIs24HourFormat;

    private final ArrayList<Event> mSelectedEvents = new ArrayList<Event>();
    private boolean mComputeSelectedEvents;
    private Event mSelectedEvent;
    protected final Resources mResources;
    protected final Drawable mCurrentTimeLine;
    protected final Drawable mCurrentTimeAnimateLine;
    protected final Drawable mTodayHeaderDrawable;
    protected Drawable mAcceptedOrTentativeEventBoxDrawable;
    private String mAmString;
    private String mPmString;
    private static int sCounter = 0;

    private final ContextMenuHandler mContextMenuHandler = new ContextMenuHandler();

    ScaleGestureDetector mScaleGestureDetector;

    /**
     * The initial state of the touch mode when we enter this view.
     */
    private static final int TOUCH_MODE_INITIAL_STATE = 0;

    /**
     * Indicates we just received the touch event and we are waiting to see if
     * it is a tap or a scroll gesture.
     */
    private static final int TOUCH_MODE_DOWN = 1;

    /**
     * Indicates the touch gesture is a vertical scroll
     */
    private static final int TOUCH_MODE_VSCROLL = 0x20;

    /**
     * Indicates the touch gesture is a horizontal scroll
     */
    private static final int TOUCH_MODE_HSCROLL = 0x40;

    private int mTouchMode = TOUCH_MODE_INITIAL_STATE;

    private boolean mScrolling = false;

    // Pixels scrolled
    private float mInitialScrollX;
    private float mInitialScrollY;

    private boolean mAnimateToday = false;
    private int mAnimateTodayAlpha = 0;

    // Animates the current time marker when Today is pressed
    ObjectAnimator mTodayAnimator;

    private final CalendarController mController;
    private final ViewSwitcher mViewSwitcher;
    private final GestureDetector mGestureDetector;
    private final OverScroller mScroller;
    private final EdgeEffectCompat mEdgeEffectTop;
    private final EdgeEffectCompat mEdgeEffectBottom;
    private boolean mCallEdgeEffectOnAbsorb;
    private final int OVERFLING_DISTANCE;
    private float mLastVelocity;

    private final ScrollInterpolator mHScrollInterpolator;

    public DayView(Context context, CalendarController controller,
                   ViewSwitcher viewSwitcher, EventLoader eventLoader, int numDays) {
        super(context);
        mContext = context;

        mResources = context.getResources();
        mNumDays = numDays;

        DATE_HEADER_FONT_SIZE = (int) mResources.getDimension(R.dimen.date_header_text_size);
        DAY_HEADER_FONT_SIZE = (int) mResources.getDimension(R.dimen.day_label_text_size);
        DAY_HEADER_HEIGHT = (int) mResources.getDimension(R.dimen.day_header_height);
        DAY_HEADER_BOTTOM_MARGIN = (int) mResources.getDimension(R.dimen.day_header_bottom_margin);
        HOURS_TEXT_SIZE = (int) mResources.getDimension(R.dimen.hours_text_size);
        AMPM_TEXT_SIZE = (int) mResources.getDimension(R.dimen.ampm_text_size);
        MIN_HOURS_WIDTH = (int) mResources.getDimension(R.dimen.min_hours_width);
        HOURS_LEFT_MARGIN = (int) mResources.getDimension(R.dimen.hours_left_margin);
        HOURS_RIGHT_MARGIN = (int) mResources.getDimension(R.dimen.hours_right_margin);
        int eventTextSizeId;
        if (mNumDays == 1) {
            eventTextSizeId = R.dimen.day_view_event_text_size;
        } else {
            eventTextSizeId = R.dimen.week_view_event_text_size;
        }
        EVENT_TEXT_FONT_SIZE = (int) mResources.getDimension(eventTextSizeId);
        MIN_EVENT_HEIGHT = mResources.getDimension(R.dimen.event_min_height);
        EVENT_TEXT_TOP_MARGIN = (int) mResources.getDimension(R.dimen.event_text_vertical_margin);
        EVENT_TEXT_BOTTOM_MARGIN = EVENT_TEXT_TOP_MARGIN;

        EVENT_TEXT_LEFT_MARGIN = (int) mResources
                .getDimension(R.dimen.event_text_horizontal_margin);
        EVENT_TEXT_RIGHT_MARGIN = EVENT_TEXT_LEFT_MARGIN;

        if (mScale == 0) {

            mScale = mResources.getDisplayMetrics().density;
            if (mScale != 1) {

                GRID_LINE_LEFT_MARGIN *= mScale;
                HOURS_TOP_MARGIN *= mScale;
                MIN_CELL_WIDTH_FOR_TEXT *= mScale;

                CURRENT_TIME_LINE_SIDE_BUFFER *= mScale;
                CURRENT_TIME_LINE_TOP_OFFSET *= mScale;

                MIN_Y_SPAN *= mScale;
                MAX_CELL_HEIGHT *= mScale;
                DEFAULT_CELL_HEIGHT *= mScale;
                DAY_HEADER_RIGHT_MARGIN *= mScale;
                DAY_HEADER_ONE_DAY_LEFT_MARGIN *= mScale;
                DAY_HEADER_ONE_DAY_RIGHT_MARGIN *= mScale;
                DAY_HEADER_ONE_DAY_BOTTOM_MARGIN *= mScale;
                EVENT_RECT_TOP_MARGIN *= mScale;
                EVENT_RECT_BOTTOM_MARGIN *= mScale;
                EVENT_RECT_LEFT_MARGIN *= mScale;
                EVENT_RECT_RIGHT_MARGIN *= mScale;
                EVENT_RECT_STROKE_WIDTH *= mScale;
            }
        }
        HOURS_MARGIN = HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN;

        mCurrentTimeLine = mResources.getDrawable(R.drawable.timeline_indicator_holo_light);
        mCurrentTimeAnimateLine = mResources
                .getDrawable(R.drawable.timeline_indicator_activated_holo_light);
        mTodayHeaderDrawable = mResources.getDrawable(R.drawable.today_blue_week_holo_light);
        mAcceptedOrTentativeEventBoxDrawable = mResources
                .getDrawable(R.drawable.panel_month_event_holo_light);

        mEventLoader = eventLoader;
        mEventGeometry = new EventGeometry();
        mEventGeometry.setMinEventHeight(MIN_EVENT_HEIGHT);
        mEventGeometry.setHourGap(HOUR_GAP);
        mEventGeometry.setCellMargin(DAY_GAP);
        mController = controller;
        mViewSwitcher = viewSwitcher;
        mGestureDetector = new GestureDetector(context, new CalendarGestureListener());
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), this);
        if(mCellHeight==0) {
            mCellHeight = DEFAULT_CELL_HEIGHT;
        }
        mScroller = new OverScroller(context);
        mHScrollInterpolator = new ScrollInterpolator();
        mEdgeEffectTop = new EdgeEffectCompat(context);
        mEdgeEffectBottom = new EdgeEffectCompat(context);
        ViewConfiguration vc = ViewConfiguration.get(context);
        mScaledPagingTouchSlop = vc.getScaledPagingTouchSlop();
        mOnDownDelay = ViewConfiguration.getTapTimeout();
        OVERFLING_DISTANCE = vc.getScaledOverflingDistance();

        init(context);
    }

    @Override
    protected void onAttachedToWindow() {
        if (mHandler == null) {
            mHandler = getHandler();
            mHandler.post(mUpdateCurrentTime);
        }
    }

    private void init(Context context) {
        setFocusable(true);

        // Allow focus in touch mode so that we can do keyboard shortcuts
        // even after we've entered touch mode.
        setFocusableInTouchMode(true);
        setClickable(true);
        setOnCreateContextMenuListener(this);

        mFirstDayOfWeek = Time.MONDAY;

        mCurrentTime = new Time(DayUtils.getTimeZone(context, mTZUpdater));
        long currentTime = System.currentTimeMillis();
        mCurrentTime.set(currentTime);
        mTodayJulianDay = Time.getJulianDay(currentTime, mCurrentTime.gmtoff);

        mEventTextPaint.setTextSize(EVENT_TEXT_FONT_SIZE);
        mEventTextPaint.setTextAlign(Align.LEFT);
        mEventTextPaint.setAntiAlias(true);

        int gridLineColor = 0xff707070;
        Paint p = mSelectionPaint;
        p.setColor(gridLineColor);
        p.setStyle(Style.FILL);
        p.setAntiAlias(false);

        p = mPaint;
        p.setAntiAlias(true);

        // Long day names
        mDayStrsLong = new String[14];

        // Allocate space for 2 weeks worth of weekday names so that we can
        // easily start the week display at any week day.
        mDayStrs = new String[14];

        // Also create an array of 2-letter abbreviations.
        mDayStrs2Letter = new String[14];

        for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
            int index = i - Calendar.SUNDAY;

            mDayStrsLong[index] = DateUtils.getDayOfWeekString(i, DateUtils.LENGTH_LONG)
                    .toUpperCase();
            mDayStrsLong[index + 7] = mDayStrsLong[index];
            // e.g. Tue for Tuesday
            mDayStrs[index] = DateUtils.getDayOfWeekString(i, DateUtils.LENGTH_MEDIUM)
                    .toUpperCase();
            mDayStrs[index + 7] = mDayStrs[index];
            // e.g. Tu for Tuesday
            mDayStrs2Letter[index] = DateUtils.getDayOfWeekString(i, DateUtils.LENGTH_SHORT)
                    .toUpperCase();

            // If we don't have 2-letter day strings, fall back to 1-letter.
            if (mDayStrs2Letter[index].equals(mDayStrs[index])) {
                mDayStrs2Letter[index] = DateUtils.getDayOfWeekString(i, DateUtils.LENGTH_SHORTEST);
            }

            mDayStrs2Letter[index + 7] = mDayStrs2Letter[index];
        }

        // Figure out how much space we need for the 3-letter abbrev names
        // in the worst case.
        p.setTextSize(DATE_HEADER_FONT_SIZE);
        p.setTypeface(mBold);
        String[] dateStrs = {" 28", " 30"};
        mDateStrWidth = computeMaxStringWidth(0, dateStrs, p);
        Time time = new Time();
        time.setJulianDay(mFirstJulianDay);
        String s = SimpleDateFormat.getDateInstance().format(new Date(time.toMillis(false)));
        mDateStrWidthLong = computeMaxStringWidth(0, new String[] {s}, p);
        p.setTextSize(DAY_HEADER_FONT_SIZE);
        mDateStrWidth += computeMaxStringWidth(0, mDayStrs, p);
        mDateStrWidthLong += computeMaxStringWidth(0, mDayStrsLong, p);

        p.setTextSize(HOURS_TEXT_SIZE);
        p.setTypeface(null);
        handleOnResume();

        mAmString = DateUtils.getAMPMString(Calendar.AM).toUpperCase();
        mPmString = DateUtils.getAMPMString(Calendar.PM).toUpperCase();
        String[] ampm = {mAmString, mPmString};
        p.setTextSize(AMPM_TEXT_SIZE);
        mHoursWidth = Math.max(HOURS_MARGIN, computeMaxStringWidth(mHoursWidth, ampm, p)
                + HOURS_RIGHT_MARGIN);
        mHoursWidth = Math.max(MIN_HOURS_WIDTH, mHoursWidth);

        LayoutInflater inflater;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resources.Theme dialogTheme = getResources().newTheme();
        dialogTheme.applyStyle(android.R.style.Theme_Dialog, true);
        TypedArray ta = dialogTheme.obtainStyledAttributes(new int[]{
                android.R.attr.windowBackground});

        // Catch long clicks for creating a new event
        mBaseDate = new Time(DayUtils.getTimeZone(context, mTZUpdater));
        long millis = System.currentTimeMillis();
        mBaseDate.set(millis);

        mEarliestStartHour = new int[mNumDays];
        mHasAllDayEvent = new boolean[mNumDays];

        // mLines is the array of points used with Canvas.drawLines() in
        // drawGridBackground() and drawAllDayEvents().  Its size depends
        // on the max number of lines that can ever be drawn by any single
        // drawLines() call in either of those methods.
        final int maxGridLines = (24 + 1)  // max horizontal lines we might draw
                + (mNumDays + 1); // max vertical lines we might draw
        mLines = new float[maxGridLines * 4];
    }

    static final String[] s12HoursNoAmPm = { "12", "1", "2", "3", "4",
            "5", "6", "7", "8", "9", "10", "11", "12",
            "1", "2", "3", "4", "5", "6", "7", "8",
            "9", "10", "11", "12" };

    static final String[] s24Hours = {"00", "01", "02", "03", "04", "05",
            "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16",
            "17", "18", "19", "20", "21", "22", "23", "00"};

    public void handleOnResume() {
        mFutureBgColor = mFutureBgColorRes;
        mIs24HourFormat = DateFormat.is24HourFormat(mContext);
        mHourStrs = mIs24HourFormat ? s24Hours : s12HoursNoAmPm;
    }

    /**
     * Returns the start of the selected time in milliseconds since the epoch.
     *
     * @return selected time in UTC milliseconds since the epoch.
     */
    public long getSelectedTimeInMillis() {
        Time time = new Time(mBaseDate);
        time.setJulianDay(mSelectionDay);
        time.hour = mSelectionHour;

        // We ignore the "isDst" field because we want normalize() to figure
        // out the correct DST value and not adjust the selected time based
        // on the current setting of DST.
        return time.normalize(true /* ignore isDst */);
    }

    Time getSelectedTime() {
        Time time = new Time(mBaseDate);
        time.setJulianDay(mSelectionDay);
        time.hour = mSelectionHour;

        // We ignore the "isDst" field because we want normalize() to figure
        // out the correct DST value and not adjust the selected time based
        // on the current setting of DST.
        time.normalize(true /* ignore isDst */);
        return time;
    }

    /**
     * Returns the start of the selected time in minutes since midnight,
     * local time.  The derived class must ensure that this is consistent
     * with the return value from getSelectedTimeInMillis().
     */
    int getSelectedMinutesSinceMidnight() {
        return mSelectionHour * MINUTES_PER_HOUR;
    }

    public int getFirstVisibleHour() {
        return mFirstHour;
    }

    public void setFirstVisibleHour(int firstHour) {
        mFirstHour = firstHour;
        mFirstHourOffset = 0;
    }

    public void setSelected(Time time, boolean ignoreTime, boolean animateToday) {
        mBaseDate.set(time);
        setSelectedHour(mBaseDate.hour);
        setSelectedEvent(null);
        long millis = mBaseDate.toMillis(false /* use isDst */);
        setSelectedDay(Time.getJulianDay(millis, mBaseDate.gmtoff));
        mSelectedEvents.clear();
        mComputeSelectedEvents = true;

        int gotoY = Integer.MIN_VALUE;

        if (!ignoreTime && mGridAreaHeight != -1) {
            int lastHour = 0;

            if (mBaseDate.hour < mFirstHour) {
                // Above visible region
                gotoY = mBaseDate.hour * (mCellHeight + HOUR_GAP);
            } else {
                lastHour = (mGridAreaHeight - mFirstHourOffset) / (mCellHeight + HOUR_GAP)
                        + mFirstHour;

                if (mBaseDate.hour >= lastHour) {
                    // Below visible region

                    // target hour + 1 (to give it room to see the event) -
                    // grid height (to get the y of the top of the visible
                    // region)
                    gotoY = (int) ((mBaseDate.hour + 1 + mBaseDate.minute / 60.0f)
                            * (mCellHeight + HOUR_GAP) - mGridAreaHeight);
                }
            }

            if (gotoY > mMaxViewStartY) {
                gotoY = mMaxViewStartY;
            } else if (gotoY < 0 && gotoY != Integer.MIN_VALUE) {
                gotoY = 0;
            }
        }

        recalc();

        mRemeasure = true;
        invalidate();

        boolean delayAnimateToday = false;
        if (gotoY != Integer.MIN_VALUE) {
            ValueAnimator scrollAnim = ObjectAnimator.ofInt(this, "viewStartY", mViewStartY, gotoY);
            scrollAnim.setDuration(GOTO_SCROLL_DURATION);
            scrollAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            scrollAnim.addListener(mAnimatorListener);
            scrollAnim.start();
            delayAnimateToday = true;
        }
        if (animateToday) {
            synchronized (mTodayAnimatorListener) {
                if (mTodayAnimator != null) {
                    mTodayAnimator.removeAllListeners();
                    mTodayAnimator.cancel();
                }
                mTodayAnimator = ObjectAnimator.ofInt(this, "animateTodayAlpha",
                        mAnimateTodayAlpha, 255);
                mAnimateToday = true;
                mTodayAnimatorListener.setFadingIn(true);
                mTodayAnimatorListener.setAnimator(mTodayAnimator);
                mTodayAnimator.addListener(mTodayAnimatorListener);
                mTodayAnimator.setDuration(150);
                if (delayAnimateToday) {
                    mTodayAnimator.setStartDelay(GOTO_SCROLL_DURATION);
                }
                mTodayAnimator.start();
            }
        }
    }

    // Called from animation framework via reflection. Do not remove
    public void setViewStartY(int viewStartY) {
        if (viewStartY > mMaxViewStartY) {
            viewStartY = mMaxViewStartY;
        }

        mViewStartY = viewStartY;

        computeFirstHour();
        invalidate();
    }

    public void setAnimateTodayAlpha(int todayAlpha) {
        mAnimateTodayAlpha = todayAlpha;
        invalidate();
    }

    public Time getSelectedDay() {
        Time time = new Time(mBaseDate);
        time.setJulianDay(mSelectionDay);
        time.hour = mSelectionHour;

        // We ignore the "isDst" field because we want normalize() to figure
        // out the correct DST value and not adjust the selected time based
        // on the current setting of DST.
        time.normalize(true /* ignore isDst */);
        return time;
    }

    public void updateTitle() {
        Time start = new Time(mBaseDate);
        start.normalize(true);
        Time end = new Time(start);
        end.monthDay += mNumDays - 1;
        // Move it forward one minute so the formatter doesn't lose a day
        end.minute += 1;
        end.normalize(true);

        long formatFlags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
        if (mNumDays != 1) {
            // Don't show day of the month if for multi-day view
            formatFlags |= DateUtils.FORMAT_NO_MONTH_DAY;

            // Abbreviate the month if showing multiple months
            if (start.month != end.month) {
                formatFlags |= DateUtils.FORMAT_ABBREV_MONTH;
            }
        }

        mController.sendEvent(this, EventType.UPDATE_TITLE, start, end, null, -1, ViewType.CURRENT,
                formatFlags, null, null);
    }

    /**
     * return a negative number if "time" is comes before the visible time
     * range, a positive number if "time" is after the visible time range, and 0
     * if it is in the visible time range.
     */
    public int compareToVisibleTimeRange(Time time) {

        int savedHour = mBaseDate.hour;
        int savedMinute = mBaseDate.minute;
        int savedSec = mBaseDate.second;

        mBaseDate.hour = 0;
        mBaseDate.minute = 0;
        mBaseDate.second = 0;

        // Compare beginning of range
        int diff = Time.compare(time, mBaseDate);
        if (diff > 0) {
            // Compare end of range
            mBaseDate.monthDay += mNumDays;
            mBaseDate.normalize(true);
            diff = Time.compare(time, mBaseDate);

            mBaseDate.monthDay -= mNumDays;
            mBaseDate.normalize(true);
            if (diff < 0) {
                // in visible time
                diff = 0;
            } else if (diff == 0) {
                // Midnight of following day
                diff = 1;
            }
        }

        mBaseDate.hour = savedHour;
        mBaseDate.minute = savedMinute;
        mBaseDate.second = savedSec;
        return diff;
    }

    private void recalc() {
        // Set the base date to the beginning of the week if we are displaying
        // 7 days at a time.
        if (mNumDays == 7) {
            adjustToBeginningOfWeek(mBaseDate);
        }

        final long start = mBaseDate.toMillis(false /* use isDst */);
        mFirstJulianDay = Time.getJulianDay(start, mBaseDate.gmtoff);
        mLastJulianDay = mFirstJulianDay + mNumDays - 1;

        mMonthLength = mBaseDate.getActualMaximum(Time.MONTH_DAY);
        mFirstVisibleDate = mBaseDate.monthDay;
        mFirstVisibleDayOfWeek = mBaseDate.weekDay;
    }

    private void adjustToBeginningOfWeek(Time time) {
        int dayOfWeek = time.weekDay;
        int diff = dayOfWeek - mFirstDayOfWeek;
        if (diff != 0) {
            if (diff < 0) {
                diff += 7;
            }
            time.monthDay -= diff;
            time.normalize(true /* ignore isDst */);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        mViewWidth = width;
        mViewHeight = height;
        mEdgeEffectTop.setSize(mViewWidth, mViewHeight);
        mEdgeEffectBottom.setSize(mViewWidth, mViewHeight);
        int gridAreaWidth = width - mHoursWidth;
        mCellWidth = (gridAreaWidth - (mNumDays * DAY_GAP)) / mNumDays;

        // This would be about 1 day worth in a 7 day view
        mHorizontalSnapBackThreshold = width / 7;

        Paint p = new Paint();
        p.setTextSize(HOURS_TEXT_SIZE);
        mHoursTextHeight = (int) Math.abs(p.ascent());
        remeasure(width, height);
    }

    /**
     * Measures the space needed for various parts of the view after
     * loading new events.  This can change if there are all-day events.
     */
    private void remeasure(int width, int height) {
        // First, clear the array of earliest start times, and the array
        // indicating presence of an all-day event.
        for (int day = 0; day < mNumDays; day++) {
            mEarliestStartHour[day] = 25;  // some big number
            mHasAllDayEvent[day] = false;
        }

        // The min is where 24 hours cover the entire visible area
        mMinCellHeight = Math.max((height - DAY_HEADER_HEIGHT) / 24, (int) MIN_EVENT_HEIGHT);
        if (mCellHeight < mMinCellHeight) {
            mCellHeight = mMinCellHeight;
        }

        // Calculate mAllDayHeight
        mFirstCell = DAY_HEADER_HEIGHT;

        mGridAreaHeight = height - mFirstCell;

        mNumHours = mGridAreaHeight / (mCellHeight + HOUR_GAP);
        mEventGeometry.setHourHeight(mCellHeight);

        final long minimumDurationMillis = (long)
                (MIN_EVENT_HEIGHT * DateUtils.MINUTE_IN_MILLIS / (mCellHeight / 60.0f));
        Event.computePositions(mEvents, minimumDurationMillis);

        // Compute the top of our reachable view
        mMaxViewStartY = HOUR_GAP + 24 * (mCellHeight + HOUR_GAP) - mGridAreaHeight;

        if (mViewStartY > mMaxViewStartY) {
            mViewStartY = mMaxViewStartY;
            computeFirstHour();
        }

        if (mFirstHour == -1) {
            initFirstHour();
            mFirstHourOffset = 0;
        }

        // When we change the base date, the number of all-day events may
        // change and that changes the cell height.  When we switch dates,
        // we use the mFirstHourOffset from the previous view, but that may
        // be too large for the new view if the cell height is smaller.
        if (mFirstHourOffset >= mCellHeight + HOUR_GAP) {
            mFirstHourOffset = mCellHeight + HOUR_GAP - 1;
        }
        mViewStartY = mFirstHour * (mCellHeight + HOUR_GAP) - mFirstHourOffset;
    }

    /**
     * Initialize the state for another view.  The given view is one that has
     * its own bitmap and will use an animation to replace the current view.
     * The current view and new view are either both Week views or both Day
     * views.  They differ in their base date.
     *
     * @param view the view to initialize.
     */
    private void initView(DayView view) {
        view.setSelectedHour(mSelectionHour);
        view.mSelectedEvents.clear();
        view.mComputeSelectedEvents = true;
        view.mFirstHour = mFirstHour;
        view.mFirstHourOffset = mFirstHourOffset;
        view.remeasure(getWidth(), getHeight());

        view.setSelectedEvent(null);
        view.mFirstDayOfWeek = mFirstDayOfWeek;

        // Redraw the screen so that the selection box will be redrawn.  We may
        // have scrolled to a different part of the day in some other view
        // so the selection box in this view may no longer be visible.
        view.recalc();
    }

    private class GotoBroadcaster implements Animation.AnimationListener {
        private final int mCounter;
        private final Time mStart;
        private final Time mEnd;

        public GotoBroadcaster(Time start, Time end) {
            mCounter = ++sCounter;
            mStart = start;
            mEnd = end;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            DayView view = (DayView) mViewSwitcher.getCurrentView();
            view.mViewStartX = 0;
            view = (DayView) mViewSwitcher.getNextView();
            view.mViewStartX = 0;

            if (mCounter == sCounter) {
                mController.sendEvent(this, EventType.GO_TO, mStart, mEnd, null, -1,
                        ViewType.CURRENT, CalendarController.EXTRA_GOTO_DATE, null, null);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }

    private View switchViews(boolean forward, float xOffSet, float width, float velocity) {
        mAnimationDistance = width - xOffSet;

        float progress = Math.abs(xOffSet) / width;
        if (progress > 1.0f) {
            progress = 1.0f;
        }

        float inFromXValue, inToXValue;
        float outFromXValue, outToXValue;
        if (forward) {
            inFromXValue = 1.0f - progress;
            inToXValue = 0.0f;
            outFromXValue = -progress;
            outToXValue = -1.0f;
        } else {
            inFromXValue = progress - 1.0f;
            inToXValue = 0.0f;
            outFromXValue = progress;
            outToXValue = 1.0f;
        }

        final Time start = new Time(mBaseDate.timezone);
        start.set(mController.getTime());
        if (forward) {
            start.monthDay += mNumDays;
        } else {
            start.monthDay -= mNumDays;
        }
        mController.setTime(start.normalize(true));

        Time newSelected = start;

        if (mNumDays == 7) {
            newSelected = new Time(start);
            adjustToBeginningOfWeek(start);
        }

        final Time end = new Time(start);
        end.monthDay += mNumDays - 1;

        // We have to allocate these animation objects each time we switch views
        // because that is the only way to set the animation parameters.
        TranslateAnimation inAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, inFromXValue,
                Animation.RELATIVE_TO_SELF, inToXValue,
                Animation.ABSOLUTE, 0.0f,
                Animation.ABSOLUTE, 0.0f);

        TranslateAnimation outAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, outFromXValue,
                Animation.RELATIVE_TO_SELF, outToXValue,
                Animation.ABSOLUTE, 0.0f,
                Animation.ABSOLUTE, 0.0f);

        long duration = calculateDuration(width - Math.abs(xOffSet), width, velocity);
        inAnimation.setDuration(duration);
        inAnimation.setInterpolator(mHScrollInterpolator);
        outAnimation.setInterpolator(mHScrollInterpolator);
        outAnimation.setDuration(duration);
        outAnimation.setAnimationListener(new GotoBroadcaster(start, end));

        mViewSwitcher.setInAnimation(inAnimation);
        mViewSwitcher.setOutAnimation(outAnimation);

        DayView view = (DayView) mViewSwitcher.getCurrentView();
        view.cleanup();
        mViewSwitcher.showNext();
        view = (DayView) mViewSwitcher.getCurrentView();
        view.setSelected(newSelected, true, false);
        view.requestFocus();
        view.reloadEvents();
        view.updateTitle();
        view.restartCurrentTimeUpdates();

        return view;
    }

    // This is called after scrolling stops to move the selected hour
    // to the visible part of the screen.
    private void resetSelectedHour() {
        if (mSelectionHour < mFirstHour + 1) {
            setSelectedHour(mFirstHour + 1);
            setSelectedEvent(null);
            mSelectedEvents.clear();
            mComputeSelectedEvents = true;
        } else if (mSelectionHour > mFirstHour + mNumHours - 3) {
            setSelectedHour(mFirstHour + mNumHours - 3);
            setSelectedEvent(null);
            mSelectedEvents.clear();
            mComputeSelectedEvents = true;
        }
    }

    private void initFirstHour() {
        mFirstHour = mSelectionHour - mNumHours / 5;
        if (mFirstHour < 0) {
            mFirstHour = 0;
        } else if (mFirstHour + mNumHours > 24) {
            mFirstHour = 24 - mNumHours;
        }
    }

    /**
     * Recomputes the first full hour that is visible on screen after the
     * screen is scrolled.
     */
    private void computeFirstHour() {
        // Compute the first full hour that is visible on screen
        mFirstHour = (mViewStartY + mCellHeight + HOUR_GAP - 1) / (mCellHeight + HOUR_GAP);
        mFirstHourOffset = mFirstHour * (mCellHeight + HOUR_GAP) - mViewStartY;
    }

    public void clearCachedEvents() {
        mLastReloadMillis = 0;
    }

    private final Runnable mCancelCallback = new Runnable() {
        public void run() {
            clearCachedEvents();
        }
    };

    /* package */
    public void reloadEvents() {
        // Protect against this being called before this view has been
        // initialized.
        if (mContext == null) {
            return;
        }

        // Make sure our time zones are up to date
        mTZUpdater.run();

        setSelectedEvent(null);
        mSelectedEvents.clear();

        // The start date is the beginning of the week at 12am
        Time weekStart = new Time(DayUtils.getTimeZone(mContext, mTZUpdater));
        weekStart.set(mBaseDate);
        weekStart.hour = 0;
        weekStart.minute = 0;
        weekStart.second = 0;
        long millis = weekStart.normalize(true /* ignore isDst */);

        // Avoid reloading events unnecessarily.
        if (millis == mLastReloadMillis) {
            return;
        }
        mLastReloadMillis = millis;

        // load events in the background
        final ArrayList<Event> events = new ArrayList<Event>();
        mEventLoader.loadEventsInBackground(mNumDays, events, mFirstJulianDay, new Runnable() {

            public void run() {
                boolean fadeinEvents = mFirstJulianDay != mLoadedFirstJulianDay;
                mEvents = events;
                mLoadedFirstJulianDay = mFirstJulianDay;

                // New events, new layouts
                if (mLayouts == null || mLayouts.length < events.size()) {
                    mLayouts = new StaticLayout[events.size()];
                } else {
                    Arrays.fill(mLayouts, null);
                }

                computeEventRelations();

                mRemeasure = true;
                mComputeSelectedEvents = true;
                recalc();

                // Start animation to cross fade the events
                if (fadeinEvents) {
                    if (mEventsCrossFadeAnimation == null) {
                        mEventsCrossFadeAnimation = ObjectAnimator.ofInt(DayView.this, "EventsAlpha", 0, 255);
                        mEventsCrossFadeAnimation.setDuration(EVENTS_CROSS_FADE_DURATION);
                    }
                    mEventsCrossFadeAnimation.start();
                } else {
                    invalidate();
                }
            }
        }, mCancelCallback);
    }

    public void setEventsAlpha(int alpha) {
        mEventsAlpha = alpha;
        invalidate();
    }

    public int getEventsAlpha() {
        return mEventsAlpha;
    }

    public void stopEventsAnimation() {
        if (mEventsCrossFadeAnimation != null) {
            mEventsCrossFadeAnimation.cancel();
        }
        mEventsAlpha = 255;
    }

    private void computeEventRelations() {
        // Compute the layout relation between each event before measuring cell
        // width, as the cell width should be adjusted along with the relation.
        //
        // Examples: A (1:00pm - 1:01pm), B (1:02pm - 2:00pm)
        // We should mark them as "overwapped". Though they are not overwapped logically, but
        // minimum cell height implicitly expands the cell height of A and it should look like
        // (1:00pm - 1:15pm) after the cell height adjustment.

        // Compute the space needed for the all-day events, if any.
        // Make a pass over all the events, and keep track of the maximum
        // number of all-day events in any one day.  Also, keep track of
        // the earliest event in each day.
        final ArrayList<Event> events = mEvents;
        final int len = events.size();
        // Num of all-day-events on each day.
        final int eventsCount[] = new int[mLastJulianDay - mFirstJulianDay + 1];
        Arrays.fill(eventsCount, 0);
        for (int ii = 0; ii < len; ii++) {
            Event event = events.get(ii);
            if (event.startDay > mLastJulianDay || event.endDay < mFirstJulianDay) {
                continue;
            }
            int daynum = event.startDay - mFirstJulianDay;
            int hour = event.startTime / 60;
            if (daynum >= 0 && hour < mEarliestStartHour[daynum]) {
                mEarliestStartHour[daynum] = hour;
            }

            // Also check the end hour in case the event spans more than
            // one day.
            daynum = event.endDay - mFirstJulianDay;
            hour = event.endTime / 60;
            if (daynum < mNumDays && hour < mEarliestStartHour[daynum]) {
                mEarliestStartHour[daynum] = hour;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRemeasure) {
            remeasure(getWidth(), getHeight());
            mRemeasure = false;
        }
        canvas.save();

        float yTranslate = -mViewStartY + DAY_HEADER_HEIGHT;
        // offset canvas by the current drag and header position
        canvas.translate(-mViewStartX, yTranslate);
        // clip to everything below the allDay area
        Rect dest = mDestRect;
        dest.top = (int) (mFirstCell - yTranslate);
        dest.bottom = (int) (mViewHeight - yTranslate);
        dest.left = 0;
        dest.right = mViewWidth;
        canvas.save();
        canvas.clipRect(dest);
        // Draw the movable part of the view
        doDraw(canvas);
        // restore to having no clip
        canvas.restore();

        if ((mTouchMode & TOUCH_MODE_HSCROLL) != 0) {
            float xTranslate;
            if (mViewStartX > 0) {
                xTranslate = mViewWidth;
            } else {
                xTranslate = -mViewWidth;
            }
            // Move the canvas around to prep it for the next view
            // specifically, shift it by a screen and undo the
            // yTranslation which will be redone in the nextView's onDraw().
            canvas.translate(xTranslate, -yTranslate);
            DayView nextView = (DayView) mViewSwitcher.getNextView();

            // Prevent infinite recursive calls to onDraw().
            nextView.mTouchMode = TOUCH_MODE_INITIAL_STATE;

            nextView.onDraw(canvas);
            // Move it back for this view
            canvas.translate(-xTranslate, 0);
        } else {
            // If we drew another view we already translated it back
            // If we didn't draw another view we should be at the edge of the
            // screen
            canvas.translate(mViewStartX, -yTranslate);
        }

        // Draw the fixed areas (that don't scroll) directly to the canvas.
        drawAfterScroll(canvas);
        mComputeSelectedEvents = false;

        // Draw overscroll glow
        if (!mEdgeEffectTop.isFinished()) {
            if (DAY_HEADER_HEIGHT != 0) {
                canvas.translate(0, DAY_HEADER_HEIGHT);
            }
            if (mEdgeEffectTop.draw(canvas)) {
                invalidate();
            }
            if (DAY_HEADER_HEIGHT != 0) {
                canvas.translate(0, -DAY_HEADER_HEIGHT);
            }
        }
        if (!mEdgeEffectBottom.isFinished()) {
            canvas.rotate(180, mViewWidth / 2, mViewHeight / 2);
            if (mEdgeEffectBottom.draw(canvas)) {
                invalidate();
            }
        }
        canvas.restore();
    }

    private void drawAfterScroll(Canvas canvas) {
        Paint p = mPaint;
        Rect r = mRect;

        drawScrollLine(r, canvas, p);
        drawDayHeaderLoop(r, canvas, p);

        // Draw the AM and PM indicators if we're in 12 hour mode
        if (!mIs24HourFormat) {
            drawAmPm(canvas, p);
        }
    }

    private void drawScrollLine(Rect r, Canvas canvas, Paint p) {
        final int right = computeDayLeftPosition(mNumDays);
        final int y = mFirstCell - 1;

        p.setAntiAlias(false);
        p.setStyle(Style.FILL);

        p.setColor(mCalendarGridLineInnerHorizontalColor);
        p.setStrokeWidth(GRID_LINE_INNER_WIDTH);
        canvas.drawLine(GRID_LINE_LEFT_MARGIN, y, right, y, p);
        p.setAntiAlias(true);
    }

    // Computes the x position for the left side of the given day (base 0)
    private int computeDayLeftPosition(int day) {
        int effectiveWidth = mViewWidth - mHoursWidth;
        return day * effectiveWidth / mNumDays + mHoursWidth;
    }

    private void drawDayHeaderLoop(Rect r, Canvas canvas, Paint p) {
        p.setTypeface(mBold);
        p.setTextAlign(Align.RIGHT);
        int cell = mFirstJulianDay;

        String[] dayNames;
        if (mDateStrWidthLong < mCellWidth && mNumDays==1) {
            dayNames = mDayStrsLong;
        } else if(mDateStrWidth < mCellWidth) {
            dayNames = mDayStrs;
        } else {
            dayNames = mDayStrs2Letter;
        }

        p.setAntiAlias(true);
        for (int day = 0; day < mNumDays; day++, cell++) {
            int dayOfWeek = day + mFirstVisibleDayOfWeek;
            if (dayOfWeek >= 14) {
                dayOfWeek -= 14;
            }

            int color = mCalendarDateBannerTextColor;
            if (mNumDays == 1) {
                if (dayOfWeek == Time.SATURDAY) {
                    color = mWeek_saturdayColor;
                } else if (dayOfWeek == Time.SUNDAY) {
                    color = mWeek_sundayColor;
                }
            } else {
                final int column = day % 7;
                if (DayUtils.isSaturday(column, mFirstDayOfWeek)) {
                    color = mWeek_saturdayColor;
                } else if (DayUtils.isSunday(column, mFirstDayOfWeek)) {
                    color = mWeek_sundayColor;
                }
            }

            p.setColor(color);
            if(mNumDays==1) {
                Time time = new Time();
                time.setJulianDay(mFirstJulianDay);
                String s = SimpleDateFormat.getDateInstance().format(new Date(time.toMillis(false)));
                drawDayHeader(dayNames[dayOfWeek], day, s, canvas, p);
            } else {
                int dateNum = mFirstVisibleDate + day;
                if (dateNum > mMonthLength) {
                    dateNum -= mMonthLength;
                }
                drawDayHeader(dayNames[dayOfWeek], day, String.valueOf(dateNum), canvas, p);
            }
        }
        p.setTypeface(null);
    }

    private void drawAmPm(Canvas canvas, Paint p) {
        p.setColor(mCalendarAmPmLabel);
        p.setTextSize(AMPM_TEXT_SIZE);
        p.setTypeface(mBold);
        p.setAntiAlias(true);
        p.setTextAlign(Align.RIGHT);
        String text = mAmString;
        if (mFirstHour >= 12) {
            text = mPmString;
        }
        int y = mFirstCell + mFirstHourOffset + 2 * mHoursTextHeight + HOUR_GAP;
        canvas.drawText(text, HOURS_LEFT_MARGIN, y, p);

        if (mFirstHour < 12 && mFirstHour + mNumHours > 12) {
            // Also draw the "PM"
            text = mPmString;
            y = mFirstCell + mFirstHourOffset + (12 - mFirstHour) * (mCellHeight + HOUR_GAP)
                    + 2 * mHoursTextHeight + HOUR_GAP;
            canvas.drawText(text, HOURS_LEFT_MARGIN, y, p);
        }
    }

    private void drawCurrentTimeLine(Rect r, final int day, final int top, Canvas canvas,
                                     Paint p) {
        r.left = computeDayLeftPosition(day) - CURRENT_TIME_LINE_SIDE_BUFFER + 1;
        r.right = computeDayLeftPosition(day + 1) + CURRENT_TIME_LINE_SIDE_BUFFER + 1;

        r.top = top - CURRENT_TIME_LINE_TOP_OFFSET;
        r.bottom = r.top + mCurrentTimeLine.getIntrinsicHeight();

        mCurrentTimeLine.setBounds(r);
        mCurrentTimeLine.draw(canvas);
        if (mAnimateToday) {
            mCurrentTimeAnimateLine.setBounds(r);
            mCurrentTimeAnimateLine.setAlpha(mAnimateTodayAlpha);
            mCurrentTimeAnimateLine.draw(canvas);
        }
    }

    private void doDraw(Canvas canvas) {
        Paint p = mPaint;
        Rect r = mRect;

        if (mFutureBgColor != 0) {
            drawBgColors(r, canvas, p);
        }
        drawGridBackground(r, canvas, p);
        drawHours(r, canvas, p);

        // Draw each day
        int cell = mFirstJulianDay;
        p.setAntiAlias(false);
        int alpha = p.getAlpha();
        p.setAlpha(mEventsAlpha);
        for (int day = 0; day < mNumDays; day++, cell++) {

            // events on every call.
            drawEvents(cell, day, HOUR_GAP, canvas, p);
            // If this is today
            if (cell == mTodayJulianDay) {
                int lineY = mCurrentTime.hour * (mCellHeight + HOUR_GAP)
                        + ((mCurrentTime.minute * mCellHeight) / 60) + 1;

                // And the current time shows up somewhere on the screen
                if (lineY >= mViewStartY && lineY < mViewStartY + mViewHeight - 2) {
                    drawCurrentTimeLine(r, day, lineY, canvas, p);
                }
            }
        }
        p.setAntiAlias(true);
        p.setAlpha(alpha);

        // drawSelectedRect(r, canvas, p);
    }

    private void drawHours(Rect r, Canvas canvas, Paint p) {
        setupHourTextPaint(p);

        int y = HOUR_GAP + mHoursTextHeight + HOURS_TOP_MARGIN;

        for (int i = 0; i < 24; i++) {
            String time = mHourStrs[i];
            canvas.drawText(time, HOURS_LEFT_MARGIN, y, p);
            y += mCellHeight + HOUR_GAP;
        }
    }

    private void setupHourTextPaint(Paint p) {
        p.setColor(mCalendarHourLabelColor);
        p.setTextSize(HOURS_TEXT_SIZE);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextAlign(Align.RIGHT);
        p.setAntiAlias(true);
    }

    private void drawDayHeader(String dayStr, int day, String dateNumStr, Canvas canvas, Paint p) {
        int x;
        p.setAntiAlias(true);

        int todayIndex = mTodayJulianDay - mFirstJulianDay;
        // Draw day of the month
        if (mNumDays > 1) {
            float y = DAY_HEADER_HEIGHT - DAY_HEADER_BOTTOM_MARGIN;

            // Draw day of the month
            x = computeDayLeftPosition(day + 1) - DAY_HEADER_RIGHT_MARGIN;
            p.setTextAlign(Align.RIGHT);
            p.setTextSize(DATE_HEADER_FONT_SIZE);

            p.setTypeface(todayIndex == day ? mBold : Typeface.DEFAULT);
            canvas.drawText(dateNumStr, x, y, p);

            // Draw day of the week
            x -= p.measureText(" " + dateNumStr);
            p.setTextSize(DAY_HEADER_FONT_SIZE);
            p.setTypeface(Typeface.DEFAULT);
            canvas.drawText(dayStr, x, y, p);
        } else {
            float y = DAY_HEADER_HEIGHT - DAY_HEADER_ONE_DAY_BOTTOM_MARGIN;
            p.setTextAlign(Align.LEFT);

            // Draw day of the week
            x = computeDayLeftPosition(day) + DAY_HEADER_ONE_DAY_LEFT_MARGIN;
            x = x+((mCellWidth-mDateStrWidthLong)/2);
            p.setTextSize(DAY_HEADER_FONT_SIZE);
            p.setTypeface(Typeface.DEFAULT);
            canvas.drawText(dayStr, x, y, p);

            // Draw day of the month
            x += p.measureText(dayStr) + DAY_HEADER_ONE_DAY_RIGHT_MARGIN;
            p.setTextSize(DATE_HEADER_FONT_SIZE);
            p.setTypeface(todayIndex == day ? mBold : Typeface.DEFAULT);
            canvas.drawText(dateNumStr, x, y, p);
        }
    }

    private void drawGridBackground(Rect r, Canvas canvas, Paint p) {
        Style savedStyle = p.getStyle();

        final float stopX = computeDayLeftPosition(mNumDays);
        float y = 0;
        final float deltaY = mCellHeight + HOUR_GAP;
        int linesIndex = 0;
        final float startY = 0;
        final float stopY = HOUR_GAP + 24 * (mCellHeight + HOUR_GAP);
        float x = mHoursWidth;

        // Draw the inner horizontal grid lines
        p.setColor(mCalendarGridLineInnerHorizontalColor);
        p.setStrokeWidth(GRID_LINE_INNER_WIDTH);
        p.setAntiAlias(false);
        y = 0;
        linesIndex = 0;
        for (int hour = 0; hour <= 24; hour++) {
            mLines[linesIndex++] = GRID_LINE_LEFT_MARGIN;
            mLines[linesIndex++] = y;
            mLines[linesIndex++] = stopX;
            mLines[linesIndex++] = y;
            y += deltaY;
        }
        if (mCalendarGridLineInnerVerticalColor != mCalendarGridLineInnerHorizontalColor) {
            canvas.drawLines(mLines, 0, linesIndex, p);
            linesIndex = 0;
            p.setColor(mCalendarGridLineInnerVerticalColor);
        }

        // Draw the inner vertical grid lines
        for (int day = 0; day <= mNumDays; day++) {
            x = computeDayLeftPosition(day);
            mLines[linesIndex++] = x;
            mLines[linesIndex++] = startY;
            mLines[linesIndex++] = x;
            mLines[linesIndex++] = stopY;
        }
        canvas.drawLines(mLines, 0, linesIndex, p);

        // Restore the saved style.
        p.setStyle(savedStyle);
        p.setAntiAlias(true);
    }

    /**
     * @param r
     * @param canvas
     * @param p
     */
    private void drawBgColors(Rect r, Canvas canvas, Paint p) {
        int todayIndex = mTodayJulianDay - mFirstJulianDay;
        // Draw the hours background color
        r.top = mDestRect.top;
        r.bottom = mDestRect.bottom;
        r.left = 0;
        r.right = mHoursWidth;
        p.setColor(mBgColor);
        p.setStyle(Style.FILL);
        p.setAntiAlias(false);
        canvas.drawRect(r, p);

        // Draw background for grid area
        if (mNumDays == 1 && todayIndex == 0) {
            // Draw a white background for the time later than current time
            int lineY = mCurrentTime.hour * (mCellHeight + HOUR_GAP)
                    + ((mCurrentTime.minute * mCellHeight) / 60) + 1;
            if (lineY < mViewStartY + mViewHeight) {
                lineY = Math.max(lineY, mViewStartY);
                r.left = mHoursWidth;
                r.right = mViewWidth;
                r.top = lineY;
                r.bottom = mViewStartY + mViewHeight;
                p.setColor(mFutureBgColor);
                canvas.drawRect(r, p);
            }
        } else if (todayIndex >= 0 && todayIndex < mNumDays) {
            // Draw today with a white background for the time later than current time
            int lineY = mCurrentTime.hour * (mCellHeight + HOUR_GAP)
                    + ((mCurrentTime.minute * mCellHeight) / 60) + 1;
            if (lineY < mViewStartY + mViewHeight) {
                lineY = Math.max(lineY, mViewStartY);
                r.left = computeDayLeftPosition(todayIndex) + 1;
                r.right = computeDayLeftPosition(todayIndex + 1);
                r.top = lineY;
                r.bottom = mViewStartY + mViewHeight;
                p.setColor(mFutureBgColor);
                canvas.drawRect(r, p);
            }

            // Paint Tomorrow and later days with future color
            if (todayIndex + 1 < mNumDays) {
                r.left = computeDayLeftPosition(todayIndex + 1) + 1;
                r.right = computeDayLeftPosition(mNumDays);
                r.top = mDestRect.top;
                r.bottom = mDestRect.bottom;
                p.setColor(mFutureBgColor);
                canvas.drawRect(r, p);
            }
        } else if (todayIndex < 0) {
            // Future
            r.left = computeDayLeftPosition(0) + 1;
            r.right = computeDayLeftPosition(mNumDays);
            r.top = mDestRect.top;
            r.bottom = mDestRect.bottom;
            p.setColor(mFutureBgColor);
            canvas.drawRect(r, p);
        }
        p.setAntiAlias(true);
    }

    public Event getSelectedEvent() {
        if (mSelectedEvent == null) {
            // There is no event at the selected hour, so create a new event.
            return getNewEvent(mSelectionDay, getSelectedTimeInMillis(),
                    getSelectedMinutesSinceMidnight());
        }
        return mSelectedEvent;
    }

    public boolean isEventSelected() {
        return (mSelectedEvent != null);
    }

    public Event getNewEvent() {
        return getNewEvent(mSelectionDay, getSelectedTimeInMillis(),
                getSelectedMinutesSinceMidnight());
    }

    static Event getNewEvent(int julianDay, long utcMillis,
                             int minutesSinceMidnight) {
        Event event = Event.newInstance();
        event.startDay = julianDay;
        event.endDay = julianDay;
        event.startMillis = utcMillis;
        event.endMillis = event.startMillis + MILLIS_PER_HOUR;
        event.startTime = minutesSinceMidnight;
        event.endTime = event.startTime + MINUTES_PER_HOUR;
        return event;
    }

    private int computeMaxStringWidth(int currentMax, String[] strings, Paint p) {
        float maxWidthF = 0.0f;

        int len = strings.length;
        for (int i = 0; i < len; i++) {
            float width = p.measureText(strings[i]);
            maxWidthF = Math.max(width, maxWidthF);
        }
        int maxWidth = (int) (maxWidthF + 0.5);
        if (maxWidth < currentMax) {
            maxWidth = currentMax;
        }
        return maxWidth;
    }

    private void setupTextRect(Rect r) {
        if (r.bottom <= r.top || r.right <= r.left) {
            r.bottom = r.top;
            r.right = r.left;
            return;
        }

        if (r.bottom - r.top > EVENT_TEXT_TOP_MARGIN + EVENT_TEXT_BOTTOM_MARGIN) {
            r.top += EVENT_TEXT_TOP_MARGIN;
            r.bottom -= EVENT_TEXT_BOTTOM_MARGIN;
        }
        if (r.right - r.left > EVENT_TEXT_LEFT_MARGIN + EVENT_TEXT_RIGHT_MARGIN) {
            r.left += EVENT_TEXT_LEFT_MARGIN;
            r.right -= EVENT_TEXT_RIGHT_MARGIN;
        }
    }

    /**
     * Return the layout for a numbered event. Create it if not already existing
     */
    private StaticLayout getEventLayout(StaticLayout[] layouts, int i, Event event, Paint paint,
                                        Rect r) {
        if (i < 0 || i >= layouts.length) {
            return null;
        }

        StaticLayout layout = layouts[i];
        // Check if we have already initialized the StaticLayout and that
        // the width hasn't changed (due to vertical resizing which causes
        // re-layout of events at min height)
        if (layout == null || r.width() != layout.getWidth()) {
            SpannableStringBuilder bob = new SpannableStringBuilder();
            if (event.title != null) {
                // MAX - 1 since we add a space
                bob.append(drawTextSanitizer(event.title.toString(), MAX_EVENT_TEXT_LEN - 1));
                bob.setSpan(new StyleSpan(Typeface.BOLD), 0, bob.length(), 0);
                bob.append(' ');
            }
            if (event.location != null) {
                bob.append(drawTextSanitizer(event.location.toString(),
                        MAX_EVENT_TEXT_LEN - bob.length()));
            }

            paint.setColor(mEventTextColor);

            // Leave a one pixel boundary on the left and right of the rectangle for the event
            layout = new StaticLayout(bob, 0, bob.length(), new TextPaint(paint), r.width(),
                    Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true, null, r.width());

            layouts[i] = layout;
        }
        layout.getPaint().setAlpha(mEventsAlpha);
        return layout;
    }

    private void drawEvents(int date, int dayIndex, int top, Canvas canvas, Paint p) {
        Paint eventTextPaint = mEventTextPaint;
        int left = computeDayLeftPosition(dayIndex) + 1;
        int cellWidth = computeDayLeftPosition(dayIndex + 1) - left + 1;
        int cellHeight = mCellHeight;

        // Use the selected hour as the selection region
        Rect selectionArea = mSelectionRect;
        selectionArea.top = top + mSelectionHour * (cellHeight + HOUR_GAP);
        selectionArea.bottom = selectionArea.top + cellHeight;
        selectionArea.left = left;
        selectionArea.right = selectionArea.left + cellWidth;

        final ArrayList<Event> events = mEvents;
        int numEvents = events.size();
        EventGeometry geometry = mEventGeometry;

        final int viewEndY = mViewStartY + mViewHeight - DAY_HEADER_HEIGHT;

        int alpha = eventTextPaint.getAlpha();
        eventTextPaint.setAlpha(mEventsAlpha);
        for (int i = 0; i < numEvents; i++) {
            Event event = events.get(i);

            if (!geometry.computeEventRect(date, left, top, cellWidth, event)) {
                continue;
            }

            // Don't draw it if it is not visible
            if (event.bottom < mViewStartY || event.top > viewEndY) {
                continue;
            }

            if (date == mSelectionDay && mComputeSelectedEvents
                    && geometry.eventIntersectsSelection(event, selectionArea)) {
                mSelectedEvents.add(event);
            }

            Rect r = drawEventRect(event, canvas, p, eventTextPaint, mViewStartY, viewEndY);
            setupTextRect(r);

            // Don't draw text if it is not visible
            if (r.top > viewEndY || r.bottom < mViewStartY) {
                continue;
            }
            StaticLayout layout = getEventLayout(mLayouts, i, event, eventTextPaint, r);
            drawEventText(layout, r, canvas, mViewStartY + 4, mViewStartY + mViewHeight
                    - DAY_HEADER_HEIGHT, false);
        }
        eventTextPaint.setAlpha(alpha);
    }

    private Rect drawEventRect(Event event, Canvas canvas, Paint p, Paint eventTextPaint, int visibleTop, int visibleBot) {
        // Draw the Event Rect
        Rect r = mRect;
        r.top = Math.max((int) event.top + EVENT_RECT_TOP_MARGIN, visibleTop);
        r.bottom = Math.min((int) event.bottom - EVENT_RECT_BOTTOM_MARGIN, visibleBot);
        r.left = (int) event.left + EVENT_RECT_LEFT_MARGIN;
        r.right = (int) event.right;

        int color;
        if (event == mClickedEvent) {
            color = mClickedColor;
        } else {
            color = event.color;
        }

        p.setStyle(Style.FILL_AND_STROKE);
        p.setAntiAlias(false);

        int floorHalfStroke = (int) Math.floor(EVENT_RECT_STROKE_WIDTH / 2.0f);
        int ceilHalfStroke = (int) Math.ceil(EVENT_RECT_STROKE_WIDTH / 2.0f);
        r.top = Math.max((int) event.top + EVENT_RECT_TOP_MARGIN + floorHalfStroke, visibleTop);
        r.bottom = Math.min((int) event.bottom - EVENT_RECT_BOTTOM_MARGIN - ceilHalfStroke,
                visibleBot);
        r.left += floorHalfStroke;
        r.right -= ceilHalfStroke;
        p.setStrokeWidth(EVENT_RECT_STROKE_WIDTH);
        p.setColor(color);
        int alpha = p.getAlpha();
        p.setAlpha(mEventsAlpha);
        canvas.drawRect(r, p);
        p.setAlpha(alpha);
        p.setStyle(Style.FILL);

        // If this event is selected, then use the selection color
        if (mSelectedEvent == event && mClickedEvent != null) {
            boolean paintIt = false;
            color = 0;

            if (paintIt) {
                p.setColor(color);
                canvas.drawRect(r, p);
            }
            p.setAntiAlias(true);
        }

        // Setup rect for drawEventText which follows
        r.top = (int) event.top + EVENT_RECT_TOP_MARGIN;
        r.bottom = (int) event.bottom - EVENT_RECT_BOTTOM_MARGIN;
        r.left = (int) event.left + EVENT_RECT_LEFT_MARGIN;
        r.right = (int) event.right - EVENT_RECT_RIGHT_MARGIN;
        return r;
    }

    private final Pattern drawTextSanitizerFilter = Pattern.compile("[\t\n],");

    // Sanitize a string before passing it to drawText or else we get little
    // squares. For newlines and tabs before a comma, delete the character.
    // Otherwise, just replace them with a space.
    private String drawTextSanitizer(String string, int maxEventTextLen) {
        Matcher m = drawTextSanitizerFilter.matcher(string);
        string = m.replaceAll(",");

        int len = string.length();
        if (maxEventTextLen <= 0) {
            string = "";
            len = 0;
        } else if (len > maxEventTextLen) {
            string = string.substring(0, maxEventTextLen);
            len = maxEventTextLen;
        }

        return string.replace('\n', ' ');
    }

    private void drawEventText(StaticLayout eventLayout, Rect rect, Canvas canvas, int top,
                               int bottom, boolean center) {
        // drawEmptyRect(canvas, rect, 0xFFFF00FF); // for debugging

        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;

        // If the rectangle is too small for text, then return
        if (eventLayout == null || width < MIN_CELL_WIDTH_FOR_TEXT) {
            return;
        }

        int totalLineHeight = 0;
        int lineCount = eventLayout.getLineCount();
        for (int i = 0; i < lineCount; i++) {
            int lineBottom = eventLayout.getLineBottom(i);
            if (lineBottom <= height) {
                totalLineHeight = lineBottom;
            } else {
                break;
            }
        }

        // + 2 is small workaround when the font is slightly bigger then the rect. This will
        // still allow the text to be shown without overflowing into the other all day rects.
        if (totalLineHeight == 0 || rect.top > bottom || rect.top + totalLineHeight + 2 < top) {
            return;
        }

        // Use a StaticLayout to format the string.
        canvas.save();
        //  canvas.translate(rect.left, rect.top + (rect.bottom - rect.top / 2));
        int padding = center ? (rect.bottom - rect.top - totalLineHeight) / 2 : 0;
        canvas.translate(rect.left, rect.top + padding);
        rect.left = 0;
        rect.right = width;
        rect.top = 0;
        rect.bottom = totalLineHeight;

        // There's a bug somewhere. If this rect is outside of a previous
        // cliprect, this becomes a no-op. What happens is that the text draw
        // past the event rect. The current fix is to not draw the staticLayout
        // at all if it is completely out of bound.
        canvas.clipRect(rect);
        eventLayout.draw(canvas);
        canvas.restore();
    }

    // The following routines are called from the parent activity when certain
    // touch events occur.
    private void doDown(MotionEvent ev) {
        mTouchMode = TOUCH_MODE_DOWN;
        mViewStartX = 0;
        mOnFlingCalled = false;
        mHandler.removeCallbacks(mContinueScroll);
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        // Save selection information: we use setSelectionFromPosition to find the selected event
        // in order to show the "clicked" color. But since it is also setting the selected info
        // for new events, we need to restore the old info after calling the function.
        Event oldSelectedEvent = mSelectedEvent;
        int oldSelectionDay = mSelectionDay;
        int oldSelectionHour = mSelectionHour;
        if (setSelectionFromPosition(x, y, false)) {
            // If a time was selected (a blue selection box is visible) and the click location
            // is in the selected time, do not show a click on an event to prevent a situation
            // of both a selection and an event are clicked when they overlap.
//            boolean pressedSelected = (mSelectionMode != SELECTION_HIDDEN)
//                    && oldSelectionDay == mSelectionDay && oldSelectionHour == mSelectionHour;
            if (/*!pressedSelected && */mSelectedEvent != null) {
                mSavedClickedEvent = mSelectedEvent;
                mDownTouchTime = System.currentTimeMillis();
                postDelayed(mSetClick, mOnDownDelay);
            } else {
                eventClickCleanup();
            }
        }
        mSelectedEvent = oldSelectedEvent;
        mSelectionDay = oldSelectionDay;
        mSelectionHour = oldSelectionHour;
        invalidate();
    }

    private void doSingleTapUp(MotionEvent ev) {
        if (!mHandleActionUp || mScrolling) {
            return;
        }

        int x = (int) ev.getX();
        int y = (int) ev.getY();

        boolean validPosition = setSelectionFromPosition(x, y, false);
        if (!validPosition) {
            if (y < DAY_HEADER_HEIGHT) {
                Time selectedTime = new Time(mBaseDate);
                selectedTime.setJulianDay(mSelectionDay);
                selectedTime.hour = mSelectionHour;
                selectedTime.normalize(true /* ignore isDst */);
            }
            return;
        }

        if (mSelectedEvent != null) {
            long clearDelay = (CLICK_DISPLAY_DURATION + mOnDownDelay) -
                    (System.currentTimeMillis() - mDownTouchTime);
            if (clearDelay > 0) {
                this.postDelayed(mClearClick, clearDelay);
            } else {
                this.post(mClearClick);
            }

        }
        invalidate();
    }

    private void doLongPress(MotionEvent ev) {
        eventClickCleanup();
        if (mScrolling) {
            return;
        }

        // Scale gesture in progress
        if (mStartingSpanY != 0) {
            return;
        }

        int x = (int) ev.getX();
        int y = (int) ev.getY();

        boolean validPosition = setSelectionFromPosition(x, y, false);
        if (!validPosition) {
            // return if the touch wasn't on an area of concern
            return;
        }

        invalidate();
        performLongClick();
    }

    private void doScroll(MotionEvent e1, MotionEvent e2, float deltaX, float deltaY) {
        cancelAnimation();
        if (mStartingScroll) {
            mInitialScrollX = 0;
            mInitialScrollY = 0;
            mStartingScroll = false;
        }

        mInitialScrollX += deltaX;
        mInitialScrollY += deltaY;
        int distanceX = (int) mInitialScrollX;
        int distanceY = (int) mInitialScrollY;

        final float focusY = getAverageY(e2);
        if (mRecalCenterHour) {
            // Calculate the hour that correspond to the average of the Y touch points
            mGestureCenterHour = (mViewStartY + focusY - DAY_HEADER_HEIGHT)
                    / (mCellHeight + DAY_GAP);
            mRecalCenterHour = false;
        }

        // If we haven't figured out the predominant scroll direction yet,
        // then do it now.
        if (mTouchMode == TOUCH_MODE_DOWN) {
            int absDistanceX = Math.abs(distanceX);
            int absDistanceY = Math.abs(distanceY);
            mScrollStartY = mViewStartY;
            mPreviousDirection = 0;

            if (absDistanceX > absDistanceY) {
                int slopFactor = mScaleGestureDetector.isInProgress() ? 20 : 2;
                if (absDistanceX > mScaledPagingTouchSlop * slopFactor &&
                        ((mLeftBoundary<mFirstJulianDay && distanceX<0)
                        || (mRightBoundary>mFirstJulianDay+mNumDays && distanceX>0))) {
                    mTouchMode = TOUCH_MODE_HSCROLL;
                    mViewStartX = distanceX;
                    initNextView(-mViewStartX);
                }
            } else {
                mTouchMode = TOUCH_MODE_VSCROLL;
            }
        } else if ((mTouchMode & TOUCH_MODE_HSCROLL) != 0) {
            // We are already scrolling horizontally, so check if we
            // changed the direction of scrolling so that the other week
            // is now visible.
            mViewStartX = distanceX;
            if (distanceX != 0) {
                int direction = (distanceX > 0) ? 1 : -1;
                if (direction != mPreviousDirection) {
                    // The user has switched the direction of scrolling
                    // so re-init the next view
                    initNextView(-mViewStartX);
                    mPreviousDirection = direction;
                }
            }
        }

        if ((mTouchMode & TOUCH_MODE_VSCROLL) != 0) {
            // Calculate the top of the visible region in the calendar grid.
            // Increasing/decrease this will scroll the calendar grid up/down.
            mViewStartY = (int) ((mGestureCenterHour * (mCellHeight + DAY_GAP))
                    - focusY + DAY_HEADER_HEIGHT);

            // If dragging while already at the end, do a glow
            final int pulledToY = (int) (mScrollStartY + deltaY);
            if (pulledToY < 0) {
                mEdgeEffectTop.onPull(deltaY / mViewHeight);
                if (!mEdgeEffectBottom.isFinished()) {
                    mEdgeEffectBottom.onRelease();
                }
            } else if (pulledToY > mMaxViewStartY) {
                mEdgeEffectBottom.onPull(deltaY / mViewHeight);
                if (!mEdgeEffectTop.isFinished()) {
                    mEdgeEffectTop.onRelease();
                }
            }

            if (mViewStartY < 0) {
                mViewStartY = 0;
                mRecalCenterHour = true;
            } else if (mViewStartY > mMaxViewStartY) {
                mViewStartY = mMaxViewStartY;
                mRecalCenterHour = true;
            }
            if (mRecalCenterHour) {
                // Calculate the hour that correspond to the average of the Y touch points
                mGestureCenterHour = (mViewStartY + focusY - DAY_HEADER_HEIGHT)
                        / (mCellHeight + DAY_GAP);
                mRecalCenterHour = false;
            }
            computeFirstHour();
        }

        mScrolling = true;

        invalidate();
    }

    private float getAverageY(MotionEvent me) {
        int count = me.getPointerCount();
        float focusY = 0;
        for (int i = 0; i < count; i++) {
            focusY += me.getY(i);
        }
        focusY /= count;
        return focusY;
    }

    private void cancelAnimation() {
        Animation in = mViewSwitcher.getInAnimation();
        if (in != null) {
            // cancel() doesn't terminate cleanly.
            in.scaleCurrentDuration(0);
        }
        Animation out = mViewSwitcher.getOutAnimation();
        if (out != null) {
            // cancel() doesn't terminate cleanly.
            out.scaleCurrentDuration(0);
        }
    }

    private void doFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        cancelAnimation();

        eventClickCleanup();

        mOnFlingCalled = true;

        if ((mTouchMode & TOUCH_MODE_HSCROLL) != 0) {
            // Horizontal fling.
            // initNextView(deltaX);
            mTouchMode = TOUCH_MODE_INITIAL_STATE;

            int deltaX = (int) e2.getX() - (int) e1.getX();
            switchViews(deltaX < 0, mViewStartX, mViewWidth, velocityX);
            mViewStartX = 0;
            return;
        }

        if ((mTouchMode & TOUCH_MODE_VSCROLL) == 0) {
            return;
        }

        // Vertical fling.
        mTouchMode = TOUCH_MODE_INITIAL_STATE;
        mViewStartX = 0;

        // Continue scrolling vertically
        mScrolling = true;
        mScroller.fling(0 /* startX */, mViewStartY /* startY */, 0 /* velocityX */,
                (int) -velocityY, 0 /* minX */, 0 /* maxX */, 0 /* minY */,
                mMaxViewStartY /* maxY */, OVERFLING_DISTANCE, OVERFLING_DISTANCE);

        // When flinging down, show a glow when it hits the end only if it
        // wasn't started at the top
        if (velocityY > 0 && mViewStartY != 0) {
            mCallEdgeEffectOnAbsorb = true;
        }
        // When flinging up, show a glow when it hits the end only if it wasn't
        // started at the bottom
        else if (velocityY < 0 && mViewStartY != mMaxViewStartY) {
            mCallEdgeEffectOnAbsorb = true;
        }
        mHandler.post(mContinueScroll);
    }

    private boolean initNextView(int deltaX) {
        // Change the view to the previous day or week
        DayView view = (DayView) mViewSwitcher.getNextView();
        Time date = view.mBaseDate;
        date.set(mBaseDate);
        boolean switchForward;
        if (deltaX > 0) {
            date.monthDay -= mNumDays;
            view.setSelectedDay(mSelectionDay - mNumDays);
            switchForward = false;
        } else {
            date.monthDay += mNumDays;
            view.setSelectedDay(mSelectionDay + mNumDays);
            switchForward = true;
        }
        date.normalize(true /* ignore isDst */);
        initView(view);
        view.layout(getLeft(), getTop(), getRight(), getBottom());
        view.reloadEvents();
        return switchForward;
    }

    // ScaleGestureDetector.OnScaleGestureListener
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        mHandleActionUp = false;
        float gestureCenterInPixels = detector.getFocusY() - DAY_HEADER_HEIGHT;
        mGestureCenterHour = (mViewStartY + gestureCenterInPixels) / (mCellHeight + DAY_GAP);

        mStartingSpanY = Math.max(MIN_Y_SPAN, Math.abs(detector.getCurrentSpan())); //TODO .getCurrentSpanY()
        mCellHeightBeforeScaleGesture = mCellHeight;

       /* if (DEBUG_SCALING) {
            float ViewStartHour = mViewStartY / (float) (mCellHeight + DAY_GAP);
            Log.d(TAG, "onScaleBegin: mGestureCenterHour:" + mGestureCenterHour
                    + "\tViewStartHour: " + ViewStartHour + "\tmViewStartY:" + mViewStartY
                    + "\tmCellHeight:" + mCellHeight + " SpanY:" + detector.getCurrentSpanY());
        }*/

        return true;
    }

    // ScaleGestureDetector.OnScaleGestureListener
    public boolean onScale(ScaleGestureDetector detector) {
        float spanY = Math.max(MIN_Y_SPAN, Math.abs(detector.getCurrentSpan()));//TODO .getCurrentSpanY()

        mCellHeight = (int) (mCellHeightBeforeScaleGesture * spanY / mStartingSpanY);

        if (mCellHeight < mMinCellHeight) {
            // If mStartingSpanY is too small, even a small increase in the
            // gesture can bump the mCellHeight beyond MAX_CELL_HEIGHT
            mStartingSpanY = spanY;
            mCellHeight = mMinCellHeight;
            mCellHeightBeforeScaleGesture = mMinCellHeight;
        } else if (mCellHeight > MAX_CELL_HEIGHT) {
            mStartingSpanY = spanY;
            mCellHeight = MAX_CELL_HEIGHT;
            mCellHeightBeforeScaleGesture = MAX_CELL_HEIGHT;
        }

        int gestureCenterInPixels = (int) detector.getFocusY() - DAY_HEADER_HEIGHT;
        mViewStartY = (int) (mGestureCenterHour * (mCellHeight + DAY_GAP)) - gestureCenterInPixels;
        mMaxViewStartY = HOUR_GAP + 24 * (mCellHeight + HOUR_GAP) - mGridAreaHeight;

        if (mViewStartY < 0) {
            mViewStartY = 0;
            mGestureCenterHour = (mViewStartY + gestureCenterInPixels)
                    / (float) (mCellHeight + DAY_GAP);
        } else if (mViewStartY > mMaxViewStartY) {
            mViewStartY = mMaxViewStartY;
            mGestureCenterHour = (mViewStartY + gestureCenterInPixels)
                    / (float) (mCellHeight + DAY_GAP);
        }
        computeFirstHour();

        mRemeasure = true;
        invalidate();
        return true;
    }

    // ScaleGestureDetector.OnScaleGestureListener
    public void onScaleEnd(ScaleGestureDetector detector) {
        mScrollStartY = mViewStartY;
        mInitialScrollY = 0;
        mInitialScrollX = 0;
        mStartingSpanY = 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();

        if ((ev.getActionMasked() == MotionEvent.ACTION_DOWN) ||
                (ev.getActionMasked() == MotionEvent.ACTION_UP) ||
                (ev.getActionMasked() == MotionEvent.ACTION_POINTER_UP) ||
                (ev.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)) {
            mRecalCenterHour = true;
        }

        if ((mTouchMode & TOUCH_MODE_HSCROLL) == 0) {
            mScaleGestureDetector.onTouchEvent(ev);
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mStartingScroll = true;

                mHandleActionUp = true;
                mGestureDetector.onTouchEvent(ev);
                return true;

            case MotionEvent.ACTION_MOVE:
                mGestureDetector.onTouchEvent(ev);
                return true;

            case MotionEvent.ACTION_UP:
                mEdgeEffectTop.onRelease();
                mEdgeEffectBottom.onRelease();
                mStartingScroll = false;
                mGestureDetector.onTouchEvent(ev);
                if (!mHandleActionUp) {
                    mHandleActionUp = true;
                    mViewStartX = 0;
                    invalidate();
                    return true;
                }

                if (mOnFlingCalled) {
                    return true;
                }

                // If we were scrolling, then reset the selected hour so that it is visible.
                if (mScrolling) {
                    mScrolling = false;
                    resetSelectedHour();
                    invalidate();
                }

                if ((mTouchMode & TOUCH_MODE_HSCROLL) != 0) {
                    mTouchMode = TOUCH_MODE_INITIAL_STATE;
                    if (Math.abs(mViewStartX) > mHorizontalSnapBackThreshold) {
                        // The user has gone beyond the threshold so switch views
                        switchViews(mViewStartX > 0, mViewStartX, mViewWidth, 0);
                        mViewStartX = 0;
                        return true;
                    } else {
                        // Not beyond the threshold so invalidate which will cause
                        // the view to snap back. Also call recalc() to ensure
                        // that we have the correct starting date and title.
                        recalc();
                        invalidate();
                        mViewStartX = 0;
                    }
                }

                return true;

            // This case isn't expected to happen.
            case MotionEvent.ACTION_CANCEL:
                mGestureDetector.onTouchEvent(ev);
                mScrolling = false;
                resetSelectedHour();
                return true;

            default:
                if (mGestureDetector.onTouchEvent(ev)) {
                    return true;
                }
                return super.onTouchEvent(ev);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        MenuItem item;

        // If the trackball is held down, then the context menu pops up and
        // we never get onKeyUp() for the long-press. So check for it here
        // and change the selection to the long-press state.
        /*if (mSelectionMode != SELECTION_LONGPRESS) {
            mSelectionMode = SELECTION_LONGPRESS;
            invalidate();
        }*/

        final long startMillis = getSelectedTimeInMillis();
        int flags = DateUtils.FORMAT_SHOW_TIME
                | DateUtils.FORMAT_CAP_NOON_MIDNIGHT
                | DateUtils.FORMAT_SHOW_WEEKDAY;
        final String title = DayUtils.formatDateRange(mContext, startMillis, startMillis, flags);
        menu.setHeaderTitle(title);

        int numSelectedEvents = mSelectedEvents.size();
        if (mNumDays == 1) {
            // Day view.

            // If there is a selected event, then allow it to be viewed and
            // edited.
            if (numSelectedEvents >= 1) {
                item = menu.add(0, MENU_EVENT_VIEW, 0, "View event");
                item.setOnMenuItemClickListener(mContextMenuHandler);
                item.setIcon(android.R.drawable.ic_menu_info_details);
            }
        } else {
            // Week view.

            // If there is a selected event, then allow it to be viewed and
            // edited.
            if (numSelectedEvents >= 1) {
                item = menu.add(0, MENU_EVENT_VIEW, 0, "View event");
                item.setOnMenuItemClickListener(mContextMenuHandler);
                item.setIcon(android.R.drawable.ic_menu_info_details);
            }

            item = menu.add(0, MENU_DAY, 0, "Show day");
            item.setOnMenuItemClickListener(mContextMenuHandler);
            item.setIcon(android.R.drawable.ic_menu_day);
            item.setAlphabeticShortcut('d');
        }
    }

    private class ContextMenuHandler implements MenuItem.OnMenuItemClickListener {

        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case MENU_EVENT_VIEW: {
                    if (mSelectedEvent != null) {
                        mController.sendEventRelatedEvent(this, EventType.VIEW_EVENT_DETAILS,
                                mSelectedEvent.id, mSelectedEvent.startMillis,
                                mSelectedEvent.endMillis, 0, 0, -1);
                    }
                    break;
                }
                case MENU_DAY: {
                    mController.sendEvent(this, EventType.GO_TO, getSelectedTime(), null, -1,
                            ViewType.DAY);
                    break;
                }
                case MENU_AGENDA: {
                    mController.sendEvent(this, EventType.GO_TO, getSelectedTime(), null, -1,
                            ViewType.AGENDA);
                    break;
                }
                default: {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Sets mSelectionDay and mSelectionHour based on the (x,y) touch position.
     * If the touch position is not within the displayed grid, then this
     * method returns false.
     *
     * @param x                the x position of the touch
     * @param y                the y position of the touch
     * @param keepOldSelection - do not change the selection info (used for invoking accessibility
     *                         messages)
     * @return true if the touch position is valid
     */
    private boolean setSelectionFromPosition(int x, final int y, boolean keepOldSelection) {

        Event savedEvent = null;
        int savedDay = 0;
        int savedHour = 0;
        if (keepOldSelection) {
            // Store selection info and restore it at the end. This way, we can invoke the
            // right accessibility message without affecting the selection.
            savedEvent = mSelectedEvent;
            savedDay = mSelectionDay;
            savedHour = mSelectionHour;
        }
        if (x < mHoursWidth) {
            x = mHoursWidth;
        }

        int day = (x - mHoursWidth) / (mCellWidth + DAY_GAP);
        if (day >= mNumDays) {
            day = mNumDays - 1;
        }
        day += mFirstJulianDay;
        setSelectedDay(day);

        if (y < DAY_HEADER_HEIGHT) {
            return false;
        }

        setSelectedHour(mFirstHour); /* First fully visible hour */

        if (y >= mFirstCell) {
            // y is now offset from top of the scrollable region
            int adjustedY = y - mFirstCell;

            if (adjustedY < mFirstHourOffset) {
                setSelectedHour(mSelectionHour - 1); /* In the partially visible hour */
            } else {
                setSelectedHour(mSelectionHour +
                        (adjustedY - mFirstHourOffset) / (mCellHeight + HOUR_GAP));
            }
        }

        findSelectedEvent(x, y);

        // Restore old values
        if (keepOldSelection) {
            mSelectedEvent = savedEvent;
            mSelectionDay = savedDay;
            mSelectionHour = savedHour;
        }
        return true;
    }

    private void findSelectedEvent(int x, int y) {
        int date = mSelectionDay;
        int cellWidth = mCellWidth;
        ArrayList<Event> events = mEvents;
        int numEvents = events.size();
        int left = computeDayLeftPosition(mSelectionDay - mFirstJulianDay);
        int top = 0;
        setSelectedEvent(null);

        mSelectedEvents.clear();

        // Adjust y for the scrollable bitmap
        y += mViewStartY - mFirstCell;

        // Use a region around (x,y) for the selection region
        Rect region = mRect;
        region.left = x - 10;
        region.right = x + 10;
        region.top = y - 10;
        region.bottom = y + 10;

        EventGeometry geometry = mEventGeometry;

        for (int i = 0; i < numEvents; i++) {
            Event event = events.get(i);
            // Compute the event rectangle.
            if (!geometry.computeEventRect(date, left, top, cellWidth, event)) {
                continue;
            }

            // If the event intersects the selection region, then add it to
            // mSelectedEvents.
            if (geometry.eventIntersectsSelection(event, region)) {
                mSelectedEvents.add(event);
            }
        }

        // If there are any events in the selected region, then assign the
        // closest one to mSelectedEvent.
        if (mSelectedEvents.size() > 0) {
            int len = mSelectedEvents.size();
            Event closestEvent = null;
            float minDist = mViewWidth + mViewHeight; // some large distance
            for (int index = 0; index < len; index++) {
                Event ev = mSelectedEvents.get(index);
                float dist = geometry.pointToEvent(x, y, ev);
                if (dist < minDist) {
                    minDist = dist;
                    closestEvent = ev;
                }
            }
            setSelectedEvent(closestEvent);

            // Keep the selected hour and day consistent with the selected
            // event. They could be different if we touched on an empty hour
            // slot very close to an event in the previous hour slot. In
            // that case we will select the nearby event.
            int startDay = mSelectedEvent.startDay;
            int endDay = mSelectedEvent.endDay;
            if (mSelectionDay < startDay) {
                setSelectedDay(startDay);
            } else if (mSelectionDay > endDay) {
                setSelectedDay(endDay);
            }

            int startHour = mSelectedEvent.startTime / 60;
            int endHour;
            if (mSelectedEvent.startTime < mSelectedEvent.endTime) {
                endHour = (mSelectedEvent.endTime - 1) / 60;
            } else {
                endHour = mSelectedEvent.endTime / 60;
            }

            if (mSelectionHour < startHour && mSelectionDay == startDay) {
                setSelectedHour(startHour);
            } else if (mSelectionHour > endHour && mSelectionDay == endDay) {
                setSelectedHour(endHour);
            }
        }
    }

    // Encapsulates the code to continue the scrolling after the
    // finger is lifted. Instead of stopping the scroll immediately,
    // the scroll continues to "free spin" and gradually slows down.
    private class ContinueScroll implements Runnable {

        public void run() {
            mScrolling = mScrolling && mScroller.computeScrollOffset();
            if (!mScrolling || mPaused) {
                resetSelectedHour();
                invalidate();
                return;
            }

            mViewStartY = mScroller.getCurrY();

            if (mCallEdgeEffectOnAbsorb) {
                if (mViewStartY < 0) {
                    mEdgeEffectTop.onAbsorb((int) mLastVelocity);
                    mCallEdgeEffectOnAbsorb = false;
                } else if (mViewStartY > mMaxViewStartY) {
                    mEdgeEffectBottom.onAbsorb((int) mLastVelocity);
                    mCallEdgeEffectOnAbsorb = false;
                }
                mLastVelocity = mScroller.getCurrVelocity();
            }

            if (mScrollStartY == 0 || mScrollStartY == mMaxViewStartY) {
                // Allow overscroll/springback only on a fling,
                // not a pull/fling from the end
                if (mViewStartY < 0) {
                    mViewStartY = 0;
                } else if (mViewStartY > mMaxViewStartY) {
                    mViewStartY = mMaxViewStartY;
                }
            }

            computeFirstHour();
            mHandler.post(this);
            invalidate();
        }
    }

    /**
     * Cleanup the pop-up and timers.
     */
    public void cleanup() {
        mPaused = true;
        if (mHandler != null) {
            mHandler.removeCallbacks(mUpdateCurrentTime);
        }

        // Clear all click animations
        eventClickCleanup();
        // Turn off redraw
        mRemeasure = false;
        // Turn off scrolling to make sure the view is in the correct state if we fling back to it
        mScrolling = false;
    }

    private void eventClickCleanup() {
        this.removeCallbacks(mClearClick);
        this.removeCallbacks(mSetClick);
        mClickedEvent = null;
        mSavedClickedEvent = null;
    }

    private void setSelectedEvent(Event e) {
        mSelectedEvent = e;
    }

    private void setSelectedHour(int h) {
        mSelectionHour = h;
    }

    private void setSelectedDay(int d) {
        mSelectionDay = d;
    }

    /**
     * Restart the update timer
     */
    public void restartCurrentTimeUpdates() {
        mPaused = false;
        if (mHandler != null) {
            mHandler.removeCallbacks(mUpdateCurrentTime);
            mHandler.post(mUpdateCurrentTime);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cleanup();
        super.onDetachedFromWindow();
    }

    class UpdateCurrentTime implements Runnable {

        public void run() {
            long currentTime = System.currentTimeMillis();
            mCurrentTime.set(currentTime);
            //% causes update to occur on 5 minute marks (11:10, 11:15, 11:20, etc.)
            if (!DayView.this.mPaused) {
                mHandler.postDelayed(mUpdateCurrentTime, UPDATE_CURRENT_TIME_DELAY
                        - (currentTime % UPDATE_CURRENT_TIME_DELAY));
            }
            mTodayJulianDay = Time.getJulianDay(currentTime, mCurrentTime.gmtoff);
            invalidate();
        }
    }

    class CalendarGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent ev) {
            DayView.this.doSingleTapUp(ev);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent ev) {
            DayView.this.doLongPress(ev);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            eventClickCleanup();

            DayView.this.doScroll(e1, e2, distanceX, distanceY);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {


            DayView.this.doFling(e1, e2, velocityX, velocityY);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent ev) {
            DayView.this.doDown(ev);
            return true;
        }
    }

    // The rest of this file was borrowed from Launcher2 - PagedView.java
    private static final int MINIMUM_SNAP_VELOCITY = 2200;

    private class ScrollInterpolator implements Interpolator {
        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            t = t * t * t * t * t + 1;

            if ((1 - t) * mAnimationDistance < 1) {
                cancelAnimation();
            }

            return t;
        }
    }

    private long calculateDuration(float delta, float width, float velocity) {
        /*
         * Here we compute a "distance" that will be used in the computation of
         * the overall snap duration. This is a function of the actual distance
         * that needs to be traveled; we keep this value close to half screen
         * size in order to reduce the variance in snap duration as a function
         * of the distance the page needs to travel.
         */
        final float halfScreenSize = width / 2;
        float distanceRatio = delta / width;
        float distanceInfluenceForSnapDuration = distanceInfluenceForSnapDuration(distanceRatio);
        float distance = halfScreenSize + halfScreenSize * distanceInfluenceForSnapDuration;

        velocity = Math.abs(velocity);
        velocity = Math.max(MINIMUM_SNAP_VELOCITY, velocity);

        /*
         * we want the page's snap velocity to approximately match the velocity
         * at which the user flings, so we scale the duration by a value near to
         * the derivative of the scroll interpolator at zero, ie. 5. We use 6 to
         * make it a little slower.
         */
        return (long) (6 * Math.round(1000 * Math.abs(distance / velocity)));
    }

    /*
     * We want the duration of the page snap animation to be influenced by the
     * distance that the screen has to travel, however, we don't want this
     * duration to be effected in a purely linear fashion. Instead, we use this
     * method to moderate the effect that the distance of travel has on the
     * overall snap duration.
     */
    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }
}
