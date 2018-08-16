package de.tum.`in`.tumcampusapp.utils

import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.RequestCreator

/**
 * Executes the block and return null in case of an [Exception].
 *
 * @param block The block of code to execute
 */
inline fun <T> tryOrNull(block: () -> T): T? {
    return try {
        block()
    } catch (_: Exception) {
        null
    }
}

fun RequestCreator.into(target: ImageView, completion: () -> Unit) {
    into(target, object : Callback {
        override fun onSuccess() {
            completion()
        }

        override fun onError(e: java.lang.Exception?) {
            completion()
        }
    })
}
