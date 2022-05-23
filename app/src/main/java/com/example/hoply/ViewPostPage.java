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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hoply.db.HoplyComment;
import com.example.hoply.db.HoplyLocation;
import com.example.hoply.db.HoplyPost;
import com.example.hoply.db.HoplyUser;
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
        myRepo = new Repo(this.getApplication());
        Log.d("TESTes", postId + "");
        if(postId == -200){
            startActivity(new Intent(this.getApplication(), LiveFeed.class));
        }
        HoplyPost hoplyPost = myRepo.returnPostFromId(postId);
        HoplyUser hoplyUser = myRepo.returnUserFromId(hoplyPost.getUserId());
        TextView postUser = findViewById(R.id.post_user);
        TextView postContent = findViewById(R.id.post_content);
        EditText commentText = findViewById(R.id.comment_textfield);
        ImageView submitComment = findViewById(R.id.submit_comment);

        postUser.setText(hoplyUser.getUserName());
        postContent.setText(hoplyPost.getContent());
        HoplyLocation location = myRepo.returnLocationFromId(postId);
        Fragment fragment;
        if(location != null) {
            FrameLayout frame = findViewById(R.id.frame_layout);
            frame.setVisibility(View.VISIBLE);
            fragment = new MapFragment(location.getLatitude(), location.getLongitude());
            getSupportFragmentManager()
                    .beginTransaction().replace(R.id.frame_layout,fragment)
                    .commit();
        }
        submitComment.setOnClickListener(view -> {
            if (commentText.getText().toString().matches("\\s+") ||
                    commentText.getText().toString().isEmpty())
                Toast.makeText(commentText.getContext(), "Post is empty", Toast.LENGTH_SHORT).show();
            else {
                viewModel.insertComment(new HoplyComment(LoginPage.currentUser.getUserId(), postId, commentText.getText().toString()));
                commentText.setText("");
                hideKeyboard(view);
            }
        });
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

    public void goToLiveFeed(View v) {
        startActivity(new Intent(ViewPostPage.this, LiveFeed.class));
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}