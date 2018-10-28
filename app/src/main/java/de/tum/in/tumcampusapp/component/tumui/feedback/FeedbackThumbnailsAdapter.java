package de.tum.in.tumcampusapp.component.tumui.feedback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.utils.Utils;

public class FeedbackThumbnailsAdapter extends RecyclerView.Adapter<FeedbackThumbnailsAdapter.ViewHolder> {
    private ArrayList<String> paths;
    private ArrayList<Bitmap> thumbs;

    private int viewing;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ViewHolder(ImageView img) {
            super(img);
            imageView = img;
        }
    }

    public FeedbackThumbnailsAdapter(ArrayList<String> paths) {
        this.paths = paths;
        thumbs = new ArrayList<>(paths.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView imageView = (ImageView) LayoutInflater.from(parent.getContext())
                                                        .inflate(R.layout.feedback_thumb, parent, false);
        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (thumbs.size() < position + 1 || thumbs.get(position) == null) {
            Bitmap bitmap = createThumb(holder.imageView, position);

            if (bitmap == null) {
                Utils.log("Image removed from feedback (thumbnail couldn't be read)");
                new File(paths.get(position)).delete();
                paths.remove(position);
                thumbs.remove(position);
                notifyItemRemoved(position);
            }

            if (thumbs.size() < position + 1) {
                thumbs.add(bitmap);
            } else {
                thumbs.set(position, bitmap);
            }
        }

        holder.imageView.setImageBitmap(thumbs.get(position));
        holder.imageView.setTag(position);

        holder.imageView.setOnClickListener(view -> {
            viewing = (int) view.getTag();
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

            LinearLayout layout =
                    (LinearLayout) View.inflate(view.getContext(), R.layout.picture_dialog, null);

            ImageView imgView = layout.findViewById(R.id.feedback_big_image);
            imgView.setImageURI(Uri.fromFile(new File(paths.get(viewing))));

            builder.setView(layout);
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.feedback_remove_image, (dialogInterface, i) -> {
                removeImage();
            });

            AlertDialog dialog = builder.create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
            }
            dialog.show();
        });
    }

    private void removeImage() {
        new File(paths.get(viewing)).delete();
        paths.remove(viewing);
        thumbs.remove(viewing);
        notifyItemRemoved(viewing);
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    private Bitmap createThumb(ImageView mImageView, int position) {
        String path = paths.get(position);
        if (path == null) {
            return null;
        }

        int targetSize = (int) mImageView.getContext()
                                         .getResources()
                                         .getDimension(R.dimen.thumbnail_size);

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetSize, photoH / targetSize);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(path, bmOptions);
    }
}
