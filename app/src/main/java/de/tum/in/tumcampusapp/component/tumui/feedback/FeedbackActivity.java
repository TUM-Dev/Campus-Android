package de.tum.in.tumcampusapp.component.tumui.feedback;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.common.base.Strings;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.other.locations.LocationManager;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.Feedback;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.Success;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private Location location;
    private LocationListener locationListener;
    private LocationManager locationManager;

    private ArrayList<String> picturePaths;

    private RecyclerView.Adapter thumbnailsAdapter;

    private int sentCount;
    private Dialog progress;
    private Dialog errorDialog;

    public FeedbackActivity() {
        super(R.layout.activity_feedback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        if (savedInstanceState == null) {
            picturePaths = new ArrayList<>();
        } else {
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
        includeLocation.setOnClickListener(view -> {
            if (includeLocation.isChecked() && checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                saveLocation();
            } else if (!includeLocation.isChecked()) {
                stopListeningForLocation();
            }
        });

        if (savedInstanceState != null) {
            includeLocation.setChecked(savedInstanceState.getBoolean(Const.FEEDBACK_INCL_LOCATION));
        } else {
            includeLocation.setChecked(false);
        }

        if (includeLocation.isChecked()
            && checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            saveLocation();
        }
    }

    /**
     * @return true if user has given permission before
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkPermission(String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            int requestCode;
            switch (permission) {
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                    requestCode = PERMISSION_FILES;
                    break;
                case Manifest.permission.CAMERA:
                    requestCode = PERMISSION_CAMERA;
                    break;
                default:
                    requestCode = PERMISSION_LOCATION;
                    break;
            }
            requestPermissions(new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }

    private void saveLocation() {
        Utils.log("saveLocation");

        locationManager = new LocationManager(this);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location gps) {
                location = gps; // just take the newest location
                Utils.log("location (" + gps.getProvider() + "): " + location.getLatitude() + " " + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
                Utils.log("Provider " + s + " disabled");
            }
        };
        locationManager.getLocationUpdates(locationListener);

        // if the feedback is sent before we received a location
        getBackupLocation();
    }

    private void stopListeningForLocation() {
        Utils.log("Stop listening for location");
        if (locationManager != null && locationListener != null) {
            locationManager.stopReceivingUpdates(locationListener);
        }
    }

    private void getBackupLocation() {
        Location backup = new LocationManager(this).getLastLocation();
        if (backup != null) {
            location = backup;
        }
        if (location == null) { // we don't know anything about the location
            includeLocation.setChecked(false);

            AlertDialog dialog = new AlertDialog.Builder(this)
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

    private void initIncludeEmail(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (!Strings.isNullOrEmpty(lrzId)) {
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
    protected void onStop() {
        super.onStop();
        stopListeningForLocation();
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
        outState.putString(Const.FEEDBACK_MESSAGE, feedbackView.getText()
                                                               .toString());
        outState.putStringArrayList(Const.FEEDBACK_PIC_PATHS, picturePaths);
        outState.putBoolean(Const.FEEDBACK_INCL_EMAIL, includeEmail.isChecked());
        outState.putBoolean(Const.FEEDBACK_INCL_LOCATION, includeLocation.isChecked());
        outState.putString(Const.FEEDBACK_EMAIL, customEmailView.getText()
                                                                .toString());
    }

    private boolean isValidEmail() {
        email = customEmailView.getText()
                               .toString();
        boolean isValid = Patterns.EMAIL_ADDRESS.matcher(email)
                                                .matches();
        if (isValid) {
            customEmailView.setTextColor(getResources().getColor(R.color.valid));
        } else {
            customEmailView.setTextColor(getResources().getColor(R.color.error));
        }
        return isValid;
    }

    private Feedback getFeedback() {
        return new Feedback(UUID.randomUUID()
                                .toString(),
                            (feedbackTopic == 0 ? Const.FEEDBACK_TOPIC_GENERAL : Const.FEEDBACK_TOPIC_APP),
                            feedbackView.getText()
                                        .toString(),
                            (includeEmail.isChecked() ? email : ""),
                            (includeLocation.isChecked() ? location.getLatitude() : 0),
                            (includeLocation.isChecked() ? location.getLongitude() : 0),
                            picturePaths == null ? 0 : picturePaths.size(),
                            Build.VERSION.RELEASE,
                            BuildConfig.VERSION_NAME);
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
        stopListeningForLocation();

        if (includeEmail.isChecked() && Strings.isNullOrEmpty(lrzId) && !isValidEmail()) {
            return;
        }

        showProgressBarDialog();
        TUMCabeClient client = TUMCabeClient.getInstance(this);

        client.sendFeedback(getFeedback(), picturePaths.toArray(new String[picturePaths.size()]), new Callback<Success>() {
            @Override
            public void onResponse(Call<Success> call, Response<Success> response) {
                Success success = response.body();
                if (success != null && success.wasSuccessfullySent()) {
                    sentCount++;
                    Utils.log(success.getSuccess());
                    if (sentCount == picturePaths.size() + 1) {
                        progress.cancel();
                        finish();
                        Toast.makeText(feedbackView.getContext(), R.string.feedback_send_success, Toast.LENGTH_SHORT)
                             .show();
                    }
                    Utils.log("sent " + sentCount + " of " + (picturePaths.size() + 1) + " message parts");
                } else {
                    showErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<Success> call, Throwable t) {
                showErrorDialog();
            }
        });
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
                .setNegativeButton(R.string.ok, null)
                .create();

        if (errorDialog.getWindow() != null) {
            errorDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        errorDialog.show();
    }

    @SuppressLint("NewApi")
    public void addPicture(View view) {
        String[] options = {getString(R.string.feedback_take_picture), getString(R.string.gallery)};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.feedback_add_picture)
                .setItems(options, (dialogInterface, i) -> {
                    if (i == 0) {
                        if (checkPermission(Manifest.permission.CAMERA)) {
                            startTakingPicture();
                        }
                    } else {
                        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            openGallery();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                // get picture, resize it and write back the image
                rescaleBitmap(Uri.fromFile(new File(mCurrentPhotoPath)), new File(mCurrentPhotoPath));

                picturePaths.add(mCurrentPhotoPath);
                thumbnailsAdapter.notifyDataSetChanged();
            } else if (requestCode == REQUEST_GALLERY) {

                File destination = null;
                try {
                    destination = createImageFile();
                } catch (IOException e) {
                    Utils.log(e);
                }
                rescaleBitmap(data.getData(), destination);

                if (destination != null) {
                    picturePaths.add(destination.getAbsolutePath());
                    thumbnailsAdapter.notifyDataSetChanged();
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    location = new LocationManager(this).getLastLocation();
                    includeLocation.setChecked(true);
                } else {
                    includeLocation.setChecked(false);
                }
                return;
            }
            case PERMISSION_CAMERA: {
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTakingPicture();
                }
                return;
            }
            case PERMISSION_FILES: {
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                }
                return;
            }
            default: // don't do anything
        }
    }

    /**
     * scales down the image and writes it to the destination file
     *
     * @param src
     * @param destination
     */
    private void rescaleBitmap(Uri src, File destination) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), src);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Utils.log("img before: " + bitmap.getWidth() + " x " + bitmap.getHeight());
            bitmap = getResizedBitmap(bitmap, 1000);
            Utils.log("img after: " + bitmap.getWidth() + " x " + bitmap.getHeight());
            bitmap.compress(Bitmap.CompressFormat.JPEG, Const.FEEDBACK_IMG_COMPRESSION_QUALITY, out);
            FileOutputStream fileOut = new FileOutputStream(destination);
            fileOut.write(out.toByteArray());
            fileOut.close();
            out.close();
        } catch (IOException e) {
            Utils.log(e);
        }
    }

    /**
     * scales the bitmap down if it's bigger than maxSize
     *
     * @param image
     * @param maxSize
     * @return
     */
    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width < maxSize && height < maxSize) {
            return image;
        }

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void startTakingPicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                Utils.log(e);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Utils.log(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
                Uri photoURI = FileProvider.getUriForFile(this,
                                                          "de.tum.in.tumcampusapp.fileprovider",
                                                          photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = DateTimeFormat.forPattern("yyyyMMdd_HHmmss")
                                         .withLocale(Locale.GERMANY)
                                         .print(DateTime.now());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_GALLERY);
    }
}
