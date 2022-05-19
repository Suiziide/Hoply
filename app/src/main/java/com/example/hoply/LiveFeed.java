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
import android.widget.Button;
import android.widget.Toast;

import com.example.hoply.db.HoplyLocation;
import com.example.hoply.db.HoplyPost;
import com.example.hoply.viewmodel.LivefeedViewmodel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LiveFeed extends AppCompatActivity {

    private int ADD_NOTE_REQUEST = 1;
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

        recyclerView = findViewById(R.id.recycler_view);

        FloatingActionButton createPostButton = findViewById(R.id.floatingActionButton);
        createPostButton.setOnClickListener(view -> startActivityForResult(new Intent(LiveFeed.this, CreatePostPage.class), ADD_NOTE_REQUEST));

        adapter = new PostAdapter(this.getApplication());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_NOTE_REQUEST && resultCode == Activity.RESULT_OK){
            HoplyPost post = new HoplyPost(LoginPage.currentUser.getUserId(), data.getStringExtra("CONTENT"));
            String imagePath = data.getStringExtra("IMAGEPATH");
            viewModel.insertPost(post);

            double latitude = data.getDoubleExtra("LATITUDE", 0.0);
            double longitude = data.getDoubleExtra("LONGITUDE", 0.0);
            if (latitude != 0.0 && longitude != 0.0){
                viewModel.insertLocation(new HoplyLocation(latitude, longitude, post.getPostId()));
            }

            Toast.makeText(this, "Post saved!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Post not saved!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(LoginPage.currentUser == null)
            startActivity(new Intent(LiveFeed.this, LoginPage.class));
    }

    public void signOut(View v) {
        LoginPage.currentUser = null;
        startActivity(new Intent(LiveFeed.this, LoginPage.class));
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }




}