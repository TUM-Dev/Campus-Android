package de.tum.in.tumcampusapp.component.tumui.feedback;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.Feedback;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.FeedbackResult;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.ImageUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

public class FeedbackPresenter implements FeedbackContract.Presenter {

    static final int REQUEST_TAKE_PHOTO = 11;
    static final int REQUEST_GALLERY = 12;
    static final int PERMISSION_LOCATION = 13;
    static final int PERMISSION_CAMERA = 14;
    static final int PERMISSION_FILES = 15;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Call<FeedbackResult> sendFeedbackCall;
    private List<Call<FeedbackResult>> sendImagesCalls = new ArrayList<>();

    private FeedbackContract.View view;

    private final Context context;
    private TUMCabeClient tumCabeClient;

    private String lrzId;
    private String currentPhotoPath;
    private int imagesSent;

    private Feedback feedback = new Feedback();

    @Inject
    public FeedbackPresenter(Context context, String lrzId, TUMCabeClient tumCabeClient) {
        this.context = context;
        this.lrzId = lrzId;
        this.tumCabeClient = tumCabeClient;
    }

    @Override
    public void attachView(@NotNull FeedbackContract.View view) {
        this.view = view;

        compositeDisposable.add(view.getMessage().subscribe(feedback::setMessage));
        compositeDisposable.add(view.getTopicInput().subscribe(this::updateFeedbackTopic));

        compositeDisposable.add(view.getIncludeEmail().subscribe(this::onIncludeEmailChanged));
        compositeDisposable.add(view.getIncludeLocation().subscribe(this::onIncludeLocationChanged));
        if (SDK_INT < M || checkPermission(ACCESS_FINE_LOCATION)) {
            listenForLocation();
        }
    }

    public void listenForLocation() {
        compositeDisposable.add(view.getLocation().subscribe(feedback::setLocation));
    }

    private void updateFeedbackTopic(int topicButton) {
        if (topicButton == R.id.tumInGeneralRadioButton) {
            feedback.setTopic(Const.FEEDBACK_TOPIC_GENERAL);
        } else {
            feedback.setTopic(Const.FEEDBACK_TOPIC_APP);
        }
    }

    private void onIncludeLocationChanged(boolean includeLocation) {
        feedback.setIncludeLocation(includeLocation);
        if (includeLocation && (SDK_INT < M || checkPermission(ACCESS_FINE_LOCATION))) {
            listenForLocation();
        }
    }

