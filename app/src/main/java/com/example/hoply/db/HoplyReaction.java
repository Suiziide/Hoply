package com.example.hoply.db;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import java.sql.Timestamp;

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
        String userId;

        @ColumnInfo(name = "post_id", index = true)
        @NonNull
        Integer postId;

        @ColumnInfo(name = "type")
        @NonNull
        int reactionType;

        @ColumnInfo(name = "stamp")
        @NonNull
        long timestamp;

        public HoplyReaction(String userId, Integer postId, int reactionType){
                this.userId = userId;
                this.postId = postId;
                this.reactionType = reactionType;
                this.timestamp = System.nanoTime();
        }
}