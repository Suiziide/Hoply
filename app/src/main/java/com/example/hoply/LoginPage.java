package com.example.hoply;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hoply.db.HoplyUser;
import com.example.hoply.db.Repo;

public class LoginPage extends AppCompatActivity {
    HoplyUser userToCheck;
    public static HoplyUser currentUser;
    private Repo myRepo;

    /**
     * Builds functionality, retrieves data, and sets values in accordance with the XML-layout for this page
     * @param savedInstanceState
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        // Sets layout for this page
        setContentView(R.layout.activity_login_page);
        // Hides keyboard when something else is touched
        findViewById(R.id.login_content).setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });
        myRepo = new Repo(this.getApplication());
        myRepo.clearAllData();
        myRepo.startTimer();
    }

    /**
     * Attempts to make a login with the input from the EditText-element on LoginPage
     * - Redirect to LiveFeed if successful
     * - Notifies user if unsuccessful
     * @param v
     */
    public void tryLogin(View v) {
        EditText userid = findViewById(R.id.loginPageName);
        userToCheck = myRepo.returnUserFromId(userid.getText().toString());

        if (userToCheck != null) {
            currentUser = userToCheck;
            goToHomePage(v);
        } else
            Toast.makeText(getApplication(),R.string.somethingWrong, Toast.LENGTH_LONG).show();
    }

    public void goToCreateAccount(View v) {
        startActivity(new Intent(LoginPage.this, CreateAccountPage.class));
    }

    private void goToHomePage(View v) {
        startActivity(new Intent(LoginPage.this, LiveFeed.class));
    }

    // Makes the keyboard go away
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}