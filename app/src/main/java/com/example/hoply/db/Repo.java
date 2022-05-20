package com.example.hoply.db;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;

public class Repo {
    private final HoplyDao dao;
    private LiveData<List<HoplyPost>> allPosts;
    private LiveData<List<HoplyComment>> allComments;

    public Repo(Application application) {
        HoplyDatabase db = HoplyDatabase.getDatabase(application);
        dao = db.hoplyDao();
        allPosts = dao.getAllPosts();
        allComments = dao.getAllComments();
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

    public void insertReaction(HoplyReaction reaction) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> {
            dao.insertReaction(reaction);
        });

    }

    public void insertLocation(HoplyLocation location) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertLocation(location));
    }

    public HoplyLocation returnLocationFromId(Integer postId) {
    public void insertComment(HoplyComment comment){
        HoplyDatabase.databaseWriteExecutor.execute(() -> {
            dao.insertComment(comment);
        });
    }

    public HoplyLocation returnLocationFromId(Integer postId){
        ExecutorCompletionService<HoplyLocation> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.returnLocationFromId(postId));
        try {
            return completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }

    public HoplyUser returnUserFromId(String userId) {
        ExecutorCompletionService<HoplyUser> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.returnUserFromId(userId));
        try {
            return completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }

    public LiveData<List<HoplyComment>> getAllComments(){
        return allComments;
    }

    public LiveData<List<HoplyPost>> getAllPosts() {
        return allPosts;
    }

    public Integer returnReactionsFromTypeAndID(Integer postid, Integer reactionType) {
        ExecutorCompletionService<Integer> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.returnReactionsFromTypeAndID(postid, reactionType));
        try {
            return completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }

    public Integer returnUserReactionToPost(String userId, Integer postId) {
        ExecutorCompletionService<Integer> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.returnUserReactionToPost(userId, postId));
        try {
            return completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }


    public Integer removeUserReactionFromPost(String userId, Integer postId) {
        ExecutorCompletionService<Integer> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.removeUserReactionFromPost(userId, postId));
        try {
            return completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }
}
