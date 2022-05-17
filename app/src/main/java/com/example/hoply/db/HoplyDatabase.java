package com.example.hoply.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Database(entities = {HoplyUser.class, HoplyPost.class, HoplyReaction.class, HoplyLocation.class}, version = 1, exportSchema = false)
public abstract class HoplyDatabase extends RoomDatabase {

    public abstract HoplyDao hoplyDao();
    private static volatile HoplyDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 1;
    static final ExecutorService databaseWriteExecutor = new ThreadPoolExecutor(4,
            4, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),
            new ThreadPoolExecutor.DiscardPolicy());

    static HoplyDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (HoplyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            HoplyDatabase.class, "hoply_database").build();
                }
            }
        }
        return INSTANCE;
    }
}