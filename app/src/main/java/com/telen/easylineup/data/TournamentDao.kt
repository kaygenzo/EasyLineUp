package com.telen.easylineup.data

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TournamentDao {

    @Query("SELECT * from tournaments ORDER BY createdAt DESC")
    fun getTournaments(): LiveData<List<Tournament>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTournament(tournament: Tournament): Single<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTournaments(tournaments: List<Tournament>): Completable

    @Update
    fun updateTournament(tournament: Tournament): Completable
}