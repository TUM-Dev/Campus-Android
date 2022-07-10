package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.squareup.picasso.Transformation

class DrawCordsPointerTransformation(
    private val cordX: Int,
    private val cordY: Int,
    private val pinDrawable: Drawable
) : Transformation {

    override fun transform(source: Bitmap?): Bitmap {
        synchronized(DrawCordsPointerTransformation::class.java) {
            if (source == null) {
                throw IllegalStateException("Bitmap not provided, cannot draw a pointer")
            }
            val resultBitmap = source.copy(source.config, true)
            val canvas = Canvas(resultBitmap)
            pinDrawable.setBounds(cordX - ICON_SIZE, cordY - ICON_SIZE, cordX, cordY)
            pinDrawable.draw(canvas)
            source.recycle()
            return resultBitmap
        }
    }

    override fun key(): String {
        return "cordspointer"
    }

    companion object {
        const val ICON_SIZE = 40
    }
}
