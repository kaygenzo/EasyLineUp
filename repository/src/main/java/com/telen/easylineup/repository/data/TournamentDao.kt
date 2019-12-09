package com.telen.easylineup.repository.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TournamentDao {

    @Query("SELECT * from tournaments ORDER BY createdAt DESC")
    fun getTournaments(): Single<List<Tournament>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTournament(tournament: Tournament): Single<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTournaments(tournaments: List<Tournament>): Completable

    @Update
    fun updateTournament(tournament: Tournament): Completable

    @Delete
    fun deleteTournament(tournament: Tournament): Completable
}