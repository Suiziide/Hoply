package com.example.hoply.db;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;

import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.hoply.db.HoplyDao;
import com.example.hoply.db.HoplyDatabase;

public class Repo {
    private HoplyDao dao;
    private HoplyUser returnUser;

    public Repo(Application application) {
        HoplyDatabase db = HoplyDatabase.getDatabase(application);
        dao = db.hoplyDao();
    }

    public void insertUser(HoplyUser user) {
        HoplyDatabase.databaseWriteExecutor.execute(() -> {
            try {
                dao.insertUser(user);
            } catch (SQLiteConstraintException e) {
                throw e;
            }

        });
    }

    public void insertPost(HoplyPost post){
        HoplyDatabase.databaseWriteExecutor.execute(() -> {
            dao.insertPost(post);
        });

    }

    public void insertReaction(HoplyReaction reaction){
        HoplyDatabase.databaseWriteExecutor.execute(() -> {
            dao.insertReaction(reaction);
        });

    }

    public HoplyUser returnUserFromId(String userId){
        HoplyDatabase.databaseWriteExecutor.execute(() -> {
           returnUser =  dao.returnUserFromId(userId);
        });
        return returnUser;
    }

}
