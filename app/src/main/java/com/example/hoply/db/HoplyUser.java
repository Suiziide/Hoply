package com.example.hoply.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class HoplyUser {
    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "id")
    String userId;

    @NonNull
    @ColumnInfo(name = "name")
    String userName;

    @NonNull
    @ColumnInfo(name = "stamp")
    long timestamp;

    public HoplyUser(@NonNull String userId, @NonNull String userName){
        this.userId = userId;
        this.userName = userName;
        this.timestamp = System.nanoTime();
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    @NonNull
    public String getUserName() {
        return userName;
    }

    @NonNull
    public long getTimestamp() {
        return timestamp;
    }
}
