package com.example.praca4.managers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.example.praca4.room.dto.UserDto;

import java.util.UUID;

public class UserDataManager {

    private final SharedPreferences sharedPreferences;


    public UserDataManager(Context context){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context );
    }
    public boolean appWasInitialized(){
        return sharedPreferences.getBoolean("completed_profile", false);
    }

    public void setAppWasInitialized(){
        sharedPreferences.edit().putBoolean("completed_profile", true).apply();
    }

    public static boolean validUsername(String userName){
        return userName.length() > 9;
    }

    public void setUserName(String userName) throws IncorrectUsername {
        if(userName == null || userName.length() < 11)
            throw new IncorrectUsername("User name must contain at least 10 characters");
        sharedPreferences.edit().putString("username", userName).apply();
    }

    public void generateUUID(){
        String uuidString = sharedPreferences.getString("uuid", null);
        if(uuidString == null) {
            UUID uuid = UUID.randomUUID();
            sharedPreferences.edit().putString("uuid", uuid.toString()).apply();
        }
    }

    public String getUUID() throws NotInitialized {
        String uuid = sharedPreferences.getString("uuid", null);
        if(uuid == null)
            throw new NotInitialized();
        return uuid;
    }

    public String getUsername() throws NotInitialized {
        String userName = sharedPreferences.getString("username", null);
        if(userName == null)
            throw new NotInitialized();
        return userName;
    }

    public UserDto getUserDto() throws NotInitialized {
        String uuid = sharedPreferences.getString("uuid", null);
        if(uuid == null)
            throw new NotInitialized();
        String userName = sharedPreferences.getString("username", null);
        if(userName == null)
            throw new NotInitialized();

        return new UserDto(userName, uuid);
    }


    public static class IncorrectUsername extends Exception{
        private final String massage;
        public IncorrectUsername(String massage) {
            this.massage = massage;
        }
        public String getMassage() {return massage;}
    }
    public static class NotInitialized extends Exception{
        public NotInitialized() {}
        public String getMassage() {return "App was not initialized";}
    }

}
