package com.example.praca4.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.praca4.room.dto.GroupMemberDto;
import com.example.praca4.room.entities.GroupMembers;

import java.util.List;

@Dao
public interface GroupMemberDao {


    @Query("select ku.username as username, gm.id, lnu.ipAddress, gm.uuid, gm.state from group_members gm join known_users ku on ku.uuid = gm.uuid left join LOCAL_NETWORK_USERS lnu on lnu.uuid = gm.uuid where gm.groupId = :id" )
    List<GroupMemberDto> getByGroupId(long id);

    @Insert
    void insertAll(GroupMembers... groupMembers );

    @Delete
    void delete(GroupMembers groupMember);

}
