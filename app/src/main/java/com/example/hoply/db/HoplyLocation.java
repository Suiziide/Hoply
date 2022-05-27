package com.example.hoply.db;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;


@Entity(tableName = "location",
foreignKeys = {@ForeignKey(
        entity = HoplyPost.class,
        parentColumns = "id",
        childColumns = "postId",
        onDelete = CASCADE)})

public class HoplyLocation {

    @PrimaryKey()
    @ColumnInfo(name = "postId")
    Integer postId;

    @ColumnInfo(name = "latitude")
    @NonNull
    private double latitude;

    @ColumnInfo(name = "longitude")
    @NonNull
    private double longitude;

    public HoplyLocation (double latitude, double longitude, Integer postId){
        this.latitude = latitude;
        this.longitude = longitude;
        this.postId = postId;
    }

    public Integer getPostId() {
        return postId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
