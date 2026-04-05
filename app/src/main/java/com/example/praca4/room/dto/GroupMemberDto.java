package com.example.praca4.room.dto;

public class GroupMemberDto {


    private final String username;
    private final long id;

    private final String ipAddress;
    private final String uuid;
    private final String state;

    public GroupMemberDto(String username, long id, String ipAddress, String uuid, String state) {
        this.username = username;
        this.id = id;
        this.ipAddress = ipAddress;
        this.uuid = uuid;
        this.state = state;
    }

    public String getUsername() {return username;}

    public String getUuid() {return uuid;}
    public String getState() {return state;}

    public long getId() {return id;}

    public String getIpAddress() {return ipAddress;}
}
