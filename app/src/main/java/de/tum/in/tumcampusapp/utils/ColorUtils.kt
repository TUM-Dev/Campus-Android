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

/**
 * Return the color with the given alpha value.
 * Examples:
 *   0xabcdef.withAlpha(0xCF) == 0xCFabcdef
 *   0xFFabcdef.withAlpha(0xCF) == 0xCFabcdef
 *
 * @param alpha the alpha channel value: [0x0..0xFF].
 * @return the color with the given alpha value applied.
 */
fun Int.withAlpha(alpha: Int): Int {
    require(alpha in 0..0xFF)
    return this and 0x00FFFFFF or (alpha shl 24)
}
