package com.example.hoply;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
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
        myRepo = new Repo(this.getApplication());
    }

    public void tryToCreateAccount(View v) {
        boolean succeeded = tryCreateUser();
        if (succeeded){
                Toast.makeText(getApplication(),R.string.userCreated, Toast.LENGTH_LONG).show();
            goToLoginPage(v);
        } else {
                Toast.makeText(getApplication(),
                        R.string.userAlreadyExists,
                        Toast.LENGTH_LONG).show();
        }
    }

    private boolean tryCreateUser() {
        boolean succeeded = false;
        EditText name = findViewById(R.id.loginPageName);
        EditText username = findViewById(R.id.loginPageUsername);
        HoplyUser user = new HoplyUser(username.getText().toString(), name.getText().toString());
        if (!name.getText().toString().matches("") || !username.getText().toString().matches("")) {
            succeeded = true;
            try {
                myRepo.insertUser(user);
            } catch (SQLiteConstraintException e) {
                succeeded = false;
            }
        }
        return succeeded;
    }

    private void goToLoginPage(View v) {
        startActivity(new Intent(CreateAccountPage.this, LoginPage.class));
    }
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}