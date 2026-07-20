/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.telen.easylineup.repository.model.RoomPlayerFieldPosition
import com.telen.easylineup.repository.model.RoomPlayerGamesCount
import com.telen.easylineup.repository.model.RoomPlayerWithPosition
import com.telen.easylineup.repository.model.RoomPositionWithLineup

@Dao
internal interface PlayerFieldPositionsDao {
    @Query("DELETE FROM playerFieldPosition")
    suspend fun deleteAll()

    @Insert
    suspend fun insertPlayerFieldPositions(fieldPositions: List<RoomPlayerFieldPosition>)

    @Update
    suspend fun updatePlayerFieldPositions(fieldPositions: List<RoomPlayerFieldPosition>)

    @Update
    suspend fun updatePlayerFieldPositionsWithRowCount(fieldPositions: List<RoomPlayerFieldPosition>):
    Int

    @Query("DELETE FROM playerFieldPosition where id=:id")
    suspend fun deletePositionById(id: Long)

    @Delete
    suspend fun deletePositions(position: List<RoomPlayerFieldPosition>)

    @Update
    suspend fun updatePlayerFieldPosition(fieldPosition: RoomPlayerFieldPosition)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerFieldPosition(fieldPositions: RoomPlayerFieldPosition): Long

    @Query("SELECT * from playerFieldPosition where hash = :hash")
    suspend fun getPlayerFieldPositionByHash(hash: String): RoomPlayerFieldPosition

    @Query("SELECT * from playerFieldPosition")
    suspend fun getPlayerFieldPositions(): List<RoomPlayerFieldPosition>

    @Query("SELECT * FROM playerFieldPosition WHERE id = :positionId")
    suspend fun getPlayerFieldPosition(positionId: Long): RoomPlayerFieldPosition

    @Query(
        """
        SELECT * FROM playerFieldPosition
        WHERE playerFieldPosition.lineupID = :lineupId
    """
    )
    suspend fun getAllPlayerFieldPositionsForLineup(lineupId: Long): List<RoomPlayerFieldPosition>

    @Query(
        """
        SELECT players.name as playerName,
        players.sex as playerSex,
        players.shirtNumber, players.licenseNumber,
        playerFieldPosition.position,
        playerFieldPosition.x, playerFieldPosition.y,
        playerFieldPosition.`order`, playerFieldPosition.id as fieldPositionID,
        playerFieldPosition.lineupID,
        playerFieldPosition.flags,
        players.id as playerID,
        players.teamID, players.image,
        players.positions as playerPositions
        FROM playerFieldPosition
        INNER JOIN players ON playerFieldPosition.playerID = players.id
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        WHERE playerFieldPosition.lineupID = :lineupId
        ORDER BY playerFieldPosition.`order` ASC
    """
    )
    suspend fun getAllPlayersWithPositionsForLineupRx(lineupId: Long): List<RoomPlayerWithPosition>

    @Query(
        """
        SELECT playerFieldPosition.* FROM playerFieldPosition
        INNER JOIN players ON playerFieldPosition.playerID = players.id
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        WHERE playerFieldPosition.lineupID = :lineupId AND playerFieldPosition.playerID = :playerId
    """
    )
    suspend fun getPlayerPositionFor(lineupId: Long, playerId: Long): RoomPlayerFieldPosition?

    @Query(
        """
        SELECT
          lineups.name as lineupName,
          tournaments.name as tournamentName,
          playerFieldPosition.position,
          playerFieldPosition.x,
          playerFieldPosition.y,
          playerFieldPosition.`order`
        FROM playerFieldPosition
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        INNER JOIN tournaments ON lineups.tournamentID = tournaments.id
        WHERE playerFieldPosition.playerID = :playerId
        ORDER BY lineups.editedAt DESC
    """
    )
    suspend fun getAllPositionsForPlayer(playerId: Long): List<RoomPositionWithLineup>

    @Query(
        """
        SELECT playerID, COUNT(*) as size FROM playerFieldPosition
        INNER JOIN lineups ON lineups.id = playerFieldPosition.lineupID
        INNER JOIN teams ON teams.id = lineups.teamID
        WHERE teams.id = :teamId
        GROUP BY playerID ORDER BY 2 DESC
     """
    )
    suspend fun getMostUsedPlayers(teamId: Long): List<RoomPlayerGamesCount>
}
