package com.example.hoply.db;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
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

    public void insertLocalUser(HoplyUser user) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertUser(user));
        sendLocalDataToRemoteDB("https://caracal.imada.sdu.dk/app2022/users", convertUserToString(user));
    }

    public void insertRemoteUserToLocal(HoplyUser user) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertUser(user));
    }

    public void insertLocalPost(HoplyPost post) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertPost(post));
        sendLocalDataToRemoteDB("https://caracal.imada.sdu.dk/app2022/posts", convertPostToString(post));
    }

    public void insertRemotePostToLocal(HoplyPost post) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertPost(post));
    }















    public void insertLocalReaction(HoplyReaction reaction) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertReaction(reaction));
        sendLocalDataToRemoteDB("https://caracal.imada.sdu.dk/app2022/reactions", convertReactionToString(reaction));
    }

    public void insertRemoteReactionToLocal(HoplyReaction reaction) {
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













    public LiveData<List<HoplyPost>> getAllPosts() {
        getAllRemotePostsAndUsers();
        getAllRemoteReactions();
        return allPosts;
    }

    private void getAllRemoteReactions() {
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> {
            int inserts = createAndInsertRemoteReactions(getRemoteDataFrom("https://caracal.imada.sdu.dk/app2022/reactions"));
            return inserts > 0;
        });
        try {
            completionService.take().get(); // waits here until everything has been inserted into the local db
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int createAndInsertRemoteReactions(String[] responseBody) {
        String currentReaction = "";
        int inserts = 0;
        clearAllLocalReactions();
        while (inserts < responseBody.length) {
            currentReaction = responseBody[inserts];
            Log.d("reactionreactionreactionreaction", currentReaction);
            String userId = currentReaction.substring(currentReaction.indexOf("\"user_id\"") + 11,
                    currentReaction.indexOf("\"post_id\"") - 2);
            Integer postId = Integer.parseInt(currentReaction.substring(currentReaction.indexOf("\"post_id\"") + 10,
                    currentReaction.indexOf("\"type\"") - 1));
            int type = Integer.parseInt(currentReaction.substring(currentReaction.indexOf("\"type\"") + 7,
                    currentReaction.indexOf("\"stamp\"") - 1));
            long timeMillis = Timestamp.valueOf((currentReaction.substring(currentReaction.lastIndexOf("\"stamp\"") + 9,
                    currentReaction.length() - 7).replace("T", " "))).getTime();
            Log.d("reactionreactionreactionreaction", "" + userId + ", " +  postId + ", " + type + ", " + timeMillis);
            insertRemoteReactionToLocal(new HoplyReaction(userId, postId, type, timeMillis));
            inserts++;
        }
        return inserts;
    }

    public void clearAllLocalReactions() {
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> {
            dao.clearAllLocalReactions();
            return true;
        });
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

    // Other Auxiliary methods for syncing with the remote database

    private void getAllRemotePostsAndUsers() {
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(
                (() -> {
                    int localUserInserts = createAndInsertUser(getRemoteDataFrom("https://caracal.imada.sdu.dk/app2022/users"));
                    int localPostInserts = createAndInsertPosts(getRemoteDataFrom("https://caracal.imada.sdu.dk/app2022/posts"));
                    return localPostInserts > 0 && localUserInserts > 0;
                }));
        try {
            completionService.take().get(); // waits here until everything has been inserted into the local db
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int createAndInsertUser(String[] responseBody) {
        String currentUser = "";
        int inserts = 0;
        while (inserts < responseBody.length) {
            currentUser = responseBody[inserts];
            String userId = currentUser.substring(currentUser.indexOf("\"id\"") + 6,
                    currentUser.indexOf("\"name\"") - 2);
            String userName = currentUser.substring(currentUser.indexOf("\"name\"") + 8,
                    currentUser.indexOf("\"stamp\"") - 2);
            long timeMillis = Timestamp.valueOf((currentUser.substring(currentUser.lastIndexOf("\"stamp\"") + 9,
                    currentUser.length() - 7).replace("T", " "))).getTime();
            insertRemoteUserToLocal(new HoplyUser(userId, userName, timeMillis));
            inserts++;
        }
        return inserts;
    }

    // made it to a while loop, should maybe be made into a for loop again.
    private int createAndInsertPosts(String[] responseBody) {
        String currentPost;
        int inserts = responseBody.length-1;
        while (inserts >= 0) {
            currentPost = responseBody[inserts];
            Integer postId = Integer.parseInt(currentPost.substring(currentPost.indexOf("\"id\"") + 5,
                    currentPost.indexOf("\"user_id\"") - 1));
            String userId = currentPost.substring(currentPost.indexOf("\"user_id\"") + 11,
                    currentPost.indexOf("\"content\"") - 2);
            String content = currentPost.substring(currentPost.indexOf("\"content\"") + 11,
                    currentPost.indexOf("\"stamp\"") - 2);
            long timeMillis = Timestamp.valueOf((currentPost.substring(currentPost.lastIndexOf("\"stamp\"") + 9,
                    currentPost.length() - 7).replace("T", " "))).getTime();
            insertRemotePostToLocal(new HoplyPost(postId, userId, content, timeMillis));
            inserts--;
        }
        return inserts;
    }

    private String[] getRemoteDataFrom(String remoteDBURL) {
        String[] responseBody = new String[0];
        try {
            URL url = new URL(remoteDBURL);
            URLConnection con = url.openConnection();
            con.setRequestProperty("Authorization" , "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYXBwMjAyMiJ9.iEPYaqBPWoAxc7iyi507U3sexbkLHRKABQgYNDG4Awk");
            InputStream response = con.getInputStream();
            String res;
            try (Scanner scanner = new Scanner(response)) {
                res = (scanner.useDelimiter("\\A").next());
                res = res.substring(1,res.length()-1);
            }
            response.close();
            responseBody = extractData(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseBody;
    }

    private String[] extractData(String res) {
        List<Integer> dataStart = new ArrayList<>();
        List<Integer> dataEnd = new ArrayList<>();
        for (int i = 0; i < res.length(); i++)
            if (res.charAt(i) == '{')
                dataStart.add(i);
            else if (res.charAt(i) == '}')
                dataEnd.add(i);
        String[] dataList = new String[dataStart.size()];
        for (int i = 0; i < dataStart.size(); i++)
            dataList[i] = (res.substring(dataStart.get(i) + 1, dataEnd.get(i)));
        return dataList;
    }

    private void sendLocalDataToRemoteDB(String remoteDBURL, String data) {
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> sendLocalDataTo(remoteDBURL, data));
        try {
            completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean sendLocalDataTo(String remoteDBURL, String data) {
        try {
            URL url = new URL(remoteDBURL);
            URLConnection con = url.openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization" , "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYXBwMjAyMiJ9.iEPYaqBPWoAxc7iyi507U3sexbkLHRKABQgYNDG4Awk");

            try (OutputStream output = con.getOutputStream()) {
                output.write(data.getBytes());
            }

            HttpURLConnection connection = (HttpURLConnection) con;
            StringBuilder sb = new StringBuilder();
            if (connection.getResponseCode()/100 == 4 || connection.getResponseCode()/100 == 5) {
                Reader errorStream = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                for (int c;(c = errorStream.read()) >= 0;)
                    sb.append((char)c);
            }
            Log.d("postpostpostpost response", sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private String convertUserToString(HoplyUser user) {
        return "{\"id\":\"" + user.getUserId() +
                "\",\"name\":\"" + user.getUserName() +
                "\",\"stamp\":\"" + new Timestamp(user.getTimestamp()).toString().trim()
                .replace(" ", "T") + "+02:00\"}";
    }

    private String convertPostToString(HoplyPost post) {
        return "{\"id\":" + post.getPostId() +
                ",\"user_id\":\"" + post.getUserId() + "\""
                + ",\"content\":\"" + post.getContent() + "\""
                + ",\"stamp\":\"" + new Timestamp(post.getTimestamp()).toString().trim()
                .replace(" ", "T") + "+02:00\"}";
    }

    private String convertReactionToString(HoplyReaction reaction) {
        return "{\"user_id\":\"" + reaction.getUserId() + "\"" +
                ",\"post_id\":" + reaction.getPostId() +
                ",\"type\":" + reaction.getReactionType() +
                ",\"stamp\":\"" + new Timestamp(reaction.getTimestamp()).toString().trim()
                .replace(" ", "T") + "+02:00\"}";
    }
}
