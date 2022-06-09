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

        /**
         * Constructor for creating comments based on the given parameters
         * @param userId - the id of the user which created the comment
         * @param postId - the id of the post the comment is related to
         * @param content - the content contained within the comment
         * @param timestamp - the time of which the comment was created
         */
        public HoplyComment(String userId, Integer postId, String content, long timestamp){
                this.userId = userId;
                this.postId = postId;
                this.content = content;
                this.timestamp = timestamp;
        }

        /**
         * Returns the postId related to this comments
         * @return this comments postId
         */
        @NonNull
        public Integer getPostId() {
                return postId;
        }

        /**
         * Returns the userId related to this comment
         * @return this comments userId
         */
        @NonNull
        public String getUserId() {
                return userId;
        }

        /**
         * Returns the content related to this comment
         * @return this comments content
         */
        @NonNull
        public String getContent() {
                return content;
        }

        /**
         * Returns the timestamp related to this comment
         * @return this posts timestamp
         */
        public long getTimestamp() {return timestamp;}

        /**
         * sets the postId of this comment
         * @param postId - the id of the post the comment is related to
         */
        @NonNull
        public void setPostId(Integer postId) {
                this.postId = postId;
        }

        /**
         * sets the content of this comment
         * @param content - the content of the string
         */
        @NonNull
        public void setContent(String content) {
                this.content = content;
        }

        /**
         * creates and returns a copy of this comment
         * @return a copy of this comment
         */
        public HoplyComment copy() {
                return (new HoplyComment(this.userId, this.postId,this.content, this.timestamp));
        }
}