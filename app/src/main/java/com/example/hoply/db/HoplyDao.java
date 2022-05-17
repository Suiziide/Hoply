package com.example.hoply.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HoplyDao {


    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertUser(HoplyUser user);

    @Insert()
    void insertPost(HoplyPost post);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertReaction(HoplyReaction reaction);

    @Insert()
    void insertLocation(HoplyLocation location);

    @Query("SELECT * FROM posts ORDER BY stamp DESC")
    LiveData<List<HoplyPost>> getAllPosts();

    @Query("SELECT * FROM users WHERE id = :userId")
    HoplyUser returnUserFromId(String userId);

    @Query("SELECT * FROM location WHERE postid = :postid")
    HoplyLocation returnLocationFromId(Integer postid);
}