    private void onIncludeEmailChanged(boolean includeEmail) {
        feedback.setIncludeEmail(includeEmail);
        view.showEmailInput(includeEmail && lrzId.isEmpty());
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(Const.FEEDBACK)) {
            feedback = savedInstanceState.getParcelable(Const.FEEDBACK);

            if (feedback != null && feedback.getMessage() != null) {
                view.setFeedback(feedback.getMessage());
            }
        }
    }

    @Override
    public void initEmail() {
        final boolean hasLrzId = lrzId != null && !lrzId.isEmpty();
        feedback.setIncludeEmail(hasLrzId);

        if (hasLrzId) {
            feedback.setEmail(lrzId + "@mytum.de");
        }
    }

    @NotNull
    @Override
    public Feedback getFeedback() {
        return feedback;
    }

    @NotNull
    @Override
    public String getLrzId() {
        return lrzId;
    }

    @Override
    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) == null) {
            return;
        }

        // Create the file where the photo should go
        File photoFile = null;
        try {
            photoFile = ImageUtils.createImageFile(context);
            currentPhotoPath = photoFile.getAbsolutePath();
        } catch (IOException e) {
            Utils.log(e);
        }

        if (photoFile == null) {
            return;
        }

        String authority = "de.tum.in.tumcampusapp.fileprovider";
        Uri photoURI = FileProvider.getUriForFile(context, authority, photoFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

        view.openCamera(takePictureIntent);
    }

    @Override
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        Intent chooser = Intent.createChooser(intent, "Select file");
        view.openGallery(chooser);
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onSendFeedback() {
        if (TextUtils.isEmpty(feedback.getMessage())) {
            view.showEmptyMessageError();
        } else {
            view.showSendConfirmationDialog();
        }
    }

    @Override
    public void onConfirmSend() {
        if (!feedback.getIncludeEmail()) {
            feedback.setEmail(null);
        }

        if (!feedback.getIncludeLocation()) {
            feedback.setLatitude(null);
            feedback.setLongitude(null);
        } else {
            if (feedback.getLocation() == null) {
                showNoLocationAccessDialog();
                return;
            }
        }

        final int images = feedback.getPicturePaths().size();
        feedback.setImageCount(images);

        imagesSent = 0;

        if (feedback.getIncludeEmail() && !isEmailValid(feedback.getEmail())) {
            view.showWarning(context.getString(R.string.invalid_email));
            return;
        }

        view.showProgressDialog();
        sendFeedbackCall = tumCabeClient.sendFeedback(feedback);
        sendFeedbackCall.enqueue(new Callback<FeedbackResult>() {
            @Override
            public void onResponse(@NotNull Call<FeedbackResult> call,
                                   @NotNull Response<FeedbackResult> response) {
                FeedbackResult result = response.body();
                if (result == null || result.isSuccess()) {
                    view.showSendErrorDialog();
                }

                if (feedback.getImageCount() == 0) {
                    view.onFeedbackSent();
                } else {
                    sendImages();
                }
            }

            @Override
            public void onFailure(@NotNull Call<FeedbackResult> call, @NotNull Throwable t) {
                if (!call.isCanceled()) {
                    view.showSendErrorDialog();
                }
            }
        });
    }

    private void showNoLocationAccessDialog() {
        String title = context.getString(R.string.location_services_off_title);
        String message = context.getString(R.string.location_services_off_message);
        view.showDialog(title, message);
    }

    private void sendImages() {
        String[] imagePaths = feedback.getPicturePaths().toArray(new String[0]);
        sendImagesCalls = tumCabeClient.sendFeedbackImages(feedback, imagePaths);

        for (Call<FeedbackResult> call : sendImagesCalls) {
            call.enqueue(new Callback<FeedbackResult>() {
                @Override
                public void onResponse(@NonNull Call<FeedbackResult> call,
                                       @NonNull Response<FeedbackResult> response) {
                    FeedbackResult result = response.body();
                    if (result == null || !result.isSuccess()) {
                        view.showSendErrorDialog();
                        return;
                    }

                    imagesSent++;

                    if (imagesSent == feedback.getImageCount()) {
                        view.onFeedbackSent();
                    }

                    Utils.log("Sent " + imagesSent + " of " + (feedback.getImageCount()) + " images");
                }

                @Override
                public void onFailure(@NonNull Call<FeedbackResult> call, @NonNull Throwable t) {
                    if (!call.isCanceled()) {
                        view.showSendErrorDialog();
                    }
                }
            });
        }
    }

    @Override
    public void removeImage(@NotNull String path) {
        final int index = feedback.getPicturePaths().indexOf(path);
        feedback.getPicturePaths().remove(path);
        new File(path).delete();
        view.onImageRemoved(index);
    }

    /**
     * @return true if user has given permission before
     */
    @RequiresApi(api = M)
    private boolean checkPermission(String permission) {
        final int permissionCheck = ContextCompat.checkSelfPermission(context, permission);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            int requestCode;
            switch (permission) {
                case READ_EXTERNAL_STORAGE:
                    requestCode = FeedbackPresenter.PERMISSION_FILES;
                    break;
                case CAMERA:
                    requestCode = FeedbackPresenter.PERMISSION_CAMERA;
                    break;
                default:
                    requestCode = FeedbackPresenter.PERMISSION_LOCATION;
                    break;
            }
            view.showPermissionRequestDialog(permission, requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onImageOptionSelected(int option) {
        if (option == 0) {
            if (SDK_INT < M || checkPermission(CAMERA)) {
                takePicture();
            }
        } else {
            if (SDK_INT < M || checkPermission(READ_EXTERNAL_STORAGE)) {
                openGallery();
            }
        }
    }

    @Override
    public void onNewImageTaken() {
        ImageUtils.rescaleBitmap(context, currentPhotoPath);
        feedback.getPicturePaths().add(currentPhotoPath);
        view.onImageAdded(currentPhotoPath);
    }

    @Override
    public void onNewImageSelected(@Nullable Uri uri) {
        String filePath = ImageUtils.rescaleBitmap(context, uri);
        if (filePath == null) {
            return;
        }

        feedback.getPicturePaths().add(filePath);
        view.onImageAdded(filePath);
    }

    @Override
    public void detachView() {
        clearPictures();

        if (sendFeedbackCall != null) {
            sendFeedbackCall.cancel();
        }

        for (Call<FeedbackResult> call : sendImagesCalls) {
            call.cancel();
        }

        compositeDisposable.dispose();
        view = null;
    }

    private void clearPictures() {
        for (String path : feedback.getPicturePaths()) {
            new File(path).delete();
        }
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        outState.putParcelable(Const.FEEDBACK, feedback);
    }

}
