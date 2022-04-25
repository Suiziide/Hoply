package com.example.hoply;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

public class CreatePostPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_post_page);
    }



    public void createPost(View v) {
        // if (all values match needed values for creating a post)
        //    INSERT INTO 'Table_name' VALUES ('VALUES FROM CREATED POST')
        goToLiveFeed(v);
        // else
        //    tell user that some required information is missing
    }

    public void goToLiveFeed(View v) {
        startActivity(new Intent(CreatePostPage.this, LiveFeed.class));
    }
}