package com.telen.easylineup.data

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface PlayerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayer(player: Player): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayers(players: List<Player>): Completable

    @Delete
    fun deletePlayer(player: Player): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updatePlayer(player: Player): Completable

    @Query("SELECT * from players WHERE id = :playerID" )
    fun getPlayerById(playerID: Long): LiveData<Player>

    @Query("SELECT * from players WHERE id = :playerID" )
    fun getPlayerByIdAsSingle(playerID: Long): Single<Player>

    @Query("SELECT * FROM players")
    fun getPlayers(): LiveData<List<Player>>

    @Query("SELECT * FROM players")
    fun getPlayersSingle(): Single<List<Player>>

    @Query("SELECT players.* FROM players INNER JOIN teams ON players.teamID = teams.id WHERE teams.id = :teamId")
    fun getPlayersForTeam(teamId: Long): LiveData<List<Player>>

    @Query("""
        SELECT result.*, position, x, y, `order`, playerFieldPosition.id as fieldPositionID
        FROM (
            SELECT lineups.id as lineupID, 
                players.name as playerName, 
                players.shirtNumber, 
                players.licenseNumber, 
                players.id as playerID, 
                players.teamID, 
                players.image, 
                players.positions as playerPositions
            FROM lineups, players where lineups.id = :lineupID) as result
        LEFT JOIN playerFieldPosition ON playerFieldPosition.lineupID = result.lineupID and playerFieldPosition.playerID = result.playerID
        ORDER BY result.playerID

    """)
    fun getTeamPlayersWithPositions(lineupID: Long): LiveData<List<PlayerWithPosition>>
}