package de.tum.in.tumcampusapp.component.tumui.feedback;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Patterns;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.other.locations.LocationProvider;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.Feedback;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.Success;
import de.tum.in.tumcampusapp.utils.ImageUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Handles choosing images and network operations and corresponding dialogs.
 */
public class FeedbackController {

    static final int REQUEST_TAKE_PHOTO = 11;
    static final int REQUEST_GALLERY = 12;
    static final int PERMISSION_LOCATION = 13;
    static final int PERMISSION_CAMERA = 14;
    static final int PERMISSION_FILES = 15;

    private final Context mContext;
    private int imagesSent;

    private String mCurrentPhotoPath;

    private Dialog progress;
    private Dialog errorDialog;

    public FeedbackController(Context context) {
        mContext = context;
    }

    String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    void startTakingPicture(Activity activity) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) == null) {
            return;
        }
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = ImageUtils.createImageFile(mContext);
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = photoFile.getAbsolutePath();
        } catch (IOException e) {
            Utils.log(e);
        }
        // Continue only if the File was successfully created
        if (photoFile == null) {
            return;
        }
        Utils.log(mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        Uri photoURI = FileProvider.getUriForFile(mContext,
                "de.tum.in.tumcampusapp.fileprovider",
                photoFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
    }

    void openGallery(Activity activity) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        activity.startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_GALLERY);
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    void sendFeedback(Activity activity, Feedback feedback, String lrzId) {
        Location location = getCurrentLocation();
        if (location == null) {
            showNoLocationDialog();
            return;
        }

        // adjust information that is to be sent to the server
        if (!feedback.getIncludeEmail()) {
            feedback.setEmail("");
        }

        if (!feedback.getIncludeLocation()) {
            feedback.setLongitude(0);
            feedback.setLatitude(0);
        } else {
            feedback.setLatitude(location.getLatitude());
            feedback.setLongitude(location.getLongitude());
        }

        feedback.setImageCount(feedback.getPicturePaths().size());

        Utils.log("Feedback: " + feedback.toString());

        imagesSent = 0;

        if (feedback.getIncludeEmail() && !isEmailValid(feedback.getEmail())) {
            Toast.makeText(activity.getApplicationContext(), "Email is not valid", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressBarDialog();

        TUMCabeClient client = TUMCabeClient.getInstance(mContext);
        client.sendFeedback(feedback, new Callback<Success>() {
            @Override
            public void onResponse(@NonNull Call<Success> call, @NonNull Response<Success> response) {
                Success success = response.body();
                if (success == null || !success.wasSuccessfullySent()) {
                    showErrorDialog((dialogInterface, i) -> sendFeedback(activity, feedback, lrzId));
                    return;
                }
                if (feedback.getImageCount() == 0) {
                    onFeedbackSent(activity);
                } else {
                    sendImages(feedback, activity, lrzId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Success> call, @NonNull Throwable t) {
                showErrorDialog((dialogInterface, i) -> sendFeedback(activity, feedback, lrzId));
            }
        });
    }

    private void sendImages(Feedback feedback, Activity activity, String lrzId) {
        TUMCabeClient client = TUMCabeClient.getInstance(mContext);
        client.sendFeedbackImages(feedback, feedback.getPicturePaths().toArray(new String[0]), new Callback<Success>() {
            @Override
            public void onResponse(@NonNull Call<Success> call, @NonNull Response<Success> response) {
                Success success = response.body();
                if (success == null || !success.wasSuccessfullySent()) {
                    showErrorDialog((dialogInterface, i) -> sendFeedback(activity, feedback, lrzId));
                    return;
                }
                imagesSent++;
                if (imagesSent == feedback.getImageCount()) {
                    onFeedbackSent(activity);
                }
                Utils.log("sent " + imagesSent + " of " + (feedback.getImageCount()) + " images");
            }

            @Override
            public void onFailure(@NonNull Call<Success> call, @NonNull Throwable t) {
                showErrorDialog((dialogInterface, i) -> sendFeedback(activity, feedback, lrzId));
            }
        });
    }

    private void onFeedbackSent(Activity activity) {
        progress.cancel();
        activity.finish();
        Toast.makeText(mContext, R.string.feedback_send_success, Toast.LENGTH_SHORT).show();
    }


    /**
     * @return true if user has given permission before
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    boolean checkPermission(String permission, Activity activity) {
        int permissionCheck = ContextCompat.checkSelfPermission(mContext, permission);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            int requestCode;
            switch (permission) {
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                    requestCode = FeedbackController.PERMISSION_FILES;
                    break;
                case Manifest.permission.CAMERA:
                    requestCode = FeedbackController.PERMISSION_CAMERA;
                    break;
                default:
                    requestCode = FeedbackController.PERMISSION_LOCATION;
                    break;
            }
            activity.requestPermissions(new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }

    void showPictureOptionsDialog(Activity activity) {
        String[] options = {
                mContext.getString(R.string.feedback_take_picture),
                mContext.getString(R.string.gallery)
        };
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.feedback_add_picture)
                .setItems(options, (dialogInterface, i) -> {
                    if (i == 0) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                                || checkPermission(Manifest.permission.CAMERA, activity)) {
                            startTakingPicture(activity);
                        }
                    } else {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                                || checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, activity)) {
                            openGallery(activity);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }
        dialog.show();
    }

    private void showProgressBarDialog() {
        progress = new AlertDialog.Builder(mContext)
                .setTitle(R.string.feedback_sending)
                .setView(new ProgressBar(mContext))
                .setCancelable(false)
                .setNeutralButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                .create();

        if (progress.getWindow() != null) {
            progress.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }
        progress.show();
    }

    private void showErrorDialog(DialogInterface.OnClickListener onTryAgain) {
        if (errorDialog != null && errorDialog.isShowing()) {
            return;
        }
        progress.cancel();

        errorDialog = new AlertDialog.Builder(mContext)
                .setMessage(R.string.feedback_sending_error)
                .setIcon(R.drawable.ic_error_outline)
                .setPositiveButton(R.string.try_again, onTryAgain)
                .setNegativeButton(R.string.ok, null)
                .create();

        if (errorDialog.getWindow() != null) {
            errorDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }
        errorDialog.show();
    }

    @Nullable
    private Location getCurrentLocation() {
        return LocationProvider.getInstance(mContext).getLastLocation();
    }

    private void showNoLocationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.location_services_off_title)
                .setMessage(R.string.location_services_off_message)
                .setPositiveButton(R.string.ok, null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        dialog.show();
    }

}
