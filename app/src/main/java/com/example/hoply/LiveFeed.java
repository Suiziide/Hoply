package com.example.hoply;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hoply.db.HoplyLocation;
import com.example.hoply.db.HoplyPost;
import com.example.hoply.viewmodel.LivefeedViewmodel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Objects;

public class LiveFeed extends AppCompatActivity {
    private LivefeedViewmodel viewModel;
    private PostAdapter adapter;
    private RecyclerView recyclerView;
    private static int length = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_live_feed);
        findViewById(R.id.livefeed_content).setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });

        recyclerView = findViewById(R.id.recycler_view);

        FloatingActionButton createPostButton = findViewById(R.id.floatingActionButton);
        createPostButton.setOnClickListener(view ->
                /*              startActivityForResult(new Intent(LiveFeed.this, CreatePostPage.class), ADD_NOTE_REQUEST) */
                activityResultLaunch.launch(new Intent(LiveFeed.this, CreatePostPage.class))
        );

        adapter = new PostAdapter(this.getApplication());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(LivefeedViewmodel.class);

        viewModel.getPostList().observe(LiveFeed.this, postList -> adapter.addItems(postList));
    }

    ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    int postId;
                    List<HoplyPost> hoplyPostList= viewModel.getPostList().getValue();
                    if (hoplyPostList == null || hoplyPostList.size() == 0)
                        postId = 1;
                    else
                        postId = viewModel.getLatestID() + 1;
                    assert result.getData() != null;
                    HoplyPost post = new HoplyPost(postId, LoginPage.currentUser.getUserId(), result.getData().getStringExtra("CONTENT"));
                    double latitude = result.getData().getDoubleExtra("LATITUDE", 200.0);
                    double longitude = result.getData().getDoubleExtra("LONGITUDE", 200.0);
                    if (viewModel.insertLocalPost(post, latitude, longitude)) {
                        if (latitude >= -180.0 && longitude <= 180.0 && longitude >= -180.0 && latitude <= 180.0)
                            viewModel.insertLocation(new HoplyLocation(latitude, longitude, postId));
                        Toast.makeText(this, "Post saved!", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(this, "Illegal character combination of ('\\, \"')!", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, "Post not saved!", Toast.LENGTH_SHORT).show();
            });

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