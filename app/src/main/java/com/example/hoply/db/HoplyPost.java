package com.example.hoply.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import static androidx.room.ForeignKey.CASCADE;
import androidx.room.PrimaryKey;

@Entity(tableName = "posts",
        foreignKeys = @ForeignKey(
                entity = HoplyUser.class,
                parentColumns = "id",
                childColumns = "user_id",
            onDelete = CASCADE))
public class HoplyPost {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    int postId;

    @NonNull
    @ColumnInfo(name = "user_id")
    String userId;

    @NonNull
    @ColumnInfo(name = "")
}
