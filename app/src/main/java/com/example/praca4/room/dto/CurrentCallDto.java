package com.example.praca4.room.dto;

public class CurrentCallDto {

    private final String uuid;

    private  final String ipAddress;
    private final int audioId;

    public CurrentCallDto(String uuid, String ipAddress, int audioId) {
        this.uuid = uuid;
        this.ipAddress = ipAddress;
        this.audioId = audioId;
    }

    public String getUuid() {return uuid;}
    public int getAudioId() {return audioId;}
    public String getIpAddress() {return ipAddress;}
}
