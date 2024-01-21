/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.telen.easylineup.repository.model.RoomTournament
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
internal interface TournamentDao {
    @Query("DELETE FROM tournaments")
    fun deleteAll(): Completable

    @Query("SELECT * from tournaments ORDER BY createdAt DESC")
    fun getTournaments(): Single<List<RoomTournament>>

    @Query("SELECT * from tournaments ORDER BY createdAt DESC")
    fun observeTournaments(): LiveData<List<RoomTournament>>

    @Query("SELECT * from tournaments where hash = :hash")
    fun getTournamentByHash(hash: String): Single<RoomTournament>

    @Query("SELECT * from tournaments where name = :name")
    fun getTournamentByName(name: String): Single<RoomTournament>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTournament(tournament: RoomTournament): Single<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTournaments(tournaments: List<RoomTournament>): Completable

    @Update
    fun updateTournament(tournament: RoomTournament): Completable

    @Update
    fun updateTournamentsWithRowCount(tournaments: List<RoomTournament>): Single<Int>

    @Delete
    fun deleteTournament(tournament: RoomTournament): Completable

    @Delete
    fun deleteTournaments(tournaments: List<RoomTournament>): Completable
}
