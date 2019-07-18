package de.tum.`in`.tumcampusapp.component.ui.transportation

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextSwitcher
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import org.joda.time.DateTime
import org.joda.time.Seconds
import java.util.*

/**
 * Custom view that shows a departure.
 * Holds an icon of the subway public transfer line, the line name and an animated
 * automatically down counting departure time
 */
class DepartureView

@JvmOverloads constructor(context: Context, private val useCompactView: Boolean = false) : LinearLayout(context) {
    private val symbolView: TextView by lazy { findViewById<TextView>(R.id.line_symbol) }
    private val lineView: TextView by lazy { findViewById<TextView>(R.id.nameTextView) }
    private val timeSwitcher: TextSwitcher by lazy { findViewById<TextSwitcher>(R.id.line_switcher) }
    private val mHandler: Handler
    private var valueAnimator: ValueAnimator? = null
    private var departureTime: DateTime? = null

    val symbol: String
        get() = symbolView.text.toString()

    init {

        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val inflater = LayoutInflater.from(context)
        if (useCompactView) {
            inflater.inflate(R.layout.departure_line_small, this, true)
        } else {
            inflater.inflate(R.layout.departure_line_big, this, true)
        }

        // Declare the in and out animations and initialize them
        val `in` = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_in_left)
        val out = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right)

        // set the animation type of textSwitcher
        timeSwitcher.inAnimation = `in`
        timeSwitcher.outAnimation = out

        mHandler = Handler()
    }

    /**
     * Sets the line symbol name
     *
     * @param symbol Symbol e.g. U6, S1, T14
     */
    fun setSymbol(symbol: String, highlight: Boolean) {
        val mvvSymbol = MVVSymbol(symbol, context)
        symbolView.setTextColor(mvvSymbol.textColor)
        symbolView.text = symbol
        symbolView.backgroundTintList = ColorStateList.valueOf(mvvSymbol.backgroundColor)

        if (highlight) {
            if (useCompactView) {
                setBackgroundColor(context.resources.getColor(R.color.reduced_opacity) and mvvSymbol.backgroundColor)
            } else {
                setBackgroundColor(mvvSymbol.backgroundColor)
                lineView.setTextColor(Color.WHITE)
                for (index in 0 until timeSwitcher.childCount) {
                    val tw = timeSwitcher.getChildAt(index) as TextView
                    tw.setTextColor(Color.WHITE)
                }
            }
        } else {
            setBackgroundColor(mvvSymbol.textColor)
            lineView.setTextColor(Color.BLACK)

            for (index in 0 until timeSwitcher.childCount) {
                val tw = timeSwitcher.getChildAt(index) as TextView
                tw.setTextColor(Color.GRAY)
            }
        }
    }

    /**
     * Sets the line name
     *
     * @param line Line name e.g. Klinikum Großhadern
     */
    fun setLine(line: CharSequence) {
        lineView.text = line
    }

    /**
     * Sets the departure time
     *
     * @param departureTime Timestamp in milliseconds, when transport leaves
     */
    fun setTime(departureTime: DateTime) {
        this.departureTime = departureTime
        updateDepartureTime()
    }

    private fun updateDepartureTime() {
        val departureOffset = Seconds.secondsBetween(DateTime.now(), departureTime ?: DateTime.now()).seconds

        if (departureOffset > 0) {

            val hours = departureOffset / ONE_HOUR_IN_SECONDS
            val minutes = departureOffset / ONE_MINUTE_IN_SECONDS % MINUTES_PER_HOUR
            val seconds = departureOffset % ONE_MINUTE_IN_SECONDS

            val text = if (hours > 0) {
                String.format(Locale.getDefault(), "%2d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%2d:%02d", minutes, seconds)
            }

            timeSwitcher.setCurrentText(text)
        } else {
            animateOut()
            return
        }
        // Keep countDown approximately in sync.
        mHandler.postDelayed({ updateDepartureTime() }, 1000)
    }

    private fun animateOut() {
        valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
                .setDuration(500).apply {
                    addUpdateListener(SlideOutAnimator())
                    interpolator = AccelerateInterpolator()
                    start()
                }
    }

    /**
     * Call this, when the DepartureView isn't needed anymore.
     */
    fun removeAllCallbacksAndMessages() {
        mHandler.removeCallbacksAndMessages(null)

        valueAnimator?.apply {
            cancel()
            removeAllUpdateListeners()
        }
    }

    private inner class SlideOutAnimator : ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationUpdate(animator: ValueAnimator) {
            val value = animator.animatedValue as Float
            if (layoutParams != null) {
                translationX = value * width
                layoutParams.height = ((1.0f - value) * height).toInt()
                alpha = 1.0f - value
                requestLayout()
                if (value >= 1.0f) {
                    visibility = View.GONE
                }
            }
        }
    }

    companion object {

        private const val ONE_HOUR_IN_SECONDS = 3600
        private const val MINUTES_PER_HOUR = 60
        private const val ONE_MINUTE_IN_SECONDS = 60
    }
}