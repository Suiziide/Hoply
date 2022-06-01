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
    private Integer postId;

    @NonNull
    @ColumnInfo(name = "user_id", index = true)
    private String userId;

    @NonNull
    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "stamp")
    private long timestamp;

    public HoplyPost(@NonNull Integer postId, @NonNull String userId, @NonNull String content){
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public HoplyPost(@NonNull Integer postId, @NonNull String userId, @NonNull String content, long timeMillis){
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.timestamp = timeMillis;
    }

    @NonNull
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setPostId(@NonNull Integer postId) {
        this.postId = postId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public HoplyPost copy() {
        return (new HoplyPost(this.postId, this.userId, this.content, this.timestamp));
    }
}
