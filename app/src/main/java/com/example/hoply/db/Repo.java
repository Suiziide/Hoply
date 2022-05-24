package com.example.hoply.db;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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

    public void insertPost(HoplyPost post) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertPost(post));

    }

    public void insertReaction(HoplyReaction reaction) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertReaction(reaction));
    }

    public void insertLocation(HoplyLocation location){
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertLocation(location));
    }

    public void insertComment (HoplyComment comment){
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertComment(comment));
    }

    public HoplyLocation returnLocationFromId (Integer postId){
        ExecutorCompletionService<HoplyLocation> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.returnLocationFromId(postId));
        try {
            return completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }

    public HoplyUser returnUserFromId (String userId){
        ExecutorCompletionService<HoplyUser> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.returnUserFromId(userId));
        try {
            return completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }

    public HoplyPost returnPostFromId (Integer postId){
        ExecutorCompletionService<HoplyPost> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.returnPostFromId(postId));
        try {
            return completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }

    public LiveData<List<HoplyComment>> getAllComments () {
        return allComments;
    }

    public LiveData<List<HoplyPost>> getAllPosts () {
        ExecutorService exec = Executors.newFixedThreadPool(4);

        exec.execute(() -> {
            URL url;
            URLConnection con;
            try {
                url = new URL("https://caracal.imada.sdu.dk/app2022/posts");
                con = url.openConnection();
                con.setRequestProperty("Authorization" , "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYXBwMjAyMiJ9.iEPYaqBPWoAxc7iyi507U3sexbkLHRKABQgYNDG4Awk");
                InputStream response = con.getInputStream();

                try (Scanner scanner = new Scanner(response)) {
                    String responseBody = scanner.useDelimiter("\\A").next();
                    Log.d("FLAHFH", responseBody);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return allPosts;
    }

    public Integer returnReactionsFromTypeAndID (Integer postid, Integer reactionType){
        ExecutorCompletionService<Integer> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.returnReactionsFromTypeAndID(postid, reactionType));
        try {
            return completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }

    public Integer returnUserReactionToPost (String userId, Integer postId){
        ExecutorCompletionService<Integer> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.returnUserReactionToPost(userId, postId));
        try {
            return completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }


    public Integer removeUserReactionFromPost (String userId, Integer postId){
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
