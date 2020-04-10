package com.telen.easylineup.repository.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.telen.easylineup.repository.model.PlayerFieldPosition
import com.telen.easylineup.repository.model.PlayerGamesCount
import com.telen.easylineup.repository.model.PlayerWithPosition
import com.telen.easylineup.repository.model.PositionWithLineup
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface PlayerFieldPositionsDao {
    @Insert
    fun insertPlayerFieldPositions(fieldPositions: List<PlayerFieldPosition>): Completable

    @Update
    fun updatePlayerFieldPositions(fieldPositions: List<PlayerFieldPosition>): Completable

    @Update
    fun updatePlayerFieldPositionsWithRowCount(fieldPositions: List<PlayerFieldPosition>): Single<Int>

    @Delete
    fun deletePosition(position: PlayerFieldPosition): Completable

    @Update
    fun updatePlayerFieldPosition(fieldPosition: PlayerFieldPosition): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayerFieldPosition(fieldPositions: PlayerFieldPosition): Single<Long>

    @Query("SELECT * from playerFieldPosition")
    fun getAllPlayerFieldPositions(): LiveData<List<PlayerFieldPosition>>

    @Query("SELECT * from playerFieldPosition where hash = :hash")
    fun getPlayerFieldPositionByHash(hash: String): Single<PlayerFieldPosition>

    @Query("SELECT * from playerFieldPosition")
    fun getPlayerFieldPositions(): Single<List<PlayerFieldPosition>>

    @Query("SELECT * FROM playerFieldPosition WHERE id = :positionID")
    fun getPlayerFieldPosition(positionID: Long): Single<PlayerFieldPosition>

    @Query("""
        SELECT * FROM playerFieldPosition
        WHERE playerFieldPosition.lineupID = :lineupId
    """)
    fun getAllPlayerFieldPositionsForLineup(lineupId: Long): Single<List<PlayerFieldPosition>>

    @Query("""
        SELECT players.name as playerName,
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
    """)
    fun getAllPlayersWithPositionsForLineup(lineupId: Long): LiveData<List<PlayerWithPosition>>

//    @Query("""
//        SELECT players.name as playerName,
//        players.shirtNumber, players.licenseNumber,
//        playerFieldPosition.position,
//        playerFieldPosition.x, playerFieldPosition.y,
//        playerFieldPosition.`order`, playerFieldPosition.id as fieldPositionID,
//        playerFieldPosition.lineupID,
//        players.id as playerID,
//        players.teamID, players.image,
//        players.positions as playerPositions
//        FROM playerFieldPosition
//        INNER JOIN players ON playerFieldPosition.playerID = players.id
//        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
//        WHERE playerFieldPosition.lineupID = :lineupId
//        ORDER BY playerFieldPosition.`order` ASC
//    """)
//    fun getAllPlayersWithPositionsForLineupRx(lineupId: Long): Maybe<List<PlayerWithPosition>>

    @Query("""
        SELECT playerFieldPosition.* FROM playerFieldPosition
        INNER JOIN players ON playerFieldPosition.playerID = players.id
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        WHERE playerFieldPosition.lineupID = :lineupID AND playerFieldPosition.playerID = :playerID
    """)
    fun getPlayerPositionFor(lineupID: Long, playerID: Long): Maybe<PlayerFieldPosition>

    @Query("""
        SELECT lineups.name as lineupName, tournaments.name as tournamentName, playerFieldPosition.position, playerFieldPosition.x, playerFieldPosition.y, playerFieldPosition.`order`
        FROM playerFieldPosition
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        INNER JOIN tournaments ON lineups.tournamentID = tournaments.id
        WHERE playerFieldPosition.playerID = :playerID
        ORDER BY lineups.editedAt DESC
    """)
    fun getAllPositionsForPlayer(playerID: Long): Single<List<PositionWithLineup>>

    @Query("""
        SELECT playerID, COUNT(*) as size FROM playerFieldPosition 
        INNER JOIN lineups ON lineups.id = playerFieldPosition.lineupID
        INNER JOIN teams ON teams.id = lineups.teamID
        WHERE teams.id = :teamID
        GROUP BY playerID ORDER BY 2 DESC
     """)
    fun getMostUsedPlayers(teamID: Long): Single<List<PlayerGamesCount>>
}