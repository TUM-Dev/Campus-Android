package de.tum.`in`.tumcampusapp.component.other.generic

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.utils.Utils

class ImageViewTouchFragment : Fragment() {

    lateinit var mRootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.fragment_image_view_touch, container, false)
        return mRootView
    }

    fun loadImage(url: String, listener: ImageLoadingListener) {
        val imageView = mRootView.findViewById<ImageView>(R.id.image_view_touch_fragment)
        Utils.log("room finder url: $url")

        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_outline_map_24px)
        icon?.setTint(Color.WHITE)

        Picasso.get()
                .load(url)
                .placeholder(icon!!)
                .into(imageView, object : Callback {
                    override fun onSuccess() {
                        // Left empty on purpose
                    }

                    override fun onError(e: Exception) {
                        listener.onImageLoadingError()
                    }
                })
    }

    interface ImageLoadingListener {
        fun onImageLoadingError()
    }

    companion object {

        fun newInstance(): ImageViewTouchFragment {
            return ImageViewTouchFragment()
        }
    }

}
