package com.telen.easylineup.data

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TeamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTeam(team: Team): Completable

    @Delete
    fun deleteTeam(team: Team)

    @Update
    fun updateTeam(team: Team): Completable

    @Query("SELECT * FROM teams WHERE id = :teamId")
    fun getTeamById(teamId: Long): LiveData<Team>

    @Query("SELECT * FROM teams")
    fun getTeams(): LiveData<List<Team>>

    @Query("SELECT * FROM teams")
    fun getTeamsList(): Single<List<Team>>
}