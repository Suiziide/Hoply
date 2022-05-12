package com.example.hoply;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.example.hoply.db.HoplyPost;
import com.example.hoply.viewmodel.LivefeedViewmodel;

import java.util.ArrayList;
import java.util.List;

public class LiveFeed extends AppCompatActivity {

    private LivefeedViewmodel viewModel;
    private PostAdapter adapter;
    private RecyclerView recyclerView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_live_feed);
        findViewById(R.id.livefeed_content).setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new PostAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(LivefeedViewmodel.class);

        viewModel.getPostList().observe(LiveFeed.this, new Observer<List<HoplyPost>>() {
            @Override
            public void onChanged(List<HoplyPost> postList) {
                adapter.addItems(postList);
            }
        });
    }


    public void signOut(View v) {
        startActivity(new Intent(LiveFeed.this, LoginPage.class));
    }

    public void goToProfilePage(View v) {
        startActivity(new Intent(LiveFeed.this, ProfilePage.class));
    }

    public void createNewPost(View v) {
        startActivity(new Intent(LiveFeed.this, CreatePostPage.class));
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }




}