package com.telen.easylineup.data

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface LineupDao {
    @Insert
    fun insertLineup(lineup: Lineup): Single<Long>

    @Insert
    fun insertLineup(lineups: List<Lineup>): Completable

    @Update
    fun updateLineup(lineup: Lineup)

    @Delete
    fun deleteLineup(lineup: Lineup): Completable

    @Query("SELECT * FROM lineups ORDER BY id ASC")
    fun getAllLineup(): LiveData<List<Lineup>>

    @Query("SELECT * FROM lineups WHERE id = :lineupId")
    fun getLineupById(lineupId: Long): LiveData<Lineup>

    @Query("SELECT * FROM lineups WHERE id = :lineupId")
    fun getLineupByIdSingle(lineupId: Long): Single<Lineup>

    @Insert
    fun insertPlayerFieldPosition(fieldPositions: List<PlayerFieldPosition>): Completable

    @Update
    fun updatePlayerFieldPositions(fieldPositions: List<PlayerFieldPosition>): Completable

    @Update
    fun updatePlayerFieldPosition(fieldPosition: PlayerFieldPosition): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayerFieldPosition(fieldPositions: PlayerFieldPosition): Single<Long>

    @Query("SELECT * from playerFieldPosition")
    fun getAllPlayerFieldPositions(): LiveData<List<PlayerFieldPosition>>

    @Query("SELECT * FROM playerFieldPosition WHERE id = :positionID")
    fun getPlayerFieldPosition(positionID: Long): Single<PlayerFieldPosition>

    @Query("""
        SELECT playerFieldPosition.* FROM playerFieldPosition
        INNER JOIN players ON playerFieldPosition.playerID = players.id
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        WHERE playerFieldPosition.lineupID = :lineupId
    """)
    fun getAllPlayerFieldPositionsForLineup(lineupId: Long): LiveData<List<PlayerFieldPosition>>

    @Query("""
        SELECT players.name as playerName,
        players.shirtNumber, players.licenseNumber,
        playerFieldPosition.position,
        playerFieldPosition.x, playerFieldPosition.y,
        playerFieldPosition.`order`, playerFieldPosition.id as fieldPositionID,
        playerFieldPosition.lineupID,
        players.id as playerID,
        players.teamID, players.image
        FROM playerFieldPosition
        INNER JOIN players ON playerFieldPosition.playerID = players.id
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        WHERE playerFieldPosition.lineupID = :lineupId
        ORDER BY playerFieldPosition.`order` ASC
    """)
    fun getAllPlayersWithPositionsForLineup(lineupId: Long): LiveData<List<PlayerWithPosition>>

    @Query("""
        SELECT players.name as playerName,
        players.shirtNumber, players.licenseNumber,
        playerFieldPosition.position,
        playerFieldPosition.x, playerFieldPosition.y,
        playerFieldPosition.`order`, playerFieldPosition.id as fieldPositionID,
        playerFieldPosition.lineupID,
        players.id as playerID,
        players.teamID, players.image
        FROM playerFieldPosition
        INNER JOIN players ON playerFieldPosition.playerID = players.id
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        WHERE playerFieldPosition.lineupID = :lineupId
        ORDER BY playerFieldPosition.`order` ASC
    """)
    fun getAllPlayersWithPositionsForLineupRx(lineupId: Long): Maybe<List<PlayerWithPosition>>

    @Query("""
        SELECT playerFieldPosition.* FROM playerFieldPosition
        INNER JOIN players ON playerFieldPosition.playerID = players.id
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        WHERE playerFieldPosition.lineupID = :lineupID AND playerFieldPosition.playerID = :playerID
    """)
    fun getPlayerPositionFor(lineupID: Long, playerID: Long): Maybe<PlayerFieldPosition>

    @Query("""
        SELECT lineups.* FROM lineups
        INNER JOIN tournaments ON lineups.tournamentID = tournaments.id
        INNER JOIN teams ON lineups.teamID = teams.id
        WHERE lineups.tournamentID = :tournamentId
    """)
    fun getLineupsForTournament(tournamentId: Long): LiveData<List<Lineup>>

    @Query("SELECT * FROM lineups ORDER BY editedAt DESC LIMIT 1")
    fun getLastLineup(): Single<Lineup>

    @Query("""
        SELECT lineups.name as lineupName, tournaments.name as tournamentName, playerFieldPosition.position, playerFieldPosition.x, playerFieldPosition.y, playerFieldPosition.`order`
        FROM playerFieldPosition
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        INNER JOIN tournaments ON lineups.tournamentID = tournaments.id
        WHERE playerFieldPosition.playerID = :playerID
        ORDER BY lineups.editedAt DESC
    """)
    fun getAllPositionsForPlayer(playerID: Long): LiveData<List<PositionWithLineup>>

    @Query("""
        SELECT tournaments.id as tournamentID,
        tournaments.name as tournamentName,
        tournaments.createdAt as tournamentCreatedAt,
        playerFieldPosition.id as fieldPositionID,
        lineups.name as lineupName,
        lineups.id as lineupID,
        x, y
        FROM tournaments
        LEFT JOIN lineups ON tournaments.id = lineups.tournamentID
        LEFT JOIN playerFieldPosition ON playerFieldPosition.lineupID = lineups.id
        WHERE tournamentName LIKE '%' || :filter || '%' OR lineupName LIKE '%' || :filter || '%'
        ORDER BY tournaments.createdAt DESC
    """)
    fun getAllTournamentsWithLineups(filter: String): LiveData<List<TournamentWithLineup>>

    @Query("""
        SELECT playerID, COUNT(*) as size FROM playerFieldPosition GROUP BY playerID ORDER BY 2 DESC
     """)
    fun getMostUsedPlayers(): Single<List<PlayerGamesCount>>
}