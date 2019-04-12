package com.telen.easylineup.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TournamentDao {

    @Query("SELECT * from tournaments ORDER BY name ASC")
    fun getTournaments(): LiveData<List<Tournament>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTournament(tournament: Tournament): Single<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTournaments(tournaments: List<Tournament>): Completable
}