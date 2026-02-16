package com.example.praca4.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.praca4.room.dto.CurrentCallDto;
import com.example.praca4.room.entities.CurrentCall;
import com.example.praca4.room.entities.KnownUsers;

import java.util.List;

@Dao
public interface CurrentCallDao {

    @Query("select lnu.ipAddress, lnu.uuid, cc.audioId from LOCAL_NETWORK_USERS lnu join CURRENT_CALL cc on lnu.uuid = cc.uuid")
    LiveData<List<CurrentCallDto>> observeUsers();
    @Insert
    void insertAll(CurrentCall... currentCalls);
    @Query("DELETE FROM current_call")
    void clearTable();
    @Delete
    void delete(CurrentCall currentCall);

}
