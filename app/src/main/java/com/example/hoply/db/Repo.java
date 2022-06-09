package com.example.hoply.db;

import android.app.Application;

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

    /**
     * Constructor for creating a repository linked to the application given as a parameter
     * @param application - the application in which to create the repository
     */
    public Repo(Application application) {
        HoplyDatabase db = HoplyDatabase.getDatabase(application);
        dao = db.hoplyDao();
        allPosts = dao.getAllPosts();
        allComments = dao.getAllComments();
    }

    /**
     * Starts a scheduled executor, which is responsible for synchronizing with remote
     */
    public void startTimer() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::syncWithRemote, 0, 3, TimeUnit.SECONDS);
    }

    /**
     * Clears all the data stored in the local database
     */
    public void clearAllData() {
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseLocalInsertExecutor);
        completionService.submit(() -> 0 < dao.clearAllData());
        try {
            completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts a HoplyUser in the local databse if the user can be inserted into the remote database
     * @param user - the user to insert into both databases
     * @return whether the insertion was successful
     */
    public boolean insertLocalUser(HoplyUser user) {
        if (sendLocalDataToRemoteDB("https://caracal.imada.sdu.dk/app2022/users", convertUserToString(user))) {
            HoplyDatabase.databaseWriteExecutor.submit(() -> dao.insertUser(user));
            return true;
        } else
            return false;
    }

    // Auxiliary method for inserting HoplyUsers from the remote database into the local database
    // @param user - the HoplyUser to be inserted into the local database from the remote database
    private void insertRemoteUserToLocal(HoplyUser user) {
        HoplyDatabase.databaseLocalInsertExecutor.submit(() -> dao.insertUser(user));
    }

    /**
     * Insert a HoplyPost along with its location into the local database
     * if it can be inserted into the remote database
     * @param post - the HoplyPost to be inserted into the both databases
     * @param latitude - the latitudinal part of the posts location
     * @param longitude - the longitudinal of the posts location
     * @return whether or not the insertion was successful
     */
    public boolean insertLocalPost(HoplyPost post, double latitude, double longitude) {
        post.setContent(doubleTrimAndReverse(post.getContent()));
        HoplyPost remotePost = post.copy();
        remotePost.setContent(formatContent(remotePost.getContent()));
        if (sendLocalDataToRemoteDB("https://caracal.imada.sdu.dk/app2022/posts", convertPostToString(remotePost, latitude, longitude))) {
            HoplyDatabase.databaseWriteExecutor.submit(() -> dao.insertPost(post));
            if (latitude >= -180.0 && longitude <= 180.0 && longitude >= -180.0 && latitude <= 180.0)
                insertLocation(new HoplyLocation(latitude, longitude, post.getPostId()));
            return true;
        } else
            return false;
    }

    // Auxiliary method for inserting HoplyPosts from the remote database into the local database
    // @param post - the post to be inserted into the local database from the remote database
    private void insertRemotePostToLocal(HoplyPost post) {
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

    /**
     * Inserts a HoplyReaction into the local database if it can be inserted into the remote database
     * @param reaction - the HoplyReaction to be inserted into both databases
     */
    public void insertLocalReaction(HoplyReaction reaction) {
        if (sendLocalDataToRemoteDB("https://caracal.imada.sdu.dk/app2022/reactions", convertReactionToString(reaction)))
            HoplyDatabase.databaseWriteExecutor.submit(() -> dao.insertReaction(reaction));
    }

    // Auxiliary method for inserting HoplyReactions from the remote database into the local database
    // @param reaction - the reaction to be inserted into the local database from the remote database
    private void insertRemoteReactionToLocal(HoplyReaction reaction) {
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

    // Auxiliary method for inserting a HoplyLocation into the local database
    // @param location - the location to be inserted into the local database
    private void insertLocation(HoplyLocation location){
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

    /**
     * Inserts a HoplyComment into the local database if it can be inserted into remote databse
     * @param comment - the comment to be inserted into both databases
     * @return whether the comment was successfully inserted
     */
    public boolean insertLocalComment(HoplyComment comment){
        comment.setContent(doubleTrimAndReverse(comment.getContent()));
        HoplyComment remoteComment = comment.copy();
        remoteComment.setContent(formatContent(remoteComment.getContent()));
        if (updateRemoteComments(remoteComment)) {
            HoplyDatabase.databaseWriteExecutor.submit(() -> dao.insertComment(comment));
            return true;
        } else
            return false;
    }

    // Auxiliary method for inserting HoplyComments into the local database from the remote database
    // @param comment - the HoplyComment to be inserted into the local database
    private void insertRemoteCommentToLocal(HoplyComment comment) {
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

    /**
     * Returns the location data related to a post given the id of the post
     * @param postId - the id of the specified post
     * @return the location data of the specified post
     */
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

    /**
     * Returns a HoplyUser given its id or null if it does not exist
     * @param userId - the id of the specified HoplyUser
     * @return the HoplyUser for the specified id or null if it does not exist
     */
    public HoplyUser returnUserFromId (String userId) {
        HoplyUser foundUser = null;
        ExecutorCompletionService<HoplyUser> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.returnUserFromId(userId));
        try {
            foundUser = completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return foundUser;
    }

    /**
     * Returns a HoplyPost given its postId or null if it does not exist
     * @param postId - the id for the specific post to find
     * @return the post corresponding to the postId or the null if it does not exist
     */
    public HoplyPost returnPostFromId (Integer postId) {
        HoplyPost foundPost = null;
        ExecutorCompletionService<HoplyPost> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.returnPostFromId(postId));
        try {
            foundPost = completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return foundPost;
    }

    /**
     * Returns the id of the latest inserted post
     * @return the id of the latest inserted post
     */
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

    /**
     * Returns a LiveData list containing all HoplyComments in the local database
     * @return a list containing all HoplyComments in the local database
     */
    public LiveData<List<HoplyComment>> getAllComments () {
        return allComments;
    }

    /**
     * Returns all post in the local database after having synchronized with the remote database
     * @return a LiveData list containing all posts in the local database
     */
    public LiveData<List<HoplyPost>> syncWithRemote() {
        clearAllLocalReactions();
        getAllRemotePostsAndUsers();
        getAllRemoteReactions();
        return allPosts;
    }

    // Auxiliary method to get all reactions from the remote database during synchronization
    private void getAllRemoteReactions() {
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> 0 <= createAndInsertRemoteReactions(
                getRemoteDataFrom("https://caracal.imada.sdu.dk/app2022/reactions")));
        try {
            completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the HoplyReaction specified by the given postid and reactiontype
     * @param postid - the id of the post the reaction was made
     * @param reactionType -
     * @return
     */
    public Integer returnReactionsFromTypeAndID(Integer postid, Integer reactionType) {
        Integer foundReaction = null;
        ExecutorCompletionService<Integer> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.returnReactionsFromTypeAndID(postid, reactionType));
        try {
            foundReaction = completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return foundReaction;
    }

    /**
     * Returns a specified users reaction to a given post
     * @param userId - the user who has reacted
     * @param postId - the post to which the user has reacted
     * @return the reactionType made to the specified post by the specified user
     */
    public Integer returnUserReactionToPost (String userId, Integer postId) {
        Integer foundReaction = null;
        ExecutorCompletionService<Integer> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> dao.returnUserReactionToPost(userId, postId));
        try {
            foundReaction = completionService.take().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return foundReaction;
    }

    /**
     * Removes a reaction from a specified user from a specified post
     * @param userId - the user who made the reaction
     * @param postId - the post the reaction was made on
     */
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

    // Auxiliary method for getting all remote posts and users from the remote database
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

    // Auxiliary method for creating and inserting HoplyUsers into the local database from the remote database
    // @param responseBody - Must be the result from calling getRemoteDataFrom on a http-request
    private int createAndInsertUser(String[] responseBody) {
        String currentUser;
        int inserts = 0;
        // goes through all the users contained in responseBody
        while (inserts < responseBody.length) {
            // extracts all attributes needed to create a user
            currentUser = responseBody[inserts];
            String userId = currentUser.substring(currentUser.indexOf("\"id\"") + 6,
                    currentUser.indexOf("\"name\"") - 2);
            String userName = currentUser.substring(currentUser.indexOf("\"name\"") + 8,
                    currentUser.indexOf("\"stamp\"") - 2);
            long timeMillis = Timestamp.valueOf((currentUser.substring(currentUser.lastIndexOf("\"stamp\"") + 9,
                    currentUser.length() - 7).replace("T", " "))).getTime();
            // inserts the user based upon the extracted attributes
            insertRemoteUserToLocal(new HoplyUser(userId, userName, timeMillis));
            inserts++;
        }
        return inserts;
    }

    // Auxiliary method for creating and inserting posts, comments and location data from the remote
    // database into the local database
    // @param responseBody - Must be the result from calling getRemoteDataFrom on a http-request
    private int createAndInsertPosts(String[] responseBody) {
        double latitude;
        double longitude;
        int inserts = responseBody.length-1;
        String[] comments = null;
        // goes through all the posts contained in responseBody
        while (inserts >= 0) {
            latitude = 200;
            longitude = 200;
            // extracts all attributes needed to create a post
            String currentPost = responseBody[inserts];
            Integer postId = Integer.parseInt(currentPost.substring(currentPost.indexOf("\"id\"") + 5,
                    currentPost.indexOf("\"user_id\"") - 1));
            String userId = currentPost.substring(currentPost.indexOf("\"user_id\"") + 11,
                    currentPost.indexOf("\"content\"") - 2);
            String content = currentPost.substring(currentPost.indexOf("\"content\"") + 11,
                    currentPost.indexOf("\"stamp\"") - 2);
            long timeMillis = Timestamp.valueOf((currentPost.substring(currentPost.lastIndexOf("\"stamp\"") + 9,
                    currentPost.length() - 7).replace("T", " "))).getTime();
            // if there have been made comments on the post they are extracted
            // and removed from the content of the post
            if (content.contains("$con§") && content.contains("$pid§") && content.contains("$uid§")
                    && content.contains("$tim§")) {
                comments = extractComments(content);
                content = content.substring(0, content.indexOf("¤$con§:"));
            }
            // if there is location data included in the content of the post it is extracted
            // and removed from the content of the post
            if (content.contains("$LA§:") && content.contains("$LO§:")) {
                latitude = Double.parseDouble(content.substring(content.indexOf("$LA§:") + 5, content.indexOf("$LO§:")));
                longitude = Double.parseDouble(content.substring(content.indexOf("$LO§:") + 5));
                content = content.substring(0, content.indexOf("$LA§:"));
            }
            // takes escaped characters like newlines etc. and reverts them to default format
            if (content.contains("\\"))
                content = content.replace("\\\\n", "\n")
                        .replace("\\\\r", "\r").replace("\\\\t", "\t")
                        .replace("\\\\", "\\").replace("\\\"","\"");
            // inserts the post based on the extracted attributes
            insertRemotePostToLocal(new HoplyPost(postId, userId, content, timeMillis));
            // insert location data if any could be extracted into the local database
            if (latitude != 200 && longitude != 200)
                insertLocation(new HoplyLocation(latitude ,longitude, postId));
            // inserts comments if any could be extracted into the local database
            if (comments != null && comments.length > 0)
                createAndInsertComments(comments);
            inserts--;
        }
        return inserts;
    }

   // Auxiliary method for creating and inserting comments into the local database from the remote database
    private void createAndInsertComments(String[] comments) {
        int inserts = comments.length-1;
        while (inserts >= 0) {
            String currentComment = comments[inserts];
            String commentContent = currentComment.substring(currentComment.indexOf("$con§:") + 6, currentComment.indexOf("$pid§:"));
            Integer commentId = Integer.parseInt(currentComment.substring(currentComment.indexOf("$pid§:") + 6, currentComment.indexOf("$uid§:")));
            String commentUserId = currentComment.substring(currentComment.indexOf("$uid§:") + 6, currentComment.indexOf("$tim§"));
            Long timestamp = Long.parseLong(currentComment.substring(currentComment.indexOf("$tim") + 6));
            // takes escaped characters like newlines etc. and reverts them to default format
            if (commentContent.contains("\\"))
                commentContent = commentContent.replace("\\\\n", "\n")
                        .replace("\\\\r", "\r").replace("\\\\t", "\t")
                        .replace("\\\\", "\\").replace("\\\"","\"");
            // inserts comments based upon the extracted attributes into the local database
            insertRemoteCommentToLocal(new HoplyComment(commentUserId, commentId, commentContent, timestamp));
            inserts--;
        }
    }

    // Auxiliary method for extracting comments from the content of the post
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

    // Auxiliary method for creating and inserting reactions, from the remote database into the local database
    // @param responseBody - Must be the result from calling getRemoteDataFrom on a http-request
    private int createAndInsertRemoteReactions(String[] responseBody) {
        if (responseBody.length <= 0)
            return 0;
        String currentReaction;
        int inserts = 0;
        clearAllLocalReactions();
        // goes through all the reactions in responseBody
        while (inserts < responseBody.length) {
            // extracts the needed attributes to create a reaction
            currentReaction = responseBody[inserts];
            String userId = currentReaction.substring(currentReaction.indexOf("\"user_id\"") + 11,
                    currentReaction.indexOf("\"post_id\"") - 2);
            Integer postId = Integer.parseInt(currentReaction.substring(currentReaction.indexOf("\"post_id\"") + 10,
                    currentReaction.indexOf("\"type\"") - 1));
            int type = Integer.parseInt(currentReaction.substring(currentReaction.indexOf("\"type\"") + 7,
                    currentReaction.indexOf("\"stamp\"") - 1));
            long timeMillis = Timestamp.valueOf((currentReaction.substring(currentReaction.lastIndexOf("\"stamp\"") + 9,
                    currentReaction.length() - 7).replace("T", " "))).getTime();
            // inserts a reaction into the local database based upon the extracted attributes
            insertRemoteReactionToLocal(new HoplyReaction(userId, postId, type, timeMillis));
            inserts++;
        }
        return inserts;
    }

    // Auxiliary method for creating "GET" type http-requests to the remote database
    private String[] getRemoteDataFrom(String remoteDBURL) {
        String[] responseBody = new String[0];
        try {
            // initiates the connection to remoteDBURL
            URL url = new URL(remoteDBURL);
            URLConnection con = url.openConnection();
            // Authorization key
            con.setRequestProperty("Authorization" , "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYXBwMjAyMiJ9.iEPYaqBPWoAxc7iyi507U3sexbkLHRKABQgYNDG4Awk");
            // collects the result of the http-request to be contained in res
            InputStream response = con.getInputStream();
            String res;
            try (Scanner scanner = new Scanner(response)) {
                res = (scanner.useDelimiter("\\A").next());
                res = res.substring(1,res.length()-1);
            }
            response.close(); // closes the connection
            responseBody = extractData(res); // extracts the data in res to be individual strings
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseBody;
    }

    // Auxiliary method for extracting the resulting data from specific "GET" http-request
    // for this projects remote database
    private String[] extractData(String res) {
        List<Integer> dataStart = new ArrayList<>();
        List<Integer> dataEnd = new ArrayList<>();
        boolean countBraces = true;
        // counts all the sets of curly brackets not contained in the content of posts or users name
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
        // extracts the individual objects based on the pairs of open and close curly brackets
        String[] dataList = new String[dataStart.size()];
        for (int i = 0; i < dataList.length; i++)
            dataList[i] = (res.substring(dataStart.get(i) + 1, dataEnd.get(i)));
        return dataList;
    }

    // Auxiliary method for sending data to the remote database
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

    // Auxiliary method for creating "POST" type http-requests
    private boolean sendLocalDataTo(String remoteDBURL, String data) {
        boolean result = true;
        try {
            // initiates the connection
            URL url = new URL(remoteDBURL);
            URLConnection con = url.openConnection();
            // sets the type of the request to "POST"
            con.setDoOutput(true);
            // the format of the data being sent
            con.setRequestProperty("Content-Type", "application/json");
            // Authorization key
            con.setRequestProperty("Authorization" , "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYXBwMjAyMiJ9.iEPYaqBPWoAxc7iyi507U3sexbkLHRKABQgYNDG4Awk");
            // writes the data to the remote database
            try (OutputStream output = con.getOutputStream()) {
                output.write(data.getBytes());
            }
            // reads any errors codes that may be returned by the
            HttpURLConnection connection = (HttpURLConnection) con;
            StringBuilder sb = new StringBuilder();
            if (connection.getResponseCode()/100 == 4 || connection.getResponseCode()/100 == 5) {
                Reader errorStream = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                for (int c;(c = errorStream.read()) >= 0;)
                    sb.append((char)c);
                result = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Auxiliary method for creating "DELETE" type http-requests
     * @param remoteRequest - a formatted http-request for what to delete
     */
    public void deleteDataFromRemoteDB(String remoteRequest) {
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> {
            try {
                // initiates the connection
                URL url = new URL(remoteRequest);
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                // sets the request type to "DELETE"
                con.setRequestMethod("DELETE");
                // Authorization key
                con.setRequestProperty("Authorization" , "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYXBwMjAyMiJ9.iEPYaqBPWoAxc7iyi507U3sexbkLHRKABQgYNDG4Awk");
                // reads any errors codes that may be returned by the
                StringBuilder sb = new StringBuilder();
                if (con.getResponseCode()/100 == 4 || con.getResponseCode()/100 == 5) {
                    Reader errorStream = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    for (int c;(c = errorStream.read()) >= 0;)
                        sb.append((char)c);
                }
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

    // Auxiliary method for updating the content of the post to include the information about
    // a comment when a comment to that post is made
    private boolean updateRemoteComments(HoplyComment newComment) {
        boolean response = false;
        ExecutorCompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(HoplyDatabase.databaseWriteExecutor);
        completionService.submit(() -> {
            // gets the content of the specified post the comment is being made to
            String oldContent = getRemoteDataFrom(
                    "https://caracal.imada.sdu.dk/app2022/posts?id=eq."
                            + newComment.getPostId())[0];
            // adds the different tags defining the attributes of the comment into the content
            // of the posts content
            oldContent = oldContent.substring(oldContent.indexOf("\"content\"") + 11, oldContent.lastIndexOf("\"stamp\"") - 2);
            String newContent = oldContent +
                    " ¤$con§:" + newComment.getContent() + "$pid§:" + newComment.getPostId()
                    + "$uid§:" + newComment.getUserId() + "$tim§:" + newComment.getTimestamp() + "¤";
            // sends the new new content to be included in the posts content as a "PATCH" request
            // to the remote database
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

    // Auxiliary method for creating "PATCH" type http-requests
    private boolean patchDataToRemoteDB(String remoteRequest, String data) {
        boolean response = true;
        try {
            // initiates the connection
            URL url = new URL(remoteRequest);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            // sets the request type
            con.setRequestMethod("PATCH");
            // sets the format of the content being sent
            con.setRequestProperty("Content-Type", "application/json");
            // Authorization key
            con.setRequestProperty("Authorization" , "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYXBwMjAyMiJ9.iEPYaqBPWoAxc7iyi507U3sexbkLHRKABQgYNDG4Awk");
            // sets the request to be able to write to the remote database
            con.setDoOutput(true);
            // writes the specified data to the remote database
            try (OutputStream output = con.getOutputStream()) {
                output.write(data.getBytes());
            }
            // reads any errors codes that may be returned by the
            StringBuilder sb = new StringBuilder();
            if (con.getResponseCode()/100 == 4 || con.getResponseCode()/100 == 5) {
                Reader errorStream = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                for (int c;(c = errorStream.read()) >= 0;)
                    sb.append((char)c);
                response =  false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    // Auxiliary method for clearing all reaction from the local database
    private void clearAllLocalReactions() {
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

    // Auxiliary method for converting a HoplyUser to JSON format as a string
    private String convertUserToString(HoplyUser user) {
        return "{\"id\":\"" + user.getUserId() +
                "\",\"name\":\"" + user.getUserName() +
                "\",\"stamp\":\"" + OffsetDateTime
                .ofInstant(Instant.ofEpochMilli(user.getTimestamp()), ZoneId.systemDefault())+ "\"}";
    }

    // Auxiliary method for converting a HoplyPost to JSON format as a string containing
    // location data if location data was active when the post was created
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

    // Auxiliary method for converting a HoplyReaction to JSON format as a string
    private String convertReactionToString(HoplyReaction reaction) {
        return "{\"user_id\":\"" + reaction.getUserId() + "\"" +
                ",\"post_id\":" + reaction.getPostId() +
                ",\"type\":" + reaction.getReactionType() +
                ",\"stamp\":\"" + OffsetDateTime
                .ofInstant(Instant.ofEpochMilli(reaction.getTimestamp()), ZoneId.systemDefault())+ "\"}";
    }

    // Auxiliary method for escaping special characters like newlines etc.
    private String formatContent(String content) {
        return content.replace("\n", "\\n")
                .replace("\r", "\\r").replace("\t", "\\t")
                .replace("\\", "\\\\").replace("\"","\\\"");
    }

    // Auxiliary method for trimming blank space from the content of a comment or post
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