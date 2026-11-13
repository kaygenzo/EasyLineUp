/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.telen.easylineup.repository.model.RoomTournament
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TournamentDao {
    @Query("DELETE FROM tournaments")
    suspend fun deleteAll()

    @Query("SELECT * from tournaments ORDER BY createdAt DESC")
    suspend fun getTournaments(): List<RoomTournament>

    @Query("SELECT * from tournaments ORDER BY createdAt DESC")
    fun observeTournaments(): Flow<List<RoomTournament>>

    @Query("SELECT * from tournaments where hash = :hash")
    suspend fun getTournamentByHash(hash: String): RoomTournament?

    @Query("SELECT * from tournaments where name = :name")
    suspend fun getTournamentByName(name: String): RoomTournament?

    // Not REPLACE: with the unique name index, REPLACE would delete the tournament already
    // using that name, and every lineup in it through the cascade, instead of failing.
    @Insert
    suspend fun insertTournament(tournament: RoomTournament): Long

    @Update
    suspend fun updateTournament(tournament: RoomTournament)

    @Update
    suspend fun updateTournamentsWithRowCount(tournaments: List<RoomTournament>): Int

    @Delete
    suspend fun deleteTournament(tournament: RoomTournament)

    @Delete
    suspend fun deleteTournaments(tournaments: List<RoomTournament>)
}
