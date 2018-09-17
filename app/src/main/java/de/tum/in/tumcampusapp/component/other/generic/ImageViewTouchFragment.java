package de.tum.in.tumcampusapp.component.other.generic;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.utils.Utils;

public class ImageViewTouchFragment extends Fragment {

    private static String mURL;
    private static Callback mCallback;
    public static ImageViewTouchFragment newInstance() {
        return new ImageViewTouchFragment();
    }

    public static ImageViewTouchFragment newInstance(String url, Callback callback) {
        mURL = url;
        mCallback = callback;
        return new ImageViewTouchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_view_touch, container, false);
        ImageView imageView = view.findViewById(R.id.image_view_touch_fragment);
        Utils.log("room finder url: " + mURL);

        Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_outline_map_24px);
        if (icon != null) {
            icon.setTint(Color.WHITE);
        }

        Picasso.get()
                .load(mURL)
                .placeholder(icon)
                .into(imageView, mCallback);

        return view;
    }
}
