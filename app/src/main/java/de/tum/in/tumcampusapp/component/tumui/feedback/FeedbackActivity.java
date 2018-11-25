package de.tum.in.tumcampusapp.component.tumui.feedback;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.other.locations.LocationProvider;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.Feedback;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.Success;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.ImageUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class FeedbackActivity extends BaseActivity {

    private static final int GENERAL_FEEDBACK = 0;
    private static final int TCA_FEEDBACK = 1;

    private static final int REQUEST_TAKE_PHOTO = 11;
    private static final int REQUEST_GALLERY = 12;
    private static final int PERMISSION_LOCATION = 13;
    private static final int PERMISSION_CAMERA = 14;
    private static final int PERMISSION_FILES = 15;

    private CheckBox includeEmail, includeLocation;
    private TextInputLayout customEmailViewLayout;
    private TextInputEditText feedbackView, customEmailView;

    private String mCurrentPhotoPath;
    private int feedbackTopic;
    private String email;
    private String lrzId;

    private ArrayList<String> picturePaths = new ArrayList<>();

    private RecyclerView.Adapter<?> thumbnailsAdapter;

    private int sentCount;
    private Dialog progress;
    private Dialog errorDialog;

    @Inject
    TUMCabeClient tumCabeClient;

    @Inject
    LocationProvider locationProvider;

    public FeedbackActivity() {
        super(R.layout.activity_feedback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getInjector().inject(this);

        feedbackTopic = GENERAL_FEEDBACK; // General feedback by default

        feedbackView = findViewById(R.id.feedback_message);
        customEmailViewLayout = findViewById(R.id.feedback_custom_email_layout);
        customEmailView = findViewById(R.id.feedback_custom_email);
        includeEmail = findViewById(R.id.feedback_include_email);
        includeLocation = findViewById(R.id.feedback_include_location);

        lrzId = Utils.getSetting(this, Const.LRZ_ID, "");

        initRadioGroup(savedInstanceState);
        initIncludeLocation(savedInstanceState);
        initIncludeEmail(savedInstanceState);

        if (savedInstanceState != null) {
            picturePaths = savedInstanceState.getStringArrayList(Const.FEEDBACK_PIC_PATHS);
            feedbackView.setText(savedInstanceState.getString(Const.FEEDBACK_MESSAGE));
        }

        RecyclerView pictureList = findViewById(R.id.feedback_image_list);
        pictureList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        thumbnailsAdapter = new FeedbackThumbnailsAdapter(picturePaths);
        pictureList.setAdapter(thumbnailsAdapter);
    }

    @SuppressLint("NewApi")
    private void initIncludeLocation(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            includeLocation.setChecked(savedInstanceState.getBoolean(Const.FEEDBACK_INCL_LOCATION));
        } else {
            includeLocation.setChecked(false);
        }
    }

    private void initIncludeEmail(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (!TextUtils.isEmpty(lrzId)) {
                email = lrzId + "@mytum.de";
                includeEmail.setText(getResources().getString(R.string.feedback_include_email_tum_id, email));
                includeEmail.setChecked(true);
            } else {
                includeEmail.setChecked(false);
            }
        } else {
            includeEmail.setChecked(savedInstanceState.getBoolean(Const.FEEDBACK_INCL_EMAIL));
            email = savedInstanceState.getString(Const.FEEDBACK_EMAIL);
            onIncludeEmailClick();
        }

        includeEmail.setOnClickListener(view -> onIncludeEmailClick());
    }

    private void onIncludeEmailClick() {
        if (includeEmail.isChecked()) {
            if (TextUtils.isEmpty(lrzId)) {
                customEmailViewLayout.setVisibility(View.VISIBLE);
                customEmailView.setText(email);
            }
        } else {
            customEmailViewLayout.setVisibility(View.GONE);
        }
    }

    private void initRadioGroup(Bundle savedInstanceState) {
        RadioGroup radioGroup = findViewById(R.id.radioButtonsGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            feedbackTopic = (checkedId == R.id.tumInGeneralRadioButton) ? GENERAL_FEEDBACK : TCA_FEEDBACK;
        });

        if (savedInstanceState != null) {
            int feedbackTopic = savedInstanceState.getInt(Const.FEEDBACK_TOPIC);
            int radioButtonId = (feedbackTopic == GENERAL_FEEDBACK) ? R.id.tumInGeneralRadioButton : R.id.tumCampusAppRadioButton;
            radioGroup.check(-1); // Clear the selection
            radioGroup.check(radioButtonId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (String path : picturePaths) {
            new File(path).delete();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Const.FEEDBACK_TOPIC, feedbackTopic);
        outState.putString(Const.FEEDBACK_MESSAGE, feedbackView.getText().toString());
        outState.putStringArrayList(Const.FEEDBACK_PIC_PATHS, picturePaths);
        outState.putBoolean(Const.FEEDBACK_INCL_EMAIL, includeEmail.isChecked());
        outState.putBoolean(Const.FEEDBACK_INCL_LOCATION, includeLocation.isChecked());
        outState.putString(Const.FEEDBACK_EMAIL, customEmailView.getText().toString());
    }

    private boolean isValidEmail() {
        email = customEmailView.getText().toString();
        boolean isValid = Patterns.EMAIL_ADDRESS.matcher(email).matches();

        if (isValid) {
            customEmailView.setTextColor(ContextCompat.getColor(this, R.color.valid));
        } else {
            customEmailView.setTextColor(ContextCompat.getColor(this, R.color.error));
        }

        return isValid;
    }

    private Feedback createFeedback() {
        String topic = feedbackTopic == 0 ? Const.FEEDBACK_TOPIC_GENERAL : Const.FEEDBACK_TOPIC_APP;
        int imageCount = picturePaths == null ? 0 : picturePaths.size();

        double latitude = 0.0;
        double longitude = 0.0;

        if (includeLocation.isChecked()) {
            Location location = locationProvider.getLastLocation();
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        }

        return new Feedback(
                UUID.randomUUID().toString(),
                topic,
                feedbackView.getText().toString(),
                (includeEmail.isChecked() ? email : ""),
                latitude,
                longitude,
                imageCount,
                Build.VERSION.RELEASE,
                BuildConfig.VERSION_NAME
        );
    }

    public void onSendClicked(View view) {
        if (feedbackView.getText().toString().trim().isEmpty()) {
            if (picturePaths.isEmpty()) {
                feedbackView.setError(getString(R.string.feedback_empty));
            } else {
                feedbackView.setError(getString(R.string.feedback_img_without_text));
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.send_feedback_question)
                .setPositiveButton(R.string.send, (dialogInterface, i) -> sendFeedback())
                .setNegativeButton(R.string.no, null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        dialog.show();
    }

    public void sendFeedback() {
        sentCount = 0;

        if (includeEmail.isChecked() && TextUtils.isEmpty(lrzId) && !isValidEmail()) {
            return;
        }

        showProgressBarDialog();

        String[] paths = picturePaths.toArray(new String[0]);
        tumCabeClient.sendFeedback(createFeedback(), paths, new Callback<Success>() {
            @Override
            public void onResponse(@NonNull Call<Success> call,
                                   @NonNull Response<Success> response) {
                Success success = response.body();
                if (response.isSuccessful() && success != null && success.wasSuccessfullySent()) {
                    handleFeedbackSuccess();
                } else {
                    showErrorDialog();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Success> call, @NonNull Throwable t) {
                showErrorDialog();
            }
        });
    }

    private void handleFeedbackSuccess() {
        sentCount++;
        if (sentCount == picturePaths.size() + 1) {
            Utils.showToast(this, R.string.feedback_send_success);
            progress.cancel();
            finish();
        }
    }

    private void showProgressBarDialog() {
        progress = new AlertDialog.Builder(this)
                .setTitle(R.string.feedback_sending)
                .setView(new ProgressBar(this))
                .setCancelable(false)
                .setNeutralButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                .create();

        if (progress.getWindow() != null) {
            progress.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        progress.show();
    }

    private void showErrorDialog() {
        if (errorDialog != null && errorDialog.isShowing()) {
            return;
        }
        progress.cancel();

        errorDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.feedback_sending_error)
                .setIcon(R.drawable.ic_error_outline)
                .setPositiveButton(R.string.try_again, (dialogInterface, i) -> sendFeedback())
                .setNegativeButton(R.string.cancel, null)
                .create();

        if (errorDialog.getWindow() != null) {
            errorDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        errorDialog.show();
    }

    @SuppressLint("NewApi")
    public void showAddPictureDialog(View view) {
        String[] options = {
                getString(R.string.feedback_take_picture),
                getString(R.string.gallery)
        };

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.feedback_add_picture)
                .setItems(options, (dialogInterface, index) -> {
                    if (index == 0) {
                        startTakingPicture();
                    } else {
                        openGallery();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_TAKE_PHOTO) {
            File imageFile = new File(mCurrentPhotoPath);
            Uri imageUri = Uri.fromFile(imageFile);
            onNewImageAvailable(imageFile, imageUri);
        } else if (requestCode == REQUEST_GALLERY) {
            File destination = ImageUtils.createImageFile(this);
            if (destination != null) {
                onNewImageAvailable(destination, data.getData());
            }
        }
    }

    private void onNewImageAvailable(File image, Uri uri) {
        mCurrentPhotoPath = image.getAbsolutePath();
        ImageUtils.rescaleBitmapAndSaveToFile(this, uri, image);
        picturePaths.add(image.getAbsolutePath());
        thumbnailsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION: {
                boolean hasLocationPermission = grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED;
                includeLocation.setChecked(hasLocationPermission);
                return;
            }
            case PERMISSION_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    startTakingPicture();
                }
                return;
            }
            case PERMISSION_FILES: {
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    openGallery();
                }
                return;
            }
            default:
                break;
        }
    }

    private void startTakingPicture() {
        // TODO
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA }, PERMISSION_CAMERA);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File image = ImageUtils.createImageFile(this);
            if (image != null) {
                mCurrentPhotoPath = image.getAbsolutePath();
                Uri photoURI = ImageUtils.getImageUri(this, image);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void openGallery() {
        // TODO
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, PERMISSION_CAMERA);
            return;
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_GALLERY);
    }
}
