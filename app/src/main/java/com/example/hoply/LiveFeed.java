package com.example.hoply;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class LiveFeed extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_live_feed);
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







}