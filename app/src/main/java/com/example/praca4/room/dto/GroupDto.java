package com.example.praca4.room.dto;

import java.util.List;

public class GroupDto {


    private final String groupName;
    private final List<UserDto> users;

    GroupDto(String groupName, List<UserDto> users){
        this.groupName = groupName;
        this.users = users;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<UserDto> getUsers() {
        return users;
    }
}
