package com.example.hoply.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class HoplyUser {
    @PrimaryKey()
    @ColumnInfo(name = "id")
    @NonNull
    private final String userId;

    @NonNull
    @ColumnInfo(name = "name")
    private final String userName;

    @NonNull
    @ColumnInfo(name = "stamp")
    private long timestamp;

    /**
     * Constructor for creating users based on the given parameters with autogenerated timestamp
     * @param userId - the id of the user, which must be unique
     * @param userName - the name of the user, which should be unique
     */
    public HoplyUser(@NonNull String userId, @NonNull String userName){
        this.userId = userId;
        this.userName = userName;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructor for creating users based on the given parameters with manual timestamp
     * @param userId - the id of the user, which must be unique
     * @param userName - the name of the user, which should be unique
     * @param timeMillis - the time of which this user was created
     */
    public HoplyUser(@NonNull String userId, @NonNull String userName, long timeMillis){
        this.userId = userId;
        this.userName = userName;
        this.timestamp = timeMillis;
    }

    /**
     * Returns the id of this user
     * @return this users userId
     */
    @NonNull
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the name of this user
     * @return this users userName
     */
    @NonNull
    public String getUserName() {
        return userName;
    }

    /**
     * Returns a long describing the time of which the user was made
     * @return this users timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of when this user was created
     * @param timestamp - the time of which this user was created
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}