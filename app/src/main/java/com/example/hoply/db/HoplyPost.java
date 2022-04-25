package com.example.hoply.db;

import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

public class HoplyPost {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    int postId;

    @ForeignKey()
}
