package com.example.hoply;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

public class LoginPage extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login_page);
        findViewById(R.id.login_content).setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });
    }


    public void tryLogin(View v) {
        // if (username && and password match recorded user && password)
        //  clear username and password data;
        goToHomePage(v);
        // else
        // inform user that either password or username wasn't correct
    }

    public void goToCreateAccount(View v) {
        startActivity(new Intent(LoginPage.this, CreateAccountPage.class));
    }

    private void goToHomePage(View v) {
        startActivity(new Intent(LoginPage.this, LiveFeed.class));
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}