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

        /**
         * Constructor for creating reactions based on the given attributes
         * @param userId - the id of the user who made the reaction
         * @param postId - the id of the post the reaction was made on
         * @param reactionType - what type of reaction was made (1 - like, 2 - dislike, 3 - could not care less)
         * @param timestamp - the time of which the reaction was made in milliseconds
         */
        public HoplyReaction(@NonNull String userId, @NonNull Integer postId, Integer reactionType, long timestamp) {
                this.userId = userId;
                this.postId = postId;
                this.reactionType = reactionType;
                this.timestamp = timestamp;
        }

        /**
         * Returns the id of the user who made this reaction
         * @return this reactions userId
         */
        @NonNull
        public String getUserId() {return userId;}

        /**
         * Returns the id of the post that the reaction was made to
         * @return this reactions postId
         */
        @NonNull
        public Integer getPostId() {return postId;}

        /**
         * Returns the type of reaction this reaction is
         * @return this reactions type
         */
        @NonNull
        public Integer getReactionType() {return reactionType;}

        /**
         * Returns the timestamp describing when this reaction was made in milliseconds
         * @return this reactions timestamp
         */
        public long getTimestamp() {return timestamp;}

        /**
         * Sets the id of the post this reaction was made to
         * @param postId - the id of the post this reaction was made to
         */
        public void setPostId(Integer postId) {this.postId = postId;}
}