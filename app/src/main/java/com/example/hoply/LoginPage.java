package com.example.hoply;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

public class LoginPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login_page);
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
}