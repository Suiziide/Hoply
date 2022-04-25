package com.example.hoply;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

public class LiveFeed extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_live_feed);
        findViewById(R.id.livefeed_content).setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });

    }


    public void signOut(View v) {
        startActivity(new Intent(LiveFeed.this, LoginPage.class));
    }

    public void goToProfilePage(View v) {
        startActivity(new Intent(LiveFeed.this, ProfilePage.class));
    }

    public void createNewPost(View v) {
        startActivity(new Intent(LiveFeed.this, CreatePostPage.class));
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }




}