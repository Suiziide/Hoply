package com.example.hoply;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class CreatePostPage extends AppCompatActivity {

    private static final int REQUEST_CODE = 104284;
    private static File takenPicture = null;
    private static final String FILE_NAME = "photo.jpg";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_post_page);
        findViewById(R.id.create_post_content).setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });

        Button createPost = findViewById(R.id.create_post_button);
        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent(CreatePostPage.this, LiveFeed.class);
                EditText text = (EditText) findViewById(R.id.post_content);
                Log.d("DFDFDFDF - createpostpage", text.getText().toString());
                data.putExtra("CONTENT",text.getText().toString());
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });
    }

    public void goToLiveFeed(View v) {
        startActivity(new Intent(CreatePostPage.this, LiveFeed.class));
    }

    public void addLocation(View v) {
        Button b = (Button) v;
        if (b.getText().equals("LOCATION: OFF"))
            b.setText(R.string.locationOn);
        else
            b.setText(R.string.locationOff);
    }

    public void takePicture(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takenPicture = getPhotoFile(FILE_NAME);
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

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}