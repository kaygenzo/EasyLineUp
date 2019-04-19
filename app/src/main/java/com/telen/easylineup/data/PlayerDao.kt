package com.telen.easylineup.data

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Completable

@Dao
interface PlayerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayer(player: Player): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayers(players: List<Player>): Completable

    @Delete
    fun deletePlayer(player: Player)

    @Query("SELECT * from players WHERE id = :playerID" )
    fun getPlayerById(playerID: Long): LiveData<Player>

    @Query("SELECT * FROM players")
    fun getPlayers(): LiveData<List<Player>>

    @Query("SELECT players.* FROM players INNER JOIN teams ON players.teamID = teams.id WHERE teams.id = :teamId")
    fun getPlayersForTeam(teamId: Long): LiveData<List<Player>>
}