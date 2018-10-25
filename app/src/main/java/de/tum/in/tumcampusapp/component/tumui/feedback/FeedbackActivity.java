package de.tum.in.tumcampusapp.component.tumui.feedback;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import com.google.common.base.Strings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    private int feedbackTopic;
    private String email;
    private String lrzId;

    private RecyclerView.Adapter thumbnailsAdapter;
    private List<String> picturePaths;

    private FeedbackController controller;

    public FeedbackActivity() {
        super(R.layout.activity_feedback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        controller = new FeedbackController(this);

        feedbackTopic = FeedbackController.GENERAL_FEEDBACK; // General feedback by default

        feedbackView = findViewById(R.id.feedback_message);
        customEmailViewLayout = findViewById(R.id.feedback_custom_email_layout);
        customEmailView = findViewById(R.id.feedback_custom_email);
        includeEmail = findViewById(R.id.feedback_include_email);
        includeLocation = findViewById(R.id.feedback_include_location);

        lrzId = Utils.getSetting(this, Const.LRZ_ID, "");

        initRadioGroup(savedInstanceState);
        initIncludeLocation(savedInstanceState);
        initIncludeEmail(savedInstanceState);
        initPictureGalley(savedInstanceState);

        if (savedInstanceState != null) {
            feedbackView.setText(savedInstanceState.getString(Const.FEEDBACK_MESSAGE));
        }
    }

    private Feedback getFeedback() {
        Feedback feedback = new Feedback();
        feedback.setTopic(feedbackTopic);
        feedback.setMessage(feedbackView.getText().toString());
        feedback.setEmail(email);
        feedback.setIncludeEmail(includeEmail.isChecked());
        if (includeLocation.isChecked()) {
            feedback.setLatitude(controller.getLocation().getLatitude());
            feedback.setLongitude(controller.getLocation().getLongitude());
        }
        feedback.setPicturePaths(picturePaths);
        return feedback;
    }

    private void initPictureGalley(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            picturePaths = new ArrayList<>();
        } else {
            picturePaths = savedInstanceState.getStringArrayList(Const.FEEDBACK_PIC_PATHS);
        }

        RecyclerView pictureList = findViewById(R.id.feedback_image_list);
        pictureList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        thumbnailsAdapter = new FeedbackThumbnailsAdapter(picturePaths);
        pictureList.setAdapter(thumbnailsAdapter);

        findViewById(R.id.feedback_add_image).setOnClickListener(
                view -> controller.showPictureOptionsDialog(FeedbackActivity.this));
    }


    @SuppressLint("NewApi")
    private void initIncludeLocation(Bundle savedInstanceState) {
        includeLocation.setOnClickListener(view -> {
            if (includeLocation.isChecked()
                    && controller.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, this)) {
                controller.saveLocation();
            } else if (!includeLocation.isChecked()) {
                controller.stopListeningForLocation();
            }
        });

        if (savedInstanceState != null) {
            includeLocation.setChecked(savedInstanceState.getBoolean(Const.FEEDBACK_INCL_LOCATION));
        } else {
            includeLocation.setChecked(false);
        }

        if (includeLocation.isChecked()
                && controller.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, this)) {
            controller.saveLocation();
        }
    }

    private void initIncludeEmail(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (!Strings.isNullOrEmpty(lrzId)) {
                email = lrzId + "@mytum.de";
                includeEmail.setText(getResources()
                        .getString(R.string.feedback_include_email_tum_id, email));
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
            if (Strings.isNullOrEmpty(lrzId)) {
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
            feedbackTopic = (checkedId == R.id.tumInGeneralRadioButton)
                    ? FeedbackController.GENERAL_FEEDBACK
                    : FeedbackController.TCA_FEEDBACK;
        });

        if (savedInstanceState != null) {
            int feedbackTopic = savedInstanceState.getInt(Const.FEEDBACK_TOPIC);
            int radioButtonId = (feedbackTopic == FeedbackController.GENERAL_FEEDBACK)
                    ? R.id.tumInGeneralRadioButton
                    : R.id.tumCampusAppRadioButton;
            radioGroup.check(-1); // Clear the selection
            radioGroup.check(radioButtonId);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.stopListeningForLocation();
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
        outState.putStringArray(Const.FEEDBACK_PIC_PATHS, picturePaths.toArray(new String[0]));
        outState.putBoolean(Const.FEEDBACK_INCL_EMAIL, includeEmail.isChecked());
        outState.putBoolean(Const.FEEDBACK_INCL_LOCATION, includeLocation.isChecked());
        outState.putString(Const.FEEDBACK_EMAIL, customEmailView.getText().toString());
    }

    public void onSendClicked(View view) {
        if (feedbackView.getText()
                .toString()
                .trim()
                .isEmpty()) {
            if (picturePaths.isEmpty()) {
                feedbackView.setError(getString(R.string.feedback_empty));
            } else {
                feedbackView.setError(getString(R.string.feedback_img_without_text));
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.send_feedback_question)
                .setPositiveButton(R.string.send,
                        (dialogInterface, i)
                                -> controller.sendFeedback(this, getFeedback(), lrzId))
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
                picturePaths.add(controller.getCurrentPhotoPath());
                thumbnailsAdapter.notifyDataSetChanged();
            } else if (requestCode == REQUEST_GALLERY) {
                String filePath = ImageUtils.rescaleBitmap(this, data.getData());
                picturePaths.add(filePath);
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
