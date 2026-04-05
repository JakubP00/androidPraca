package com.example.praca4.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "groups")
public class Groups {

    @NonNull
    @PrimaryKey
    private String uuid;

    private String groupName;

    private String customName;

    private String ownerUuid;

    public Groups(@NonNull String uuid, String groupName, String ownerUuid) {
        this.uuid = uuid;
        this.groupName = groupName;
        this.ownerUuid = ownerUuid;
    }

    @NonNull
    public String getUuid() {
        return uuid;
    }

    public void setUuid(@NonNull String uuid) {
        this.uuid = uuid;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(String ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }
}
