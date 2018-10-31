package de.tum.in.tumcampusapp.component.other.generic;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.utils.Utils;

public class ImageViewTouchFragment extends Fragment {

    private View mRootView;

    public static ImageViewTouchFragment newInstance() {
        return new ImageViewTouchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_image_view_touch, container, false);
        return mRootView;
    }

    public void loadImage(String url, ImageLoadingListener listener) {
        ImageView imageView = mRootView.findViewById(R.id.image_view_touch_fragment);
        Utils.log("room finder url: " + url);

        Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_outline_map_24px);
        if (icon != null) {
            icon.setTint(Color.WHITE);
        }

        Picasso.get()
                .load(url)
                .placeholder(icon)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Free ad space
                    }

                    @Override
                    public void onError(Exception e) {
                        listener.onImageLoadingError();
                    }
                });
    }

    public interface ImageLoadingListener {
        void onImageLoadingError();
    }

}
