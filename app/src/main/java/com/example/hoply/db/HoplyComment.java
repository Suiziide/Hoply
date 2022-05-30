package com.example.hoply.db;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "comments",
        primaryKeys = {"user_id", "post_id", "stamp"},
        foreignKeys = {
                @ForeignKey(
                        entity = HoplyPost.class,
                        parentColumns = "id",
                        childColumns = "post_id",
                        onDelete = CASCADE),
                @ForeignKey(
                        entity = HoplyUser.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = CASCADE)
        })
public class HoplyComment {

        @ColumnInfo(name = "user_id", index = true)
        @NonNull
        private String userId;

        @ColumnInfo(name = "post_id", index = true)
        @NonNull
        private Integer postId;

        @ColumnInfo(name = "content")
        @NonNull
        private String content;

        @ColumnInfo(name = "stamp")
        @NonNull
        private long timestamp;

        public HoplyComment(String userId, Integer postId, String content, long timestamp){
                this.userId = userId;
                this.postId = postId;
                this.content = content;
                this.timestamp = timestamp;
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

        public long getTimestamp() {return timestamp;}

        @NonNull
        public void setPostId(Integer postId) {
                this.postId = postId;
        }

        @NonNull
        public void setUserId(String userId) {
                this.userId = userId;
        }

        @NonNull
        public void setContent(String content) {
                this.content = content;
        }

        public void setTimestamp(long timestamp) {this.timestamp = timestamp;}
}
