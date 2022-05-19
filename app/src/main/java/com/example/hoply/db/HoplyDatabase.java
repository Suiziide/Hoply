package com.example.hoply.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {HoplyUser.class, HoplyPost.class, HoplyReaction.class, HoplyLocation.class, HoplyComment.class}, version = 1, exportSchema = false)
public abstract class HoplyDatabase extends RoomDatabase {

    public abstract HoplyDao hoplyDao();
    private static volatile HoplyDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor = Executors.newWorkStealingPool();

    static HoplyDatabase getDatabase(final Context context) {
        if (INSTANCE == null)
            synchronized (HoplyDatabase.class) {
                if (INSTANCE == null)
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            HoplyDatabase.class, "hoply_database").build();
            }
        return INSTANCE;
    }
}