package com.telen.easylineup.repository.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.telen.easylineup.repository.model.Team
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TeamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTeam(team: Team): Single<Long>

    @Delete
    fun deleteTeam(team: Team): Completable

    @Delete
    fun deleteTeams(team: List<Team>): Completable

    @Update
    fun updateTeam(team: Team): Completable

    @Update
    fun updateTeams(team: List<Team>): Completable

    @Update
    fun updateTeamsWithRowCount(team: List<Team>): Single<Int>

    @Query("SELECT * FROM teams WHERE id = :teamId")
    fun getTeamById(teamId: Long): Single<Team>

    @Query("SELECT * FROM teams WHERE hash = :hash")
    fun getTeamByHash(hash: String): Single<Team>

    @Query("SELECT * FROM teams")
    fun getTeams(): LiveData<List<Team>>

    @Query("SELECT * FROM teams")
    fun getTeamsRx(): Single<List<Team>>
}