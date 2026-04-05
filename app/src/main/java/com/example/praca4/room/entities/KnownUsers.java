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


    private String username;


    private String customName;

    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    private String lastUpdate;


    public KnownUsers(@NonNull String uuid){
        this.uuid = uuid;
    }

    @Ignore
    public KnownUsers(@NonNull String uuid, String username){
        this.uuid = uuid;
        this.username = username;
    }

    public String getUsername() {return username;}
    public void setUsername(String userName) {this.username = userName;}

    public String getCustomName() {return customName;}
    public void setCustomName(String customName) {this.customName = customName;}

    public String getUuid() {return uuid;}
    public void setUuid(String uuid) {this.uuid = uuid;}

    public String getLastUpdate() {return lastUpdate;}
    public void setLastUpdate(String lastUpdate) {this.lastUpdate = lastUpdate;}
}
