package com.example.praca4.room.entities;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "current_call")
public class CurrentCall {


    @NonNull
    @PrimaryKey
    private String uuid;

    private int audioId;

    public CurrentCall(@NonNull String uuid, int audioId) {
        this.uuid = uuid;
    }

    @NonNull
    public String getUuid() {return uuid;}
    public void setUuid(@NonNull String uuid) {this.uuid = uuid;}
    public int getAudioId() {return audioId;}
    public void setAudioId(int audioId) {this.audioId = audioId;}
}
