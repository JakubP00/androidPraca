package com.example.praca4.room.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.praca4.room.dto.UserDto;


@Entity(tableName = "local_network_users",
        indices = {
                @Index(value = {"ipAddress"}, unique = true)
        })
public class LocalNetworkUsers {

    @NonNull
    @PrimaryKey
    private String uuid;

    private String userName;

    private String ipAddress;


    public LocalNetworkUsers(@NonNull String uuid){
        this.uuid = uuid;
    }



    public LocalNetworkUsers(@NonNull UserDto userDto){
        uuid = userDto.getUuid();
        userName = userDto.getUsername();
        ipAddress = userDto.getIpAddress();
    }


    @NonNull
    public String getUuid() {return uuid;}
    public void setUuid(@NonNull String uuid) {this.uuid = uuid;}

    public String getUserName() {return userName;}
    public void setUserName(String userName) {this.userName = userName;}

    public String getIpAddress() {return ipAddress;}
    public void setIpAddress(String ipAddress) {this.ipAddress = ipAddress;}
}
