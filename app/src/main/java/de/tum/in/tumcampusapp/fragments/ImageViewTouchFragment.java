package de.tum.in.tumcampusapp.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

import de.tum.in.tumcampusapp.R;

public class ImageViewTouchFragment extends Fragment {

    private static final String URI_ARG = "uri";

    public static ImageViewTouchFragment newInstance() {
        return new ImageViewTouchFragment();
    }

    public static ImageViewTouchFragment newInstance(File image) {
        ImageViewTouchFragment fragment = new ImageViewTouchFragment();
        Bundle args = new Bundle();
        args.putParcelable(URI_ARG, Uri.fromFile(image));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_view_touch, container, false);
        ImageView mImage = view.findViewById(R.id.image_view_touch_fragment);

        if (getArguments() != null) {
            Uri image = getArguments().getParcelable(URI_ARG);
            mImage.setImageURI(image);
        }

        return view;
    }
}
