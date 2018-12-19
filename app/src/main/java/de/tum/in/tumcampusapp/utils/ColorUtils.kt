package de.tum.`in`.tumcampusapp.utils

import android.graphics.Color

object ColorUtils {

    private const val SATURATION_ADJUST = 1.3f
    private const val INTENSITY_ADJUST = 0.8f

    fun getDisplayColorFromColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[1] = Math.min(hsv[1] * SATURATION_ADJUST, 1.0f)
        hsv[2] *= INTENSITY_ADJUST
        return Color.HSVToColor(hsv)
    }

}
