package com.telen.easylineup.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.telen.easylineup.repository.model.RoomPlayerFieldPosition
import com.telen.easylineup.repository.model.RoomPlayerGamesCount
import com.telen.easylineup.repository.model.RoomPlayerWithPosition
import com.telen.easylineup.repository.model.RoomPositionWithLineup
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

@Dao
internal interface PlayerFieldPositionsDao {

    @Query("DELETE FROM playerFieldPosition")
    fun deleteAll(): Completable

    @Insert
    fun insertPlayerFieldPositions(fieldPositions: List<RoomPlayerFieldPosition>): Completable

    @Update
    fun updatePlayerFieldPositions(fieldPositions: List<RoomPlayerFieldPosition>): Completable

    @Update
    fun updatePlayerFieldPositionsWithRowCount(fieldPositions: List<RoomPlayerFieldPosition>): Single<Int>

    @Query("DELETE FROM playerFieldPosition where id=:id")
    fun deletePositionById(id: Long): Completable

    @Delete
    fun deletePositions(position: List<RoomPlayerFieldPosition>): Completable

    @Update
    fun updatePlayerFieldPosition(fieldPosition: RoomPlayerFieldPosition): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayerFieldPosition(fieldPositions: RoomPlayerFieldPosition): Single<Long>

    @Query("SELECT * from playerFieldPosition")
    fun getAllPlayerFieldPositions(): LiveData<List<RoomPlayerFieldPosition>>

    @Query("SELECT * from playerFieldPosition where hash = :hash")
    fun getPlayerFieldPositionByHash(hash: String): Single<RoomPlayerFieldPosition>

    @Query("SELECT * from playerFieldPosition")
    fun getPlayerFieldPositions(): Single<List<RoomPlayerFieldPosition>>

    @Query("SELECT * FROM playerFieldPosition WHERE id = :positionID")
    fun getPlayerFieldPosition(positionID: Long): Single<RoomPlayerFieldPosition>

    @Query(
        """
        SELECT * FROM playerFieldPosition
        WHERE playerFieldPosition.lineupID = :lineupId
    """
    )
    fun getAllPlayerFieldPositionsForLineup(lineupId: Long): Single<List<RoomPlayerFieldPosition>>

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
    fun getAllPlayersWithPositionsForLineup(lineupId: Long): LiveData<List<RoomPlayerWithPosition>>

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
    fun getAllPlayersWithPositionsForLineupRx(lineupId: Long): Single<List<RoomPlayerWithPosition>>

    @Query(
        """
        SELECT playerFieldPosition.* FROM playerFieldPosition
        INNER JOIN players ON playerFieldPosition.playerID = players.id
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        WHERE playerFieldPosition.lineupID = :lineupID AND playerFieldPosition.playerID = :playerID
    """
    )
    fun getPlayerPositionFor(lineupID: Long, playerID: Long): Maybe<RoomPlayerFieldPosition>

    @Query(
        """
        SELECT lineups.name as lineupName, tournaments.name as tournamentName, playerFieldPosition.position, playerFieldPosition.x, playerFieldPosition.y, playerFieldPosition.`order`
        FROM playerFieldPosition
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        INNER JOIN tournaments ON lineups.tournamentID = tournaments.id
        WHERE playerFieldPosition.playerID = :playerID
        ORDER BY lineups.editedAt DESC
    """
    )
    fun getAllPositionsForPlayer(playerID: Long): Single<List<RoomPositionWithLineup>>

    @Query(
        """
        SELECT playerID, COUNT(*) as size FROM playerFieldPosition 
        INNER JOIN lineups ON lineups.id = playerFieldPosition.lineupID
        INNER JOIN teams ON teams.id = lineups.teamID
        WHERE teams.id = :teamID
        GROUP BY playerID ORDER BY 2 DESC
     """
    )
    fun getMostUsedPlayers(teamID: Long): Single<List<RoomPlayerGamesCount>>
}