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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

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

    public void startTimer() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::getAllPosts, 0, 1, TimeUnit.SECONDS);
    }

    public void insertLocalUser(HoplyUser user) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertUser(user));
        sendLocalDataToRemoteDB("https://caracal.imada.sdu.dk/app2022/users", convertUserToString(user));
    }

    public void insertRemoteUserToLocal(HoplyUser user) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertUser(user));
    }

    public void insertLocalPost(HoplyPost post, double latitude, double longitude) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertPost(post));
        sendLocalDataToRemoteDB("https://caracal.imada.sdu.dk/app2022/posts", convertPostToString(post, latitude, longitude));
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

    public void insertLocalComment(HoplyComment comment){
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertComment(comment));
        updateRemoteComments(comment);
    }

    public void insertRemoteCommentToLocal(HoplyComment comment) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> dao.insertComment(comment));
    }

    private void updateRemoteComments(HoplyComment newComment) {
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> {
            String oldContent = getRemoteDataFrom(
                    "https://caracal.imada.sdu.dk/app2022/posts?id=eq."
                            + newComment.getPostId())[0];
            oldContent = oldContent.substring(oldContent.indexOf("\"content\"") + 11, oldContent.lastIndexOf("\"stamp\"") - 2);
            Log.d("maybemaybe oldcontent", oldContent);

            String newContent = oldContent +
                    " ¤$con§:" + newComment.getContent() + "$pid§:" + newComment.getPostId()
                    + "$uid§:" + newComment.getUserId() + "$tim§:" + newComment.getTimestamp() + "¤";
            Log.d("maybemaybe newContent", newContent);
            patchDataToRemoteDB("https://caracal.imada.sdu.dk/app2022/posts?id=eq."
                    + newComment.getPostId(),"{\"content\":\"" + newContent + "\"}");
            return true;
        });
        try {
            completionService.take().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
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

    public Integer getLatestID() {
        ExecutorCompletionService<Integer> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(dao::getLatestID);
        try {
            return completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            return 0;
        }
    }

    public LiveData<List<HoplyComment>> getAllComments () {
        return allComments;
    }

    public LiveData<List<HoplyPost>> getAllPosts() {
        //deleteDataFromRemoteDB("https://caracal.imada.sdu.dk/app2022/posts?user_id=eq.faxekondiaddict");
        clearAllLocalReactions();
        Log.d("markermarkermarkermarkermarker", "1");
        getAllRemotePostsAndUsers();
        Log.d("markermarkermarkermarkermarker", "2");
        getAllRemoteReactions();
        Log.d("markermarkermarkermarkermarker", "3");
        return allPosts;
    }

    private void getAllRemoteReactions() {
        Log.d("markermarkermarkermarkermarker", "4");
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> {
            Log.d("markermarkermarkermarkermarker", "6");
            int inserts = createAndInsertRemoteReactions(getRemoteDataFrom("https://caracal.imada.sdu.dk/app2022/reactions"));
            Log.d("markermarkermarkermarkermarker", "7");
            return inserts >= 0;
        });
        try {
            Log.d("markermarkermarkermarkermarker", "5");
            completionService.take().get();
            Log.d("markermarkermarkermarkermarker", "8");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
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
            completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int createAndInsertUser(String[] responseBody) {
        String currentUser;
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

    private int createAndInsertPosts(String[] responseBody) {

        double latitude = 200;
        double longitude = 200;
        int inserts = responseBody.length-1;
        String[] comments = null;
        while (inserts >= 0) {
            String currentPost = responseBody[inserts];
            Integer postId = Integer.parseInt(currentPost.substring(currentPost.indexOf("\"id\"") + 5,
                    currentPost.indexOf("\"user_id\"") - 1));
            String userId = currentPost.substring(currentPost.indexOf("\"user_id\"") + 11,
                    currentPost.indexOf("\"content\"") - 2);
            String content = currentPost.substring(currentPost.indexOf("\"content\"") + 11,
                    currentPost.indexOf("\"stamp\"") - 2);
            if (content.contains("$LA§:") && content.contains("$LO§:")) {
                latitude = Double.parseDouble(content.substring(content.indexOf("$LA§:") + 5, content.indexOf("$LO§:")));
                longitude = Double.parseDouble(content.substring(content.indexOf("$LO§:") + 5));
                content = content.substring(0, content.indexOf("$LA§:"));
            }
            if (content.contains("$con§") && content.contains("$pid§") && content.contains("$uid§")
                    && content.contains("$tim§")) {
                comments = extractComments(content);
                content = content.substring(0, content.indexOf("$con§:"));
            }
            long timeMillis = Timestamp.valueOf((currentPost.substring(currentPost.lastIndexOf("\"stamp\"") + 9,
                    currentPost.length() - 7).replace("T", " "))).getTime();
            insertRemotePostToLocal(new HoplyPost(postId, userId, content, timeMillis));
            if (latitude != 200 && longitude != 200)
                insertLocation(new HoplyLocation(latitude ,longitude, postId));
            if (comments != null && comments.length > 0)
                createAndInsertComments(comments);
            inserts--;
        }
        return inserts;
    }

    private void createAndInsertComments(String[] comments) {
        int inserts = comments.length-1;
        while (inserts >= 0) {
            String currentComment = comments[inserts];
            String commentContent = currentComment.substring(currentComment.indexOf("$con§:") + 6, currentComment.indexOf("$pid§:"));
            Integer commentId = Integer.parseInt(currentComment.substring(currentComment.indexOf("$pid§:") + 6, currentComment.indexOf("$uid§:")));
            String commentUserId = currentComment.substring(currentComment.indexOf("$uid§:") + 6, currentComment.indexOf("$tim§"));
            Long timestamp = Long.parseLong(currentComment.substring(currentComment.indexOf("$tim") + 6));
            insertRemoteCommentToLocal(new HoplyComment(commentUserId, commentId, commentContent, timestamp));
            inserts--;
        }
    }

    private String[] extractComments(String content) {
        List<Integer> dataPoints = new ArrayList<>();
        for (int i = 0; i < content.length(); i++)
            if (content.charAt(i) == '¤')
                dataPoints.add(i);
        String[] dataList = new String[dataPoints.size()/2];
        for (int i = 1; i < dataPoints.size(); i+=2)
            dataList[(i-1)/2] = (content.substring(dataPoints.get(i - 1) + 1, dataPoints.get(i)));
        return dataList;
    }

    private int createAndInsertRemoteReactions(String[] responseBody) {
        if (responseBody.length <= 0)
            return 0;
        String currentReaction;
        int inserts = 0;
        Log.d("markermarkermarkermarkermarker", "12");
        clearAllLocalReactions();
        Log.d("markermarkermarkermarkermarker", "13");
        while (inserts < responseBody.length) {
            currentReaction = responseBody[inserts];
            String userId = currentReaction.substring(currentReaction.indexOf("\"user_id\"") + 11,
                    currentReaction.indexOf("\"post_id\"") - 2);
            Integer postId = Integer.parseInt(currentReaction.substring(currentReaction.indexOf("\"post_id\"") + 10,
                    currentReaction.indexOf("\"type\"") - 1));
            int type = Integer.parseInt(currentReaction.substring(currentReaction.indexOf("\"type\"") + 7,
                    currentReaction.indexOf("\"stamp\"") - 1));
            long timeMillis = Timestamp.valueOf((currentReaction.substring(currentReaction.lastIndexOf("\"stamp\"") + 9,
                    currentReaction.length() - 7).replace("T", " "))).getTime();
            Log.d("markermarkermarkermarkermarker", "14");
            insertRemoteReactionToLocal(new HoplyReaction(userId, postId, type, timeMillis));
            inserts++;
        }
        return inserts;
    }

    public void clearAllLocalReactions() {
        Log.d("markermarkermarkermarkermarker", "13");
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(Executors.newSingleThreadExecutor());
        Log.d("markermarkermarkermarkermarker", "14");
        completionService.submit(() -> {
            Log.d("markermarkermarkermarkermarker", "17");
            dao.clearAllLocalReactions();
            Log.d("markermarkermarkermarkermarker", "18");
            return true;
        });
        try {
            Log.d("markermarkermarkermarkermarker", "15");
            completionService.take().get();
            Log.d("markermarkermarkermarkermarker", "16");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String[] getRemoteDataFrom(String remoteDBURL) {
        Log.d("markermarkermarkermarkermarker", "10");
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
        Log.d("markermarkermarkermarkermarker", "11");
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
        for (int i = 0; i < dataList.length; i++)
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
        Log.d("maybemaybe senddata", data);
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
            Log.d("maybemaybe Stringbuilder Senddata", sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void deleteDataFromRemoteDB(String remoteRequest) {
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> {
            try {
                URL url = new URL(remoteRequest);
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                con.setRequestMethod("DELETE");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization" , "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYXBwMjAyMiJ9.iEPYaqBPWoAxc7iyi507U3sexbkLHRKABQgYNDG4Awk");
                StringBuilder sb = new StringBuilder();
                if (con.getResponseCode()/100 == 4 || con.getResponseCode()/100 == 5) {
                    Reader errorStream = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    for (int c;(c = errorStream.read()) >= 0;)
                        sb.append((char)c);
                }
                Log.d("maybemaybe Stringbuilder deleteremotedata", sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        });
        try {
            completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void patchDataToRemoteDB(String remoteRequest, String data) {
        try {
            URL url = new URL(remoteRequest);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("PATCH");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization" , "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYXBwMjAyMiJ9.iEPYaqBPWoAxc7iyi507U3sexbkLHRKABQgYNDG4Awk");
            con.setDoOutput(true);
            try (OutputStream output = con.getOutputStream()) {
                output.write(data.getBytes());
            }
            StringBuilder sb = new StringBuilder();
            if (con.getResponseCode()/100 == 4 || con.getResponseCode()/100 == 5) {
                Reader errorStream = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                for (int c;(c = errorStream.read()) >= 0;)
                    sb.append((char)c);
            }
            Log.d("maybemaybe Stringbuilder patchdata", sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String convertUserToString(HoplyUser user) {
        return "{\"id\":\"" + user.getUserId() +
                "\",\"name\":\"" + user.getUserName() +
                "\",\"stamp\":\"" + new Timestamp(user.getTimestamp()).toString().trim()
                .replace(" ", "T") + "+02:00\"}";
    }

    private String convertPostToString(HoplyPost post, double latitude, double longitude) {
        String postString =  "{\"id\":" + post.getPostId() +
                ",\"user_id\":\"" + post.getUserId() + "\""
                + ",\"content\":\"" + post.getContent() + "\"";
        if(latitude != 200 && longitude != 200)
            postString += "$LA§:" + latitude
                    + "$LO§:" + longitude + "\""
                    + ",\"stamp\":\"" + new Timestamp(post.getTimestamp()).toString().trim()
                    .replace(" ", "T") + "+02:00\"}";
        else
            postString += ",\"stamp\":\"" + new Timestamp(post.getTimestamp()).toString().trim()
                    .replace(" ", "T") + "+02:00\"}";
        return postString;
    }

    private String convertReactionToString(HoplyReaction reaction) {
        return "{\"user_id\":\"" + reaction.getUserId() + "\"" +
                ",\"post_id\":" + reaction.getPostId() +
                ",\"type\":" + reaction.getReactionType() +
                ",\"stamp\":\"" + new Timestamp(reaction.getTimestamp()).toString().trim()
                .replace(" ", "T") + "+02:00\"}";
    }
}
