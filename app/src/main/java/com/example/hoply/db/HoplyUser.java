package com.example.hoply.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;

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
    Timestamp timestamp;

    public HoplyUser(@NonNull String name, @NonNull Timestamp timestamp){
        this.userName = name;
        this.timestamp = timestamp;
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
    public Timestamp getTimestamp() {
        return timestamp;
    }
}
