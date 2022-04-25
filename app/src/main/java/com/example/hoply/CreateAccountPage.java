package com.example.hoply;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccountPage extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_account_page);
        findViewById(R.id.create_account_content).setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });
    }

    public void tryToCreateAccount(View v) {
        // if (username = ANY select username from 'table_name';)
        //    tell app user that username is already taken / exists
        // else {
        //    INSERT INTO 'table_name' VALUES(username, password, 'other_data....');
        //    tell app user that account was successfully created
        goToLoginPage(v);
        // }
    }

    public void goToLoginPage(View v) {
        startActivity(new Intent(CreateAccountPage.this, LoginPage.class));
    }
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}