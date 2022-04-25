package com.example.hoply;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hoply.db.HoplyUser;
import com.example.hoply.db.Repo;

public class LoginPage extends AppCompatActivity {
    public static HoplyUser currentUser;
    private Repo myRepo;

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
        myRepo = new Repo(this.getApplication());
    }


    public void tryLogin(View v) {
        EditText userid = findViewById(R.id.loginPageUsername);
        Log.d("userid", userid.getText().toString());
        /*
        if (myRepo.compareUser(userid.getText().toString())) {
            goToHomePage(v);
            // currentUser = database kalde, which returns user.
        }
        */
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