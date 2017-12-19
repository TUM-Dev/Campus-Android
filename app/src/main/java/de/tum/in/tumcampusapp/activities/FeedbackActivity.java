package de.tum.in.tumcampusapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.common.base.Strings;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.adapters.FeedbackThumbAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.LocationManager;

public class FeedbackActivity extends BaseActivity {

    private static final int REQUEST_TAKE_PHOTO = 11;
    private static final int REQUEST_GALLERY = 12;
    private static final int PERMISSION_LOCATION = 13;
    private static final int PERMISSION_CAMERA = 14;
    private static final int PERMISSION_FILES = 15;

    private CheckBox includeEmail, includeLocation;
    private EditText feedbackView, customEmailView;

    private String mCurrentPhotoPath;
    private int feedbackTopic;
    private String email;
    private String lrzId;
    private Location location;

    private ArrayList<String> picPaths;

    private RecyclerView.Adapter thumbAdapter;

    public FeedbackActivity() {
        super(R.layout.activity_feedback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        feedbackTopic = 0; // General feedback by default

        feedbackView = findViewById(R.id.feedback_message);
        customEmailView = findViewById(R.id.feedback_custom_email);
        includeEmail = findViewById(R.id.feedback_include_email);
        includeLocation = findViewById(R.id.feedback_include_location);

        lrzId = Utils.getSetting(this, Const.LRZ_ID, "");

        initTopicSpinner(savedInstanceState);
        initIncludeLocation(savedInstanceState);
        initIncludeEmail(savedInstanceState);

        if(savedInstanceState == null){
            picPaths = new ArrayList<>();
        } else {
            picPaths = savedInstanceState.getStringArrayList(Const.FEEDBACK_PIC_PATHS);
            feedbackView.setText(savedInstanceState.getString(Const.FEEDBACK_MESSAGE));
        }

        RecyclerView pictureList = findViewById(R.id.feedback_image_list);
        pictureList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
        thumbAdapter = new FeedbackThumbAdapter(picPaths);
        pictureList.setAdapter(thumbAdapter);
    }

    @SuppressLint("NewApi")
    private void initIncludeLocation(Bundle savedInstanceState){
        includeLocation.setOnClickListener(view -> {
            if(includeLocation.isChecked() && location == null
               && checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
                saveLocation();
            }
        });

        if(savedInstanceState != null){
            includeLocation.setChecked(savedInstanceState.getBoolean(Const.FEEDBACK_INCL_LOCATION));
        } else {
            includeLocation.setChecked(true);
        }

        if(includeLocation.isChecked()
           && checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
            saveLocation();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    /**
     * @return true if user has given permission before
     */
    private boolean checkPermission(String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            int requestCode;
            switch(permission){
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                    requestCode = PERMISSION_FILES;
                    break;
                case Manifest.permission.CAMERA:
                    requestCode = PERMISSION_CAMERA;
                    break;
                default: requestCode = PERMISSION_LOCATION;
                    break;
            }
            requestPermissions(new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }

    private void saveLocation(){
        location = new LocationManager(this).getLastLocation();
        if(location == null){
            includeLocation.setChecked(false);
        }
    }

    private void initIncludeEmail(Bundle savedInstanceState){
        if(savedInstanceState == null){
            if(!Strings.isNullOrEmpty(lrzId)){
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

    private void onIncludeEmailClick(){
        if(includeEmail.isChecked()){
            if(Strings.isNullOrEmpty(lrzId)){
                customEmailView.setVisibility(View.VISIBLE);
                customEmailView.setText(email);
            }
        } else {
            customEmailView.setVisibility(View.GONE);
        }
    }

    private void initTopicSpinner(Bundle savedInstanceState){
        Spinner spinner = findViewById(R.id.feedback_topic_spinner);
        spinner.requestFocus();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.feedback_options, R.layout.spinner_item_bold);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                feedbackTopic = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                feedbackTopic = 0;
            }
        });

        if(savedInstanceState != null){
            spinner.getOnItemSelectedListener().onItemSelected(null, null, savedInstanceState.getInt(Const.FEEDBACK_TOPIC), 0);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        for(String path: picPaths){
            new File(path).delete();
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
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startTakingPicture();
                }
                return;
            }
            case PERMISSION_FILES: {
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openGallery();
                }
                return;
            }
            default: // don't do anything
        }
    }

    private boolean isValidEmail(){
        String email = customEmailView.getText().toString();
        boolean isValid = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        if(isValid){
            customEmailView.setTextColor(getResources().getColor(R.color.valid));
        } else {
            customEmailView.setTextColor(getResources().getColor(R.color.error));
        }
        return isValid;
    }

    public void sendFeedback(View view){
        if(includeEmail.isChecked() && Strings.isNullOrEmpty(lrzId) && !isValidEmail()){
            return;
        }

        // TODO send the feedback data to the server
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(Const.FEEDBACK_TOPIC, feedbackTopic);
        outState.putString(Const.FEEDBACK_MESSAGE, feedbackView.getText().toString());
        outState.putStringArrayList(Const.FEEDBACK_PIC_PATHS, picPaths);
        outState.putBoolean(Const.FEEDBACK_INCL_EMAIL, includeEmail.isChecked());
        outState.putBoolean(Const.FEEDBACK_INCL_LOCATION, includeLocation.isChecked());
        outState.putString(Const.FEEDBACK_EMAIL, customEmailView.getText().toString());
    }

    @SuppressLint("NewApi")
    public void addPicture(View view){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.feedback_add_picture);
        dialog.setIcon(R.drawable.ic_add_photo_colored);
        String[] options = {getString(R.string.feedback_take_picture), getString(R.string.gallery)};
        dialog.setItems(options, (dialogInterface, i) -> {
            if(i == 0){
                if(checkPermission(Manifest.permission.CAMERA)){
                    startTakingPicture();
                }
            } else {
                if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)){
                    openGallery();
                }
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if (requestCode == REQUEST_TAKE_PHOTO) {
                picPaths.add(mCurrentPhotoPath);
                thumbAdapter.notifyDataSetChanged();
            } else if(requestCode == REQUEST_GALLERY) {
                File destination = null;

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, Const.FEEDBACK_IMG_COMPRESSION_QUALITY, out);
                    destination = createImageFile();
                    FileOutputStream fileOut = new FileOutputStream(destination);
                    fileOut.write(out.toByteArray());
                    fileOut.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (destination != null) {
                    picPaths.add(destination.getAbsolutePath());
                    thumbAdapter.notifyDataSetChanged();
                }
            }
        }

    }

    private void startTakingPicture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
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

    private void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_GALLERY);
    }
}
