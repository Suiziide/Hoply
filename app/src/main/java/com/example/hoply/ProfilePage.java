package com.example.hoply;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

public class ProfilePage extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_profile_page);
        findViewById(R.id.profile_page_content).setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });
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

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }



}