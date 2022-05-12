package com.example.hoply.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.hoply.db.HoplyPost;
import com.example.hoply.db.Repo;
import java.util.List;

public class LivefeedViewmodel extends AndroidViewModel {

    private final LiveData<List<HoplyPost>> postList;
    private final Repo repo;

    public LivefeedViewmodel(Application application){
        super(application);
        repo = new Repo(application);
        postList = repo.getAllPosts();
    }


    public LiveData<List<HoplyPost>> getPostList() {
        return postList;
    }
}
