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
        myRepo.getAllPosts();
    }

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

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}