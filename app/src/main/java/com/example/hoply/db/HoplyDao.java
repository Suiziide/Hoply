package com.example.hoply.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HoplyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertUser(HoplyUser user);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPost(HoplyPost post);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertReaction(HoplyReaction reaction);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertLocation(HoplyLocation location);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertComment(HoplyComment comment);

    @Query("SELECT * FROM posts WHERE id = :postid")
    HoplyPost returnPostFromId(Integer postid);

    @Query("SELECT * FROM posts ORDER BY stamp DESC")
    LiveData<List<HoplyPost>> getAllPosts();

    @Query("SELECT id FROM posts ORDER BY id DESC LIMIT 1")
    Integer getLatestID();

    @Query("SELECT * FROM users WHERE id = :userId")
    HoplyUser returnUserFromId(String userId);

    @Query("SELECT * FROM location WHERE postid = :postid")
    HoplyLocation returnLocationFromId(Integer postid);

    @Query("SELECT COUNT(*) FROM reactions WHERE post_id = :postid AND type = :reactionType")
    Integer returnReactionsFromTypeAndID(Integer postid, int reactionType);

    @Query("SELECT type FROM reactions WHERE user_id = :userid AND post_id = :postid")
    Integer returnUserReactionToPost(String userid, Integer postid);

    @Query("SELECT * FROM comments ORDER BY stamp DESC")
    LiveData<List<HoplyComment>> getAllComments();

    @Query("DELETE FROM reactions WHERE user_id = :userid AND post_id = :postid")
    Integer removeUserReactionFromPost(String userid, Integer postid);

    @Query("DELETE FROM reactions")
    void clearAllLocalReactions();

    @Query("DELETE FROM users")
    void clearAllData();
}
