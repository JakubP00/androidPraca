package com.example.praca4.room.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.praca4.room.entities.LocalNetworkUsers;

import java.util.List;

@Dao
public interface LocalNetworkUsersDao {

    @Query("SELECT * FROM local_network_users")
    List<LocalNetworkUsers> getAll();

    @Query("SELECT * FROM local_network_users where ipAddress = :ipAddress")
    LocalNetworkUsers getByIpAddress(String ipAddress);



    @Query("SELECT * FROM local_network_users")
    LiveData<List<LocalNetworkUsers>> observeUsers();

    @Query("DELETE FROM local_network_users")
    void clearTable();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(LocalNetworkUsers... localNetworkUser);

    @Delete
    void delete(LocalNetworkUsers localNetworkUser);

}
