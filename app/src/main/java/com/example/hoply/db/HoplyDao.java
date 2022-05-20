package com.example.hoply.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HoplyDao {


    @Insert()
    void insertUser(HoplyUser user);

    @Insert()
    void insertPost(HoplyPost post);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertReaction(HoplyReaction reaction);

    @Insert()
    void insertLocation(HoplyLocation location);

    @Insert()
    void insertComment(HoplyComment comment);

    @Query("SELECT * FROM posts WHERE id = :postid")
    HoplyPost returnPostFromId(Integer postid);

    @Query("SELECT * FROM posts ORDER BY stamp DESC LIMIT 30")
    LiveData<List<HoplyPost>> getAllPosts();

    @Query("SELECT * FROM users WHERE id = :userId")
    HoplyUser returnUserFromId(String userId);

    @Query("SELECT * FROM location WHERE postid = :postid")
    HoplyLocation returnLocationFromId(Integer postid);

    @Query("SELECT COUNT(*) FROM reactions WHERE post_id = :postid AND type = :reactionType")
    Integer returnReactionsFromTypeAndID(Integer postid, int reactionType);

    @Query("SELECT type fROM REACTIONS WHERE user_id = :userid AND post_id = :postid")
    Integer returnUserReactionToPost(String userid, Integer postid);

    @Query("SELECT * FROM comments ORDER BY stamp DESC")
    LiveData<List<HoplyComment>> getAllComments();

    @Query("DELETE FROM REACTIONS WHERE user_id = :userid AND post_id = :postid;")
    Integer removeUserReactionFromPost(String userid, Integer postid);
}
