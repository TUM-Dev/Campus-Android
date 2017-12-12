package de.tum.in.tumcampusapp.activities;

import android.Manifest;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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

        EditText feedbackView = findViewById(R.id.feedback_message);
        feedbackView.clearFocus();
        if(savedInstanceState != null){
            feedbackView.setText(savedInstanceState.getString(Const.FEEDBACK_MESSAGE));
        }

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

        CheckBox includeLocationBox = findViewById(R.id.feedback_include_location);
        includeLocationBox.setOnClickListener(view -> {
            CheckBox box = (CheckBox) view;
            if(box.isChecked() && location == null){

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR);
                    if( permissionCheck == PackageManager.PERMISSION_DENIED){
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
                    }
                }

                location = new LocationManager(view.getContext()).getLastLocation();
                if(location == null){
                    box.setChecked(false);
                }
            }
        });

        lrzId = Utils.getSetting(this, Const.LRZ_ID, "");

        if(!"".equals(lrzId)){
            email = lrzId + "@mytum.de";
        } else {
            if(savedInstanceState != null){
                email = savedInstanceState.getString(Const.FEEDBACK_EMAIL, null);
            }
        }

        CheckBox includeEmail = findViewById(R.id.feedback_include_email);
        if(email != null){
            includeEmail.setText(getResources().getString(R.string.feedback_include_email_tum_id, email));
            includeEmail.setChecked(true);
        } else {
            includeEmail.setChecked(false);
        }
        includeEmail.setOnClickListener(view -> {
            Checkable box = (Checkable) view;
            if(box.isChecked()){
                if("".equals(lrzId)){
                    EditText emailView = findViewById(R.id.feedback_custom_email);
                    emailView.setVisibility(View.VISIBLE);
                    emailView.setText(email);
                }
            } else {
                findViewById(R.id.feedback_custom_email).setVisibility(View.GONE);
            }
        });

        if(savedInstanceState != null){
            includeLocationBox.setChecked(savedInstanceState.getBoolean(Const.FEEDBACK_INCL_LOCATION));
            spinner.getOnItemSelectedListener().onItemSelected(null, null, savedInstanceState.getInt(Const.FEEDBACK_TOPIC), 0);
            picPaths = savedInstanceState.getStringArrayList(Const.FEEDBACK_PIC_PATHS);
        } else {
            picPaths = new ArrayList<>();
            includeLocationBox.setChecked(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                includeLocationBox.callOnClick();
            } else {
                location = new LocationManager(this).getLastLocation();
                if(location == null){
                    includeLocationBox.setChecked(false);
                }
            }
        }

        RecyclerView pictureList = findViewById(R.id.feedback_image_list);
        pictureList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
        thumbAdapter = new FeedbackThumbAdapter(picPaths);
        pictureList.setAdapter(thumbAdapter);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        for(String path: picPaths){
            new File(path).delete();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    location = new LocationManager(this).getLastLocation();
                    ((Checkable)findViewById(R.id.feedback_include_location)).setChecked(true);
                } else {
                    ((Checkable)findViewById(R.id.feedback_include_location)).setChecked(false);
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
        }
    }

    public void sendFeedback(View view){
        // TODO check format of user-provided email and warn the user if it's not correct

        // TODO send the feedback data to the server

    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(Const.FEEDBACK_TOPIC, feedbackTopic);
        outState.putString(Const.FEEDBACK_MESSAGE, ((TextView) findViewById(R.id.feedback_message)).getText().toString());
        outState.putStringArrayList(Const.FEEDBACK_PIC_PATHS, picPaths);
        outState.putBoolean(Const.FEEDBACK_INCL_EMAIL, ((Checkable) findViewById(R.id.feedback_include_email)).isChecked());
        outState.putBoolean(Const.FEEDBACK_INCL_LOCATION, ((Checkable) findViewById(R.id.feedback_include_location)).isChecked());
        outState.putString(Const.FEEDBACK_EMAIL, ((TextView) findViewById(R.id.feedback_custom_email)).getText().toString());
    }

    public void addPicture(View view){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.feedback_add_picture);
        dialog.setIcon(R.drawable.ic_add_photo_colored);
        String[] options = {getString(R.string.feedback_take_picture), getString(R.string.gallery)};
        dialog.setItems(options, (dialogInterface, i) -> {
            if(i == 0){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
                    if( permissionCheck == PackageManager.PERMISSION_DENIED){
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
                        return;
                    }
                }
                startTakingPicture();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                    if( permissionCheck == PackageManager.PERMISSION_DENIED){
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_FILES);
                        return;
                    }
                }
                openGallery();
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Utils.log("onActivityResult called");
        if(resultCode == RESULT_OK){
            if (requestCode == REQUEST_TAKE_PHOTO) {
                picPaths.add(mCurrentPhotoPath);
                thumbAdapter.notifyDataSetChanged();
            } else if(requestCode == REQUEST_GALLERY) {
                File destination = null;

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
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
        Utils.log("file created: " + mCurrentPhotoPath);
        return image;
    }

    private void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_GALLERY);
    }
}
