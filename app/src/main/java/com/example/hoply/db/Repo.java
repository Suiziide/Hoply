package com.example.hoply.db;

import android.app.Application;

import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.hoply.db.HoplyDao;
import com.example.hoply.db.HoplyDatabase;

public class Repo {
    private HoplyDao dao;

    public Repo(Application application) {
        HoplyDatabase db = HoplyDatabase.getDatabase(application);
        dao = db.hoplyDao();
    }

    void insertUser(HoplyUser user){
        dao.insertUser(user);
    }

    void insertPost(HoplyPost post){
        dao.insertPost(post);
    }

    void insertReaction(HoplyReaction reaction){
        dao.insertReaction(reaction);
    }

    boolean compareUser(String userId){
        return userId.equals(dao.compareUser(userId));
    }

}
