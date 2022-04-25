package com.example.hoply.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface HoplyDao {


    @Insert()
    void insertUser(HoplyUser user);

    @Insert()
    void insertPost(HoplyPost post);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertReaction(HoplyReaction reaction);


    @Query("SELECT * FROM users WHERE id = :userId")
    HoplyUser returnUserFromId(String userId);
}
