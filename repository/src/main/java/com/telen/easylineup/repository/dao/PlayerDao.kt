package com.telen.easylineup.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.telen.easylineup.repository.model.RoomPlayer
import com.telen.easylineup.repository.model.RoomPlayerWithPosition
import io.reactivex.Completable
import io.reactivex.Single

@Dao
internal interface PlayerDao {

    @Query("DELETE FROM players")
    fun deleteAll(): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayer(player: RoomPlayer): Single<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayers(players: List<RoomPlayer>): Completable

    @Delete
    fun deletePlayer(player: RoomPlayer): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updatePlayer(player: RoomPlayer): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updatePlayersWithRowCount(players: List<RoomPlayer>): Single<Int>

    @Query("SELECT * from players WHERE hash = :hash" )
    fun getPlayerByHash(hash: String): Single<RoomPlayer>

    @Query("SELECT * from players WHERE id = :playerID" )
    fun getPlayerById(playerID: Long): LiveData<RoomPlayer>

    @Query("SELECT * from players WHERE id = :playerID" )
    fun getPlayerByIdAsSingle(playerID: Long): Single<RoomPlayer>

//    @Query("SELECT * FROM players WHERE players.teamID = :teamID")
//    fun getPlayers(teamID: Long): LiveData<List<Player>>

    @Query("SELECT * FROM players WHERE players.teamID = :teamID")
    fun getPlayers(teamID: Long): Single<List<RoomPlayer>>

    @Query("SELECT * FROM players")
    fun getPlayers(): Single<List<RoomPlayer>>

    @Query("""
        SELECT result.*, position, x, y, `order`, playerFieldPosition.id as fieldPositionID, flags
        FROM (
            SELECT lineups.id as lineupID, 
                players.name as playerName, 
                players.shirtNumber, 
                players.licenseNumber, 
                players.id as playerID, 
                players.teamID, 
                players.image, 
                players.positions as playerPositions
            FROM lineups, players where lineups.id = :lineupID AND players.teamID = lineups.teamID) as result
        LEFT JOIN playerFieldPosition ON playerFieldPosition.lineupID = result.lineupID and playerFieldPosition.playerID = result.playerID
        ORDER BY result.playerID

    """)
    fun getTeamPlayersAndMaybePositions(lineupID: Long): LiveData<List<RoomPlayerWithPosition>>
}