package com.example.hoply;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccountPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_account_page);
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
}