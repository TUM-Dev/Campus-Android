package de.tum.in.tumcampusapp.component.tumui.feedback;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.Feedback;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.ImageUtils;
import de.tum.in.tumcampusapp.utils.Utils;

import static de.tum.in.tumcampusapp.component.tumui.feedback.FeedbackController.PERMISSION_CAMERA;
import static de.tum.in.tumcampusapp.component.tumui.feedback.FeedbackController.PERMISSION_FILES;
import static de.tum.in.tumcampusapp.component.tumui.feedback.FeedbackController.PERMISSION_LOCATION;
import static de.tum.in.tumcampusapp.component.tumui.feedback.FeedbackController.REQUEST_GALLERY;
import static de.tum.in.tumcampusapp.component.tumui.feedback.FeedbackController.REQUEST_TAKE_PHOTO;

public class FeedbackActivity extends BaseActivity {

    private CheckBox includeEmail, includeLocation;
    private TextInputLayout customEmailViewLayout;
    private TextInputEditText feedbackView, customEmailView;

    private RecyclerView.Adapter thumbnailsAdapter;

    private FeedbackController controller;
    private Feedback feedback;
    private String lrzId;

    public FeedbackActivity() {
        super(R.layout.activity_feedback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        controller = new FeedbackController(this);

        boolean loadingSavedState = savedInstanceState != null;
        if (loadingSavedState) {
            feedback = savedInstanceState.getParcelable(Const.FEEDBACK);
        } else {
            feedback = new Feedback();
        }

        feedbackView = findViewById(R.id.feedback_message);
        customEmailViewLayout = findViewById(R.id.feedback_custom_email_layout);
        customEmailView = findViewById(R.id.feedback_custom_email);
        includeEmail = findViewById(R.id.feedback_include_email);
        includeLocation = findViewById(R.id.feedback_include_location);

        lrzId = Utils.getSetting(this, Const.LRZ_ID, "");

        feedbackView.setText(feedback.getMessage());
        initFeedbackType();
        initIncludeLocation();
        initIncludeEmail(loadingSavedState);
        initPictureGalley();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Const.FEEDBACK, getFeedback());
    }

    private Feedback getFeedback() {
        if (feedback == null) {
            feedback = new Feedback();
        }
        // get values that we don't observe
        feedback.setMessage(feedbackView.getText().toString());
        if (lrzId == null || lrzId.isEmpty()) {
            feedback.setEmail(customEmailView.getText().toString());
        }
        feedback.setIncludeLocation(includeLocation.isChecked());
        if (controller.getLocation() != null) {
            feedback.setLatitude(controller.getLocation().getLatitude());
            feedback.setLongitude(controller.getLocation().getLongitude());
        }
        return feedback;
    }

    private void initPictureGalley() {
        RecyclerView pictureList = findViewById(R.id.feedback_image_list);
        pictureList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        thumbnailsAdapter = new FeedbackThumbnailsAdapter(feedback.getPicturePaths());
        pictureList.setAdapter(thumbnailsAdapter);

        findViewById(R.id.feedback_add_image).setOnClickListener(
                view -> controller.showPictureOptionsDialog(FeedbackActivity.this));
    }

    @SuppressLint("NewApi")
    private void initIncludeLocation() {
        includeLocation.setOnClickListener(view -> {
            if (includeLocation.isChecked()
                    && controller.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, this)) {
                controller.saveLocation();
            } else if (!includeLocation.isChecked()) {
                controller.stopListeningForLocation();
            }
        });

        includeLocation.setChecked(feedback.getIncludeLocation());
        if (includeLocation.isChecked()
                && controller.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, this)) {
            controller.saveLocation();
        }
    }

    private void initIncludeEmail(boolean loadingSavedState) {
        if (!loadingSavedState) {
            // set initial values based on lrzId availability
            if (lrzId != null && !lrzId.isEmpty()) {
                feedback.setEmail(lrzId + "@mytum.de");
                feedback.setIncludeEmail(true);
            } else {
                feedback.setIncludeEmail(false);
            }
        }

        includeEmail.setChecked(feedback.getIncludeEmail());
        if (TextUtils.isEmpty(lrzId)) {
            includeEmail.setText(R.string.feedback_include_email);
            customEmailView.setText(feedback.getEmail());
        } else {
            includeEmail.setText(getResources()
                                         .getString(R.string.feedback_include_email_tum_id, feedback.getEmail()));
        }
        hideOrShowEmailInput();

        includeEmail.setOnClickListener(view -> {
            feedback.setIncludeEmail(!feedback.getIncludeEmail());
            hideOrShowEmailInput();
        });
    }

    private void hideOrShowEmailInput() {
        if (includeEmail.isChecked()) {
            if (TextUtils.isEmpty(lrzId)) {
                customEmailViewLayout.setVisibility(View.VISIBLE);
            }
        } else {
            customEmailViewLayout.setVisibility(View.GONE);
        }
    }

    private void initFeedbackType() {
        RadioGroup radioGroup = findViewById(R.id.radioButtonsGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            feedback.setTopic(checkedId == R.id.tumInGeneralRadioButton
                    ? Const.FEEDBACK_TOPIC_GENERAL
                    : Const.FEEDBACK_TOPIC_APP);
        });

        int selectedButtonId = (feedback.getTopic().equals(Const.FEEDBACK_TOPIC_GENERAL))
                ? R.id.tumInGeneralRadioButton
                : R.id.tumCampusAppRadioButton;
        radioGroup.check(-1); // Clear the selection
        radioGroup.check(selectedButtonId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.stopListeningForLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (String path : feedback.getPicturePaths()) {
            new File(path).delete();
        }
    }

    public void onSendClicked(View view) {
        getFeedback();

        if (feedback.getMessage().trim().isEmpty()) {
            if (feedback.getPicturePaths().isEmpty()) {
                feedbackView.setError(getString(R.string.feedback_empty));
            } else {
                feedbackView.setError(getString(R.string.feedback_img_without_text));
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.send_feedback_question)
                .setPositiveButton(R.string.send,
                        (dialogInterface, i)
                                -> controller.sendFeedback(this, feedback, lrzId))
                .setNegativeButton(R.string.no, null)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                ImageUtils.rescaleBitmap(this, controller.getCurrentPhotoPath());
                feedback.getPicturePaths().add(controller.getCurrentPhotoPath());
                thumbnailsAdapter.notifyDataSetChanged();
            } else if (requestCode == REQUEST_GALLERY) {
                String filePath = ImageUtils.rescaleBitmap(this, data.getData());
                feedback.getPicturePaths().add(filePath);
                thumbnailsAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    controller.saveLocation();
                    includeLocation.setChecked(true);
                } else {
                    includeLocation.setChecked(false);
                }
                return;
            }
            case PERMISSION_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    controller.startTakingPicture(this);
                }
                return;
            }
            case PERMISSION_FILES: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    controller.openGallery(this);
                }
                return;
            }
            default: // don't do anything
        }
    }
}
