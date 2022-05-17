package com.example.hoply;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.hoply.db.HoplyLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.IOException;

public class CreatePostPage extends AppCompatActivity {

    private static final int REQUEST_CODE = 104284;
    private static File takenPicture = null;
    private static final String FILE_NAME = "photo.jpg";

    FusedLocationProviderClient flc;
    private Location lastKnownLocation;
    int PERMISSION_ID = 22;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        flc = LocationServices.getFusedLocationProviderClient(this);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_post_page);
        findViewById(R.id.create_post_content).setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });

        Button createPost = findViewById(R.id.create_post_button);
        createPost.setOnClickListener(view -> {
            Intent data = new Intent(CreatePostPage.this, LiveFeed.class);
            EditText text = findViewById(R.id.post_content);
            data.putExtra("CONTENT",text.getText().toString());
            if (takenPicture != null)
                data.putExtra("IMAGEPATH", takenPicture.getAbsolutePath());
            if (lastKnownLocation != null) {
                data.putExtra("LATITUDE", lastKnownLocation.getLatitude());
                data.putExtra("LONGITUDE", lastKnownLocation.getLongitude());
            }
            setResult(Activity.RESULT_OK, data);
            finish();
        });
    }

    public void goToLiveFeed(View v) {
        startActivity(new Intent(CreatePostPage.this, LiveFeed.class));
    }

    public void addLocation(View v) {
        Button b = (Button) v;
        if (b.getText().equals("LOCATION: OFF")) {
            b.setText(R.string.locationOn);
            getLastLocation();
        }
        else {
            b.setText(R.string.locationOff);
            lastKnownLocation = null;
        }
    }

    public void takePicture(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takenPicture = getPhotoFile(FILE_NAME);
        if (takenPicture != null)
            takenPicture.deleteOnExit();
        Uri fileProvider = FileProvider.getUriForFile(this, "com.example.hoply.fileprovider", takenPicture);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        try {
            startActivityForResult(takePictureIntent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this,"No camera detected", Toast.LENGTH_SHORT).show();
        }
    }

    private File getPhotoFile(String fileName) {
        try {
            return File.createTempFile(fileName, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap imageBitmap = BitmapFactory.decodeFile(takenPicture.getAbsolutePath());
            ImageView picture = findViewById(R.id.TakenPictureView);
            picture.setImageBitmap(imageBitmap);

        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check for permissions
        if(checkPermissions()) {

            // check if location is enabled
            if(isLocationEnabled()){
                // getting last location from flc
                flc.getLastLocation().addOnCompleteListener(task -> {
                    Location location = task.getResult();
                    if (location == null){
                        requestNewLocationData();
                    }else{
                        lastKnownLocation = location;
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else{
            // if permissions aren't available, request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        // Initializing LocationRequest object
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5);
        locationRequest.setFastestInterval(0);
        locationRequest.setNumUpdates(1);

        // setting LocationRequest on flc
        flc = LocationServices.getFusedLocationProviderClient(this);
        flc.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            lastKnownLocation = locationResult.getLastLocation();
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    // method to request permissions
    private void requestPermissions(){
        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_ID) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(checkPermissions()){
            getLastLocation();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}