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

    public void insertUser(HoplyUser user){
        dao.insertUser(user);
    }

    public void insertPost(HoplyPost post){
        dao.insertPost(post);
    }

    public void insertReaction(HoplyReaction reaction){
        dao.insertReaction(reaction);
    }

    public HoplyUser returnUserFromId(String userId){
        return dao.returnUserFromId(userId);
    }

}
