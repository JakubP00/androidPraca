package com.example.praca4.room;


import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.praca4.room.dao.CurrentCallDao;
import com.example.praca4.room.dao.KnownUsersDao;
import com.example.praca4.room.dao.LocalNetworkUsersDao;
import com.example.praca4.room.entities.CurrentCall;
import com.example.praca4.room.entities.KnownUsers;
import com.example.praca4.room.entities.LocalNetworkUsers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@androidx.room.Database(entities = {KnownUsers.class, LocalNetworkUsers.class, CurrentCall.class}, version = 7)
public abstract class Database extends RoomDatabase {
    private static Database INSTANCE;
    public abstract KnownUsersDao knownUsersDao();
    public abstract LocalNetworkUsersDao localNetworkUsersDao();

    public abstract CurrentCallDao currentCallDao();
    public static final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();
    public static Database getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            Database.class,
                            "users_and_groups"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

}
