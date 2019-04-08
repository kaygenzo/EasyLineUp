package com.telen.easylineup.data

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Completable

@Dao
interface TeamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTeam(team: Team): Completable

    @Delete
    fun deleteTeam(team: Team)

    @Update
    fun updateTeam(team: Team)

    @Query("SELECT * FROM teams WHERE id = :teamId")
    fun getTeamById(teamId: Int): LiveData<Team>

    @Query("SELECT * FROM teams")
    fun getTeams(): LiveData<List<Team>>
}