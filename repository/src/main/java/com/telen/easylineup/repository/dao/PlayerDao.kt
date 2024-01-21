/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.telen.easylineup.repository.model.RoomPlayer
import com.telen.easylineup.repository.model.RoomPlayerWithPosition
import com.telen.easylineup.repository.model.RoomShirtNumberEntry
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

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

    @Query("SELECT * from players WHERE hash = :hash")
    fun getPlayerByHash(hash: String): Single<RoomPlayer>

    @Query("SELECT * from players WHERE id = :playerId")
    fun getPlayerById(playerId: Long): LiveData<RoomPlayer?>

    @Query("SELECT * from players WHERE id = :playerId")
    fun getPlayerByIdAsSingle(playerId: Long): Single<RoomPlayer>

    // @Query("SELECT * FROM players WHERE players.teamID = :teamID")
    // fun getPlayers(teamID: Long): LiveData<List<Player>>

    @Query("SELECT * FROM players WHERE players.teamID = :teamId")
    fun getPlayersByTeamId(teamId: Long): Single<List<RoomPlayer>>

    @Query("SELECT * FROM players")
    fun getPlayers(): Single<List<RoomPlayer>>

    @Query("SELECT * FROM players WHERE players.teamID = :teamId")
    fun getPlayersAsLiveData(teamId: Long): LiveData<List<RoomPlayer>>

    @Query(
        """
        SELECT result.*, position, x, y, `order`, playerFieldPosition.id as fieldPositionID, flags
        FROM (
            SELECT lineups.id as lineupID,
                players.name as playerName,
                players.sex as playerSex,
                players.shirtNumber,
                players.licenseNumber,
                players.id as playerID,
                players.teamID,
                players.image,
                players.positions as playerPositions
            FROM lineups, players
            where lineups.id = :lineupId AND players.teamID = lineups.teamID)
            as result
        LEFT JOIN playerFieldPosition ON playerFieldPosition.lineupID = result.lineupID
          and playerFieldPosition.playerID = result.playerID
        ORDER BY result.playerID
    """
    )
    fun getTeamPlayersAndMaybePositions(lineupId: Long): LiveData<List<RoomPlayerWithPosition>>

    @Query(
        """
        SELECT players.name as playerName,
        players.shirtNumber as number,
        players.id as playerID,
        lineups.eventTime as time,
        lineups.createdAt as createdAt,
        lineups.id as lineupID,
        lineups.name as lineupName
        FROM players
        LEFT JOIN playerFieldPosition ON playerFieldPosition.playerID = players.id
        LEFT JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        WHERE players.teamID = :teamId AND lineupID > 0 AND number = :number;
    """
    )
    fun getShirtNumberHistoryFromPlayers(
        teamId: Long,
        number: Int
    ): Single<List<RoomShirtNumberEntry>>

    @Query(
        """
        SELECT players.name as playerName,
        playerNumberOverlay.number as number,
        players.id as playerID,
        lineups.eventTime as time,
        lineups.createdAt as createdAt,
        lineups.id as lineupID,
        lineups.name as lineupName
        FROM players
        LEFT JOIN playerNumberOverlay ON playerNumberOverlay.playerID = players.id
        LEFT JOIN lineups ON playerNumberOverlay.lineupID = lineups.id
        WHERE players.teamID = :teamId AND number = :number;
    """
    )
    fun getShirtNumberHistoryFromOverlays(
        teamId: Long,
        number: Int
    ): Single<List<RoomShirtNumberEntry>>
}
