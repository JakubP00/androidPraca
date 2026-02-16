package com.example.praca4.room.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.praca4.room.entities.KnownUsers;

import java.util.List;

@Dao
public interface KnownUsersDao {
    @Query("SELECT * FROM known_users")
    List<KnownUsers> getAll();

    @Insert
    void insertAll(KnownUsers... knownUsers);

    @Delete
    void delete(KnownUsers knownUsers);

}
