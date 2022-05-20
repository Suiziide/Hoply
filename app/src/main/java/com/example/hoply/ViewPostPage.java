package com.example.hoply;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.example.hoply.db.HoplyComment;
import com.example.hoply.db.HoplyLocation;
import com.example.hoply.db.HoplyPost;
import com.example.hoply.db.Repo;
import com.example.hoply.viewmodel.LivefeedViewmodel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.stream.Collectors;

public class ViewPostPage extends AppCompatActivity {

    private int ADD_NOTE_REQUEST = 1;
    private LivefeedViewmodel viewModel;
    private CommentAdapter adapter;
    private RecyclerView recyclerView;
    private Integer postId;
    private Repo myRepo;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_view_post_page);
        findViewById(R.id.view_post_content).setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });
        Intent intent = getIntent();
        postId = intent.getIntExtra("POSTID", -200);
        if(postId == -200){
            startActivity(new Intent(this.getApplication(), LiveFeed.class));
        }
        myRepo = new Repo(this.getApplication());
        HoplyLocation location = myRepo.returnLocationFromId(postId);
            Log.d("ETTESTET", postId + ", " + location.getPostid() + ", " + location.getLatitude());
        Fragment fragment;
        if(location != null) {
            FrameLayout frame = findViewById(R.id.frame_layout);
            frame.setVisibility(View.VISIBLE);
            fragment = new MapFragment(location.getLatitude(), location.getLongitude());
            getSupportFragmentManager()
                    .beginTransaction().replace(R.id.frame_layout,fragment)
                    .commit();
        }

        recyclerView = findViewById(R.id.recycler_view_comments);

        adapter = new CommentAdapter(this.getApplication());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(LivefeedViewmodel.class);

        viewModel.getCommentList().observe(ViewPostPage.this, new Observer<List<HoplyComment>>() {
            @Override
            public void onChanged(List<HoplyComment> commentList) {
                adapter.addItems(commentList.stream()
                        .filter(hoplyComment -> hoplyComment.getPostId().equals(postId))
                        .collect(Collectors.toList()));
            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}