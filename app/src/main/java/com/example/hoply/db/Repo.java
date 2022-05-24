package com.example.hoply.db;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(
                (() -> {
                    String[] responseBody = new String[0];
                    try {
                        URL url = new URL("https://caracal.imada.sdu.dk/app2022/users");
                        URLConnection con = url.openConnection();
                        con.setRequestProperty("Authorization" , "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYXBwMjAyMiJ9.iEPYaqBPWoAxc7iyi507U3sexbkLHRKABQgYNDG4Awk");
                        InputStream response = con.getInputStream();
                        String res = "";
                        try (Scanner scanner = new Scanner(response)) {
                            res = (scanner.useDelimiter("\\A").next());
                            res = res.substring(1,res.length()-1);
                        }
                        response.close();
                        responseBody = extractData(res);
                        createAndInsertUser(responseBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        URL url = new URL("https://caracal.imada.sdu.dk/app2022/posts");
                        URLConnection con = url.openConnection();
                        con.setRequestProperty("Authorization" , "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYXBwMjAyMiJ9.iEPYaqBPWoAxc7iyi507U3sexbkLHRKABQgYNDG4Awk");
                        InputStream response = con.getInputStream();
                        String res = "";
                        try (Scanner scanner = new Scanner(response)) {
                            res = (scanner.useDelimiter("\\A").next());
                            res = res.substring(1,res.length()-1);
                        }
                        responseBody = extractData(res);
                        createAndInsertPosts(responseBody);
                        response.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return responseBody.length > 0;
                }));
        return allPosts;
    }

    private void createAndInsertUser(String[] responseBody) {
        String currentUser = "";
        for (String s : responseBody) {
            currentUser = s;
            String userId = currentUser.substring(currentUser.indexOf("\"id\"") + 6,
                    currentUser.indexOf("\"name\"") - 2);
            String userName = currentUser.substring(currentUser.indexOf("\"name\"") + 8,
                    currentUser.indexOf("\"stamp\"") - 2);
            long timeMillis = Timestamp.valueOf((currentUser.substring(currentUser.lastIndexOf("\"stamp\"") + 9,
                    currentUser.length() - 7).replace("T", " "))).getTime();
            Log.d("user", userId + ", " + userName + ", " + timeMillis);
            insertUser(new HoplyUser(userId, userName, timeMillis));
        }
    }

    private void createAndInsertPosts(String[] responseBody) {
        String currentPost;
        int lastId;
        if (allPosts.getValue() != null) {
            lastId = allPosts.getValue().get(0).postId;
            Log.d("postpost", lastId + "lastId");
        } else
            lastId = 1;
        for (int i = 0; i < responseBody.length; i++) {
            currentPost = responseBody[i];
            Integer postId = lastId + i;
            String userId = currentPost.substring(currentPost.indexOf("\"user_id\"") + 11,
                    currentPost.indexOf("\"content\"") - 2);
            String content = currentPost.substring(currentPost.indexOf("\"content\"") + 11,
                    currentPost.indexOf("\"stamp\"") - 2);
            long timeMillis = Timestamp.valueOf((currentPost.substring(currentPost.lastIndexOf("\"stamp\"") + 9,
                    currentPost.length() - 7).replace("T", " "))).getTime();

            Log.d("postpost", postId + ", " + userId + ", " + content + ", " + timeMillis);
            insertPost(new HoplyPost(postId, userId, content, timeMillis));
        }
    }

    private String[] extractData(String res) {
        List<Integer> postStart = new ArrayList<>();
        List<Integer> postEnd = new ArrayList<>();
        for (int i = 0; i < res.length(); i++)
            if (res.charAt(i) == '{')
                postStart.add(i);
            else if (res.charAt(i) == '}')
                postEnd.add(i);
        String[] postList = new String[postStart.size()];
        for (int i = 0; i < postStart.size(); i++)
            postList[i] = (res.substring(postStart.get(i)+1,postEnd.get(i)));
        return postList;
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
