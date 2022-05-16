package com.example.hoply.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import static androidx.room.ForeignKey.CASCADE;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;

@Entity(tableName = "posts",
        foreignKeys = {@ForeignKey(
                entity = HoplyUser.class,
                parentColumns = "id",
                childColumns = "user_id",
            onDelete = CASCADE)})
public class HoplyPost {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    Integer postId;

    @NonNull
    @ColumnInfo(name = "user_id", index = true)
    String userId;

    @NonNull
    @ColumnInfo(name = "content")
    String content;

    @NonNull
    @ColumnInfo(name = "stamp")
    long timestamp;

    public HoplyPost(String userId, String content){
        this.userId = userId;
        this.content = content;
        this.timestamp = System.nanoTime();
    }

    public Integer getPostId() {
        return postId;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    @NonNull
    public String getContent() {
        return content;
    }

    @NonNull
    public long getTimestamp() {
        return timestamp;
    }
}
