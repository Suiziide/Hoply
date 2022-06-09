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

public class CreateAccountPage extends AppCompatActivity {
    private Repo myRepo;

    /**
     *
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
        setContentView(R.layout.activity_create_account_page);
        // Hides keyboard when something else is touched
        findViewById(R.id.create_account_content).setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });
        myRepo = new Repo(this.getApplication());
    }

    // Attempts to create a new account based on input in the two EditText-elements
    public void tryToCreateAccount(View v) {
        EditText id = findViewById(R.id.loginPageName);
        EditText username = findViewById(R.id.loginPageUsername);
        String USERNAME_PATTERN = "^[a-zA-Z 0-9]{2,25}$"; // Regex for legal name-input
        String USERID_PATTERN = "^[a-zA-Z0-9]{2,50}$"; // Regex for legal ID-input
        if (id.getText().toString().trim().matches(USERNAME_PATTERN) &&
                username.getText().toString().trim().matches(USERID_PATTERN)) {
            // Insert new user if one with identical ID does not already exist
            if (myRepo.returnUserFromId(id.getText().toString().trim()) == null) {
                HoplyUser user = new HoplyUser(username.getText().toString(), id.getText().toString());
                goToLoginPage(v);
                // Notifies the user of the result of the attempt at making a new post
                if(!myRepo.insertLocalUser(user))
                    Toast.makeText(getApplication(), R.string.FailedToCreateUser, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplication(), R.string.userCreated, Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(getApplication(), R.string.userAlreadyExists, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplication(), "Illegal character use", Toast.LENGTH_LONG).show();
        }
    }

    public void goToLoginPage(View v) { // must not be private
        startActivity(new Intent(CreateAccountPage.this, LoginPage.class));
    }

    // Makes the keyboard go away
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}