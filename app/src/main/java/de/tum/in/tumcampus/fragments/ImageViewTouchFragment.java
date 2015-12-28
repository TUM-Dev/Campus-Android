package de.tum.in.tumcampus.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.tum.in.tumcampus.R;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageViewTouchFragment extends Fragment {

    private static final String BITMAP_ARG = "bitmap";

    public static ImageViewTouchFragment newInstance() {
        return new ImageViewTouchFragment();
    }

    public static ImageViewTouchFragment newInstance(Bitmap bitmap) {
        ImageViewTouchFragment fragment = new ImageViewTouchFragment();
        Bundle args = new Bundle();
        args.putParcelable(BITMAP_ARG, bitmap);
        fragment.setArguments(args);
        return fragment;
    }

    ImageView mImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_view_touch, container, false);
        mImage = (ImageView) view.findViewById(R.id.image_view_touch_fragment);
        new PhotoViewAttacher(mImage);

        if (getArguments() != null) {
            Bitmap bitmap = getArguments().getParcelable(BITMAP_ARG);
            mImage.setImageBitmap(bitmap);
        }

        return view;
    }
}
