package com.example.praca4.room.dto;

import com.example.praca4.network.NetworkUtilities;
import com.example.praca4.room.entities.LocalNetworkUsers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class UserDto implements Serializable {

    private final String username;
    private final String uuid;

    private final String ipAddress;
    public UserDto(String username, String uuid) {
        this.username = username;
        this.uuid = uuid;
        ipAddress = NetworkUtilities.getLocalIpAddress().getHostAddress();
    }

    public UserDto(String jsonString){
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            username = jsonObject.getString("username");
            uuid = jsonObject.getString("uuid");
            ipAddress = jsonObject.getString("ipAddress");
        } catch (JSONException e) {
            throw new RuntimeException("jsonString does not match object requirements " + e.getMessage());
        }

    }

    public UserDto(LocalNetworkUsers localNetworkUser){
        username = localNetworkUser.getUserName();
        uuid = localNetworkUser.getUuid();
        ipAddress = localNetworkUser.getIpAddress();
    }


    public JSONObject getJSON(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("uuid", uuid);
            jsonObject.put("ipAddress", ipAddress);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject;
    }


    public String getIpAddress() {return ipAddress;}
    public String getUuid() {return uuid;}
    public String getUsername() {return username;}
}
