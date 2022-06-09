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

    /**
     * Constructor for creating locations based on the given parameters
     * @param latitude - the latitudinal component of the location
     * @param longitude - the longitudinal component of the location
     * @param postId - the id of the post the location is related to
     */
    public HoplyLocation (double latitude, double longitude, Integer postId){
        this.latitude = latitude;
        this.longitude = longitude;
        this.postId = postId;
    }

    /**
     * Returns the postId related to this location
     * @return this locations postId
     */
    public Integer getPostId() {
        return postId;
    }

    /**
     * Returns the latitudinal component of this location
     * @return this locations latitudinal component
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitudinal component of the location
     * @return this locations longitudinal location
     */
    public double getLongitude() {
        return longitude;
    }
}