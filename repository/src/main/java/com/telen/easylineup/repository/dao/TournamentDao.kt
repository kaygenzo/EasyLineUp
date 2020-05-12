package com.telen.easylineup.repository.dao

import androidx.room.*
import com.telen.easylineup.repository.model.RoomTournament
import io.reactivex.Completable
import io.reactivex.Single

@Dao
internal interface TournamentDao {

    @Query("DELETE FROM tournaments")
    fun deleteAll(): Completable

    @Query("SELECT * from tournaments ORDER BY createdAt DESC")
    fun getTournaments(): Single<List<RoomTournament>>

    @Query("SELECT * from tournaments where hash = :hash")
    fun getTournamentByHash(hash: String): Single<RoomTournament>

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