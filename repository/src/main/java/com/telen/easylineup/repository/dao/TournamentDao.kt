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
    suspend fun getTournamentByHash(hash: String): RoomTournament

    @Query("SELECT * from tournaments where name = :name")
    suspend fun getTournamentByName(name: String): RoomTournament

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: RoomTournament): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournaments(tournaments: List<RoomTournament>)

    @Update
    suspend fun updateTournament(tournament: RoomTournament)

    @Update
    suspend fun updateTournamentsWithRowCount(tournaments: List<RoomTournament>): Int

    @Delete
    suspend fun deleteTournament(tournament: RoomTournament)

    @Delete
    suspend fun deleteTournaments(tournaments: List<RoomTournament>)
}
