package com.example.hoply;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hoply.db.HoplyComment;
import com.example.hoply.db.HoplyLocation;
import com.example.hoply.db.HoplyPost;
import com.example.hoply.db.HoplyUser;
import com.example.hoply.db.Repo;
import com.example.hoply.viewmodel.LivefeedViewmodel;

import java.util.stream.Collectors;

public class ViewPostPage extends AppCompatActivity {

    private int ADD_NOTE_REQUEST = 1;
    private LivefeedViewmodel viewModel;
    private CommentAdapter adapter;
    private RecyclerView recyclerView;
    private Integer postId;
    private Repo myRepo;

    /**
     *  onCreate method for ViewPostPage
     *  Builds functionality, retrieves data, and sets values in accordance with the XML-layout for this page
     * @param  savedInstanceState
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        // Sets layout for this page
        setContentView(R.layout.activity_view_post_page);
        // Hides keyboard when something else is touched
        findViewById(R.id.view_post_content).setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });
        // Redirects user to LiveFeed-page if the posts ID defaults to -200
        Intent intent = getIntent();
        postId = intent.getIntExtra("POSTID", -200);
        myRepo = new Repo(this.getApplication());
        if (postId == -200)
            startActivity(new Intent(this.getApplication(), LiveFeed.class));

        HoplyPost hoplyPost = myRepo.returnPostFromId(postId);
        HoplyUser hoplyUser = myRepo.returnUserFromId(hoplyPost.getUserId());
        // Instantiates design-elements from XML as variables
        TextView postUser = findViewById(R.id.post_user);
        TextView postContent = findViewById(R.id.post_content);
        EditText commentText = findViewById(R.id.comment_textfield);
        ImageView submitComment = findViewById(R.id.submit_comment);

        // Sets values of said variables to appropriate values
        postUser.setText(hoplyUser.getUserName());
        postContent.setText(hoplyPost.getContent());
        HoplyLocation location = myRepo.returnLocationFromId(postId);
        Fragment fragment;
        if (location != null) {
            FrameLayout frame = findViewById(R.id.frame_layout);
            frame.setVisibility(View.VISIBLE);
            fragment = new MapFragment(location.getLatitude(), location.getLongitude());
            getSupportFragmentManager()
                    .beginTransaction().replace(R.id.frame_layout,fragment)
                    .commit();
        }
        // Sets onClickListener on image and gives functionality for when clicked
        submitComment.setOnClickListener(view -> {
            String commentContent = commentText.getText().toString();
            if (commentContent.matches("\\s+") || commentContent.isEmpty())
                //lets user know that the comment is empty
                Toast.makeText(commentText.getContext(), "Textfield is empty", Toast.LENGTH_SHORT).show();
            else {
                if (!viewModel.insertComment(new HoplyComment(LoginPage.currentUser.getUserId(), postId, commentContent, System.currentTimeMillis())))
                    //lets user know that an error occurred when trying to make the comments
                    Toast.makeText(commentText.getContext(), "failed to create comments", Toast.LENGTH_SHORT).show();
                else
                    commentText.setText("");
                hideKeyboard(view);
            }
        });
        // Sets recyclerView to element in XML
        recyclerView = findViewById(R.id.recycler_view_comments);
        adapter = new CommentAdapter(this.getApplication());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Sets adapter for recyclerView
        recyclerView.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(LivefeedViewmodel.class);
        // Adds comment-items to adapter
        viewModel.getCommentList().observe(ViewPostPage.this, commentList -> adapter.addItems(commentList.stream()
                .filter(hoplyComment -> hoplyComment.getPostId().equals(postId))
                .collect(Collectors.toList())));
    }

    // Used by "BACK" buttons to return to LiveFeed
    public void goToLiveFeed(View v) {
        startActivity(new Intent(ViewPostPage.this, LiveFeed.class));
    }

    // Makes the keyboard go away
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}