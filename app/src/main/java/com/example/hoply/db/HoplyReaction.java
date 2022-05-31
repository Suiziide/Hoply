package com.example.hoply.db;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "reactions",
        primaryKeys = {"user_id", "post_id", "stamp"},
        foreignKeys = {
                @ForeignKey(
                        entity = HoplyUser.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = CASCADE),
                @ForeignKey(
                        entity = HoplyPost.class,
                        parentColumns = "id",
                        childColumns = "post_id",
                        onDelete = CASCADE)
        })
public class HoplyReaction {

        @ColumnInfo(name = "user_id", index = true)
        @NonNull
        private String userId;

        @ColumnInfo(name = "post_id", index = true)
        @NonNull
        private Integer postId;

        @ColumnInfo(name = "type")
        @NonNull
        private Integer reactionType;

        @ColumnInfo(name = "stamp")
        @NonNull
        private long timestamp;

        public HoplyReaction(@NonNull String userId, @NonNull Integer postId, Integer reactionType, long timestamp) {
                this.userId = userId;
                this.postId = postId;
                this.reactionType = reactionType;
                this.timestamp = timestamp;
        }

        public String getUserId() {return userId;}

        public Integer getPostId() {return postId;}

        public Integer getReactionType() {return reactionType;}

        public long getTimestamp() {return timestamp;}

        public void setUserId(String userId) {this.userId = userId;}

        public void setPostId(Integer postId) {this.postId = postId;}

        public void setReactionType(int reactionType) {this.reactionType = reactionType;}

        public void setTimestamp(long timestamp) {this.timestamp = timestamp;}
}