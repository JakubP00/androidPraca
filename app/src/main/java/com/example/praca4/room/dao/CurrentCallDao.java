package com.example.praca4.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.praca4.room.dto.CurrentCallDto;
import com.example.praca4.room.entities.CurrentCall;

import java.util.List;

@Dao
public interface CurrentCallDao {

//    @Query("select lnu.ipAddress, lnu.uuid, cc.audioId from LOCAL_NETWORK_USERS lnu join CURRENT_CALL cc on lnu.uuid = cc.uuid")
//    LiveData<List<CurrentCallDto>> observeUsers();
//

    @Query("select lnu.ipAddress as ipAddress, lnu.uuid as uuid, cc.audioId as audioId, cc.state as state, COALESCE(NULLIF(ku.customName, ''), lnu.userName) as userName, ku.uuid is not null as saved  from CURRENT_CALL cc left join LOCAL_NETWORK_USERS lnu on lnu.uuid = cc.uuid left join KNOWN_USERS ku on ku.uuid = cc.uuid where cc.uuid = :uuid")
    CurrentCallDto getUserData(String uuid);


    @Query("select cc.uuid from CURRENT_CALL cc")
    LiveData<List<String>> observeUsersInCall();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(CurrentCall... currentCalls);
    @Query("DELETE FROM current_call")
    void clearTable();
    @Delete
    void delete(CurrentCall currentCall);

}
