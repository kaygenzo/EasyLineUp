/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.telen.easylineup.repository.model.RoomTeam
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TeamDao {
    @Query("DELETE FROM teams")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: RoomTeam): Long

    @Delete
    suspend fun deleteTeam(team: RoomTeam)

    @Delete
    suspend fun deleteTeams(teams: List<RoomTeam>)

    @Update
    suspend fun updateTeam(team: RoomTeam)

    @Update
    suspend fun updateTeams(teams: List<RoomTeam>)

    @Update
    suspend fun updateTeamsWithRowCount(teams: List<RoomTeam>): Int

    @Query("SELECT * FROM teams WHERE id = :teamId")
    suspend fun getTeamById(teamId: Long): RoomTeam

    @Query("SELECT * FROM teams WHERE hash = :hash")
    suspend fun getTeamByHash(hash: String): RoomTeam

    @Query("SELECT * FROM teams")
    fun getTeams(): Flow<List<RoomTeam>>

    @Query("SELECT * FROM teams")
    suspend fun getTeamsRx(): List<RoomTeam>
}
