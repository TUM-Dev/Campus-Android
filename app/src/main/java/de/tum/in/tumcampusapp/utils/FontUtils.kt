package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.util.TypedValue.applyDimension

object FontUtils {

    @JvmStatic
    fun getFontSizeInPx(context: Context, fontSize: Float): Int {
        return applyDimension(COMPLEX_UNIT_SP, fontSize, context.resources.displayMetrics).toInt()
    }

}
