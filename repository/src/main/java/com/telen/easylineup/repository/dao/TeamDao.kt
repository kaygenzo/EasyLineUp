package com.telen.easylineup.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.telen.easylineup.repository.model.RoomTeam
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
internal interface TeamDao {

    @Query("DELETE FROM teams")
    fun deleteAll(): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTeam(team: RoomTeam): Single<Long>

    @Delete
    fun deleteTeam(team: RoomTeam): Completable

    @Delete
    fun deleteTeams(teams: List<RoomTeam>): Completable

    @Update
    fun updateTeam(team: RoomTeam): Completable

    @Update
    fun updateTeams(teams: List<RoomTeam>): Completable

    @Update
    fun updateTeamsWithRowCount(teams: List<RoomTeam>): Single<Int>

    @Query("SELECT * FROM teams WHERE id = :teamId")
    fun getTeamById(teamId: Long): Single<RoomTeam>

    @Query("SELECT * FROM teams WHERE hash = :hash")
    fun getTeamByHash(hash: String): Single<RoomTeam>

    @Query("SELECT * FROM teams")
    fun getTeams(): LiveData<List<RoomTeam>>

    @Query("SELECT * FROM teams")
    fun getTeamsRx(): Single<List<RoomTeam>>
}