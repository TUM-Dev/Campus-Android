package de.tum.`in`.tumcampusapp.component.other.generic

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.fragment_image_view_touch.*

class ImageViewTouchFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_image_view_touch, container, false)

    fun loadImage(url: String, onImageLoadingError: () -> Unit) {
        Utils.log("room finder url: $url")

        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_outline_map_24px)
        icon?.setTint(Color.WHITE)

        Picasso.get()
                .load(url)
                .placeholder(icon!!)
                .into(photoView, object : Callback {
                    override fun onSuccess() {
                        // Left empty on purpose
                    }

                    override fun onError(e: Exception) {
                        onImageLoadingError()
                    }
                })
    }

    companion object {

        fun newInstance(): ImageViewTouchFragment {
            return ImageViewTouchFragment()
        }
    }
}
