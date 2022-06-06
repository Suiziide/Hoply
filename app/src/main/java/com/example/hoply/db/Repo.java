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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
        executor.scheduleAtFixedRate(this::syncWithRemote, 0, 3, TimeUnit.SECONDS);
    }

    public void clearAllData() {
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseLocalInsertExecutor);
        completionService.submit(() -> 0 < dao.clearAllData());
    }

    public void insertLocalUser(HoplyUser user) {
        HoplyDatabase.databaseWriteExecutor.submit(() -> dao.insertUser(user));
        sendLocalDataToRemoteDB("https://caracal.imada.sdu.dk/app2022/users", convertUserToString(user));
    }

    public void insertRemoteUserToLocal(HoplyUser user) {
        HoplyDatabase.databaseLocalInsertExecutor.submit(() -> dao.insertUser(user));
    }

    public boolean insertLocalPost(HoplyPost post, double latitude, double longitude) {
        post.setContent(doubleTrimAndReverse(post.getContent()));
        HoplyPost remotePost = post.copy();
        remotePost.setContent(formatContent(remotePost.getContent()));
        if (sendLocalDataToRemoteDB("https://caracal.imada.sdu.dk/app2022/posts", convertPostToString(remotePost, latitude, longitude))) {
            HoplyDatabase.databaseWriteExecutor.submit(() -> dao.insertPost(post));
            return true;
        } else
            return false;
    }

    public void insertRemotePostToLocal(HoplyPost post) {
        ExecutorCompletionService<Boolean> completionService = new
                ExecutorCompletionService<>(HoplyDatabase.databaseLocalInsertExecutor);
        completionService.submit(() -> {
            dao.insertPost(post);
            return true;
        });
        try {
            completionService.take().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void insertLocalReaction(HoplyReaction reaction) {
        HoplyDatabase.databaseWriteExecutor.submit(() -> dao.insertReaction(reaction));
        sendLocalDataToRemoteDB("https://caracal.imada.sdu.dk/app2022/reactions", convertReactionToString(reaction));
    }

    public void insertRemoteReactionToLocal(HoplyReaction reaction) {
        ExecutorCompletionService<Boolean> completionService = new
                ExecutorCompletionService<>(HoplyDatabase.databaseLocalInsertExecutor);
        completionService.submit(() -> {
            dao.insertReaction(reaction);
            return true;
        });
        try {
            completionService.take().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void insertLocation(HoplyLocation location){
        ExecutorCompletionService<Boolean> completionService = new
                ExecutorCompletionService<>(HoplyDatabase.databaseLocalInsertExecutor);
        completionService.submit(() -> {
            dao.insertLocation(location);
            return true;
        });
        try {
            completionService.take().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public boolean insertLocalComment(HoplyComment comment){
        comment.setContent(doubleTrimAndReverse(comment.getContent()));
        HoplyComment remoteComment = comment.copy();
        remoteComment.setContent(formatContent(remoteComment.getContent()));
        if (updateRemoteComments(comment)) {
            HoplyDatabase.databaseWriteExecutor.submit(() -> dao.insertComment(comment));
            return true;
        } else
            return false;
    }

    public void insertRemoteCommentToLocal(HoplyComment comment) {
        ExecutorCompletionService<Boolean> completionService = new
                ExecutorCompletionService<>(HoplyDatabase.databaseLocalInsertExecutor);
        completionService.submit(() -> {
            dao.insertComment(comment);
            return true;
        });
        try {
            completionService.take().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public HoplyLocation returnLocationFromId (Integer postId) {
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

    public LiveData<List<HoplyPost>> syncWithRemote() {
        // deleteDataFromRemoteDB("https://caracal.imada.sdu.dk/app2022/reactions");
        // deleteDataFromRemoteDB("https://caracal.imada.sdu.dk/app2022/posts");
        clearAllLocalReactions();
        getAllRemotePostsAndUsers();
        getAllRemoteReactions();
        return allPosts;
    }

    private void getAllRemoteReactions() {
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> {
            int inserts = createAndInsertRemoteReactions(getRemoteDataFrom("https://caracal.imada.sdu.dk/app2022/reactions"));
            return inserts >= 0;
        });
        try {
            completionService.take().get();
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

    public void removeUserReactionFromPost (String userId, Integer postId){
        ExecutorCompletionService<Integer> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.removeUserReactionFromPost(userId, postId));
        try {
            completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
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
                if (content.contains("¤$con§")) {
                    longitude = Double.parseDouble(content.substring(content.indexOf("$LO§:") + 5, content.indexOf("¤$con§:")));
                    content = content.substring(0, content.indexOf("$LA§:")) + content.substring(content.indexOf("¤$con§"));
                } else {
                    longitude = Double.parseDouble(content.substring(content.indexOf("$LO§:") + 5));
                    content = content.substring(0, content.indexOf("$LA§:"));
                }
            }
            if (content.contains("$con§") && content.contains("$pid§") && content.contains("$uid§")
                    && content.contains("$tim§")) {
                comments = extractComments(content);
                content = content.substring(0, content.indexOf("¤$con§:"));
            }
            long timeMillis = Timestamp.valueOf((currentPost.substring(currentPost.lastIndexOf("\"stamp\"") + 9,
                    currentPost.length() - 7).replace("T", " "))).getTime();
            if (content.contains("\\"))
                content = content.replace("\\\\n", "\n")
                        .replace("\\\\r", "\r").replace("\\\\t", "\t")
                        .replace("\\\\", "\\").replace("\\\"","\"");
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
            if (commentContent.contains("\\"))
                commentContent = commentContent.replace("\\\\n", "\n")
                        .replace("\\\\r", "\r").replace("\\\\t", "\t")
                        .replace("\\\\", "\\").replace("\\\"","\"");
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
        clearAllLocalReactions();
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
            insertRemoteReactionToLocal(new HoplyReaction(userId, postId, type, timeMillis));
            inserts++;
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
        boolean countBraces = true;
        for (int i = 0; i < res.length(); i++) {
            if (i < res.length() - 5 && res.charAt(i) == '\"' &&  res.charAt(i+1) == 'i' &&
                    res.charAt(i+2) == 'd' && res.charAt(i+3) == '\"' && res.charAt(i+4) == ':')
                countBraces = false;
            else if (i < res.length() - 8 && res.charAt(i) == '\"' &&  res.charAt(i+1) == 's' &&
                    res.charAt(i+2) == 't' && res.charAt(i+3) == 'a' && res.charAt(i+4) == 'm' &&
                    res.charAt(i+5) == 'p' && res.charAt(i+6) == '\"' && res.charAt(i+7) == ':' &&
                    res.charAt(i+8) == '\"')
                countBraces = true;
            if (countBraces)
                if (res.charAt(i) == '{')
                    dataStart.add(i);
                else if (res.charAt(i) == '}')
                    dataEnd.add(i);
        }
        String[] dataList = new String[dataStart.size()];
        for (int i = 0; i < dataList.length; i++)
            dataList[i] = (res.substring(dataStart.get(i) + 1, dataEnd.get(i)));
        return dataList;
    }

    private boolean sendLocalDataToRemoteDB(String remoteDBURL, String data) {
        boolean result = true;
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> sendLocalDataTo(remoteDBURL, data));
        try {
            result = completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean sendLocalDataTo(String remoteDBURL, String data) {
        boolean result = true;
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
                result = false;
            }
            Log.d("maybemaybemaybemaybe Stringbuilder Senddata", sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
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
                Log.d("maybemaybemaybemaybe Stringbuilder deleteremotedata", sb.toString());
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

    private boolean updateRemoteComments(HoplyComment newComment) {
        boolean response = false;
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> {
            String oldContent = getRemoteDataFrom(
                    "https://caracal.imada.sdu.dk/app2022/posts?id=eq."
                            + newComment.getPostId())[0];
            oldContent = oldContent.substring(oldContent.indexOf("\"content\"") + 11, oldContent.lastIndexOf("\"stamp\"") - 2);
            String newContent = oldContent +
                    " ¤$con§:" + newComment.getContent() + "$pid§:" + newComment.getPostId()
                    + "$uid§:" + newComment.getUserId() + "$tim§:" + newComment.getTimestamp() + "¤";
            return patchDataToRemoteDB("https://caracal.imada.sdu.dk/app2022/posts?id=eq."
                    + newComment.getPostId(),"{\"content\":\"" + newContent + "\"}");
        });
        try {
            response =  completionService.take().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return response;
    }

    public boolean patchDataToRemoteDB(String remoteRequest, String data) {
        boolean response = true;
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
                response =  false;
            }
            Log.d("maybemaybe Stringbuilder patchdata", sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public void clearAllLocalReactions() {
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseLocalInsertExecutor);
        completionService.submit(() -> {
            dao.clearAllLocalReactions();
            return true;
        });
        try {
            completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
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
                + ",\"content\":\"" + post.getContent();
        if(latitude != 200 && longitude != 200)
            postString += "$LA§:" + latitude
                    + "$LO§:" + longitude + "\""
                    + ",\"stamp\":\"" + OffsetDateTime
                    .ofInstant(Instant.ofEpochMilli(post.getTimestamp()), ZoneId.systemDefault())+ "\"}";
        else
            postString += "\",\"stamp\":\"" + OffsetDateTime
                    .ofInstant(Instant.ofEpochMilli(post.getTimestamp()), ZoneId.systemDefault()) + "\"}";
        return postString;
    }

    private String convertReactionToString(HoplyReaction reaction) {
        return "{\"user_id\":\"" + reaction.getUserId() + "\"" +
                ",\"post_id\":" + reaction.getPostId() +
                ",\"type\":" + reaction.getReactionType() +
                ",\"stamp\":\"" + OffsetDateTime
                .ofInstant(Instant.ofEpochMilli(reaction.getTimestamp()), ZoneId.systemDefault())+ "\"}";
    }

    private String formatContent(String content) {
        return content.replace("\n", "\\n")
                .replace("\r", "\\r").replace("\t", "\\t")
                .replace("\\", "\\\\").replace("\"","\\\"");
    }

    private String doubleTrimAndReverse(String content) {
        content = content.trim();
        String tempReverse = "";
        for (int i = content.length()-1; i >= 0 ; i--)
            tempReverse += content.charAt(i);
        tempReverse = tempReverse.trim();
        content = "";
        for (int i = tempReverse.length()-1; i >= 0 ; i--)
            content += tempReverse.charAt(i);
        return content;
    }
}
