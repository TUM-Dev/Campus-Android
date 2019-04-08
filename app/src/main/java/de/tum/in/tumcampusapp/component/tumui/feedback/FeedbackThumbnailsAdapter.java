package de.tum.in.tumcampusapp.component.tumui.feedback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.RecyclerView;
import de.tum.in.tumcampusapp.R;

public class FeedbackThumbnailsAdapter extends RecyclerView.Adapter<FeedbackThumbnailsAdapter.ViewHolder> {

    private List<String> paths;
    private ArrayMap<String, Bitmap> pathsToThumbnails = new ArrayMap<>();

    private RemoveListener listener;
    private int thumbnailSize;

    FeedbackThumbnailsAdapter(List<String> paths, RemoveListener listener, int thumbnailSize) {
        this.listener = listener;
        this.paths = paths;
        this.thumbnailSize = thumbnailSize;

        for (String path: paths) {
            pathsToThumbnails.put(path, createThumbnail(path, thumbnailSize));
        }
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feedback_thumb, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
        String path = paths.get(position);
        Bitmap thumbnail = pathsToThumbnails.get(path);
        holder.bind(path, thumbnail, listener);
    }

    public void update(List<String> paths) {
        this.paths = paths;
        notifyDataSetChanged();
    }

    void addImage(String path) {
        Bitmap thumbnail = createThumbnail(path, thumbnailSize);
        pathsToThumbnails.put(path, thumbnail);

        final int index = paths.indexOf(path);
        notifyItemInserted(index);
    }

    void removeImage(int position) {
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    private Bitmap createThumbnail(String path, int thumbnailSize) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        final int width = bmOptions.outWidth;
        final int height = bmOptions.outHeight;

        // Determine how much to scale down the image
        final int scaleFactor = Math.min(width / thumbnailSize, height / thumbnailSize);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(path, bmOptions);
    }

    interface RemoveListener {
        void onThumbnailRemoved(String path);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }

        public void bind(String path, Bitmap thumbnail, RemoveListener listener) {
            imageView.setImageBitmap(thumbnail);
            imageView.setTag(getAdapterPosition());
            imageView.setOnClickListener(view -> listener.onThumbnailRemoved(path));
        }

    }

}
