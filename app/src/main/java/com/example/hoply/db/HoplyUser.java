package com.example.hoply.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class HoplyUser {
    @PrimaryKey()
    @ColumnInfo(name = "id")
    @NonNull
    private final String userId;

    @NonNull
    @ColumnInfo(name = "name")
    private final String userName;

    @NonNull
    @ColumnInfo(name = "stamp")
    private long timestamp;

    public HoplyUser(@NonNull String userId, @NonNull String userName){
        this.userId = userId;
        this.userName = userName;
        this.timestamp = System.currentTimeMillis();
    }

    public HoplyUser(@NonNull String userId, @NonNull String userName, long timeMillis){
        this.userId = userId;
        this.userName = userName;
        this.timestamp = timeMillis;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    @NonNull
    public String getUserName() {
        return userName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}