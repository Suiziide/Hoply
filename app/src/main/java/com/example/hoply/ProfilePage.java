package com.example.hoply;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

public class ProfilePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_profile_page);
    }


    public void saveChangesToProfile(View v) {
        // if (inputted information is valid and doesn't already exist)
        //    UPDATE 'Table_name' where xxxxxx
        //    Tell user that changes was saved / updated
        // else
        //    Tell user something isn't correct and do nothing
    }

    public void goToLivePage(View v) {
        startActivity(new Intent(ProfilePage.this, LiveFeed.class));
    }




}