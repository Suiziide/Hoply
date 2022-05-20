package com.example.hoply.db;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "posts",
        foreignKeys = {@ForeignKey(
                entity = HoplyUser.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = CASCADE)})
public class HoplyPost {

    @PrimaryKey()
    @ColumnInfo(name = "id")
    @NonNull
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

    public HoplyPost(Integer postId, String userId, String content){
        this.postId = postId;
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
