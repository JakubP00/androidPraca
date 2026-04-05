package com.example.praca4.room.dto;

public class CurrentCallDto {

    private final String uuid;
    private  final String ipAddress;
    private final int audioId;
    private int state;
    private boolean saved;
    private String userName;




    public CurrentCallDto(String uuid, String ipAddress, int audioId, int state, boolean saved, String userName) {
        this.uuid = uuid;
        this.ipAddress = ipAddress;
        this.audioId = audioId;
        this.state = state;
        this.saved = saved;
        this.userName = userName;
    }

    public String getUuid() {return uuid;}
    public int getAudioId() {return audioId;}
    public String getIpAddress() {return ipAddress;}

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
