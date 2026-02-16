package com.example.praca4.room.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "known_users")
public class KnownUsers {

    @NonNull
    @PrimaryKey
    private String uuid;

    @ColumnInfo(name = "user_name")
    private String userName;

    @ColumnInfo(name = "custom_name")
    private String customName;

    @ColumnInfo(
            name = "created_at",
            defaultValue = "CURRENT_TIMESTAMP"
    )
    private String updatedAt;


    public KnownUsers(@NonNull String uuid){
        this.uuid = uuid;
    }

    @Ignore
    public KnownUsers(@NonNull String uuid, String userName){
        this.uuid = uuid;
        this.userName = userName;
    }

    public String getUserName() {return userName;}
    public void setUserName(String userName) {this.userName = userName;}

    public String getCustomName() {return customName;}
    public void setCustomName(String customName) {this.customName = customName;}

    public String getUuid() {return uuid;}
    public void setUuid(String uuid) {this.uuid = uuid;}

    public String getUpdatedAt() {return updatedAt;}
    public void setUpdatedAt(String updatedAt) {this.updatedAt = updatedAt;}
}
