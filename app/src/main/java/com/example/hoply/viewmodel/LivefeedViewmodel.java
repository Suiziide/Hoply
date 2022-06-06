package com.example.hoply.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.hoply.db.HoplyComment;
import com.example.hoply.db.HoplyLocation;
import com.example.hoply.db.HoplyPost;
import com.example.hoply.db.Repo;

import java.util.List;

public class LivefeedViewmodel extends AndroidViewModel {

    private final LiveData<List<HoplyPost>> postList;
    private final LiveData<List<HoplyComment>> commentList;
    private final Repo repo;

    public LivefeedViewmodel(Application application){
        super(application);
        repo = new Repo(application);
        postList = repo.syncWithRemote();
        commentList = repo.getAllComments();
    }

    public boolean insertLocalPost(HoplyPost post, double latitude, double longitude){
        return repo.insertLocalPost(post, latitude, longitude);
    }

    public LiveData<List<HoplyPost>> getPostList() {
        return postList;
    }

    public boolean insertComment(HoplyComment comment){
        return repo.insertLocalComment(comment);
    }

    public LiveData<List<HoplyComment>> getCommentList() {
        return commentList;
    }

    public Integer getLatestID() {return repo.getLatestID();}
}