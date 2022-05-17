package com.example.hoply.db;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;

import androidx.lifecycle.LiveData;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.hoply.db.HoplyDao;
import com.example.hoply.db.HoplyDatabase;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

public class Repo {
    private final HoplyDao dao;
    private HoplyUser returnUser;
    private LiveData<List<HoplyPost>> allPosts;

    public Repo(Application application) {
        HoplyDatabase db = HoplyDatabase.getDatabase(application);
        dao = db.hoplyDao();
        allPosts = dao.getAllPosts();
    }

    public void insertUser(HoplyUser user) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> {
            try {
                dao.insertUser(user);
            } catch (SQLiteConstraintException e) {
                throw e;
            }
        });
    }

    public void insertPost(HoplyPost post){
        HoplyDatabase.databaseWriteExecutor.execute(() -> {
            dao.insertPost(post);
        });

    }

    public void insertReaction(HoplyReaction reaction){
        HoplyDatabase.databaseWriteExecutor.execute(() -> {
            dao.insertReaction(reaction);
        });

    }

    public void insertLocation(HoplyLocation location){
        HoplyDatabase.databaseWriteExecutor.execute(() -> {
            dao.insertLocation(location);
        });
    }

    public HoplyLocation returnLocationFromId(Integer postId){
        HoplyDatabase.databaseWriteExecutor.execute(() -> {
            dao.returnLocationFromId(postId);
        });
    }

    public HoplyUser returnUserFromId(String userId) {
        ExecutorCompletionService<HoplyUser> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.returnUserFromId(userId));
        try {
            return (HoplyUser) completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }

    public LiveData<List<HoplyPost>> getAllPosts() {
        return allPosts;
    }
}
