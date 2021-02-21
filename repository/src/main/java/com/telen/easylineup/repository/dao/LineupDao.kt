package com.telen.easylineup.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.telen.easylineup.repository.model.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
internal interface LineupDao {

    @Query("DELETE FROM lineups")
    fun deleteAll(): Completable

    @Insert
    fun insertLineup(lineup: RoomLineup): Single<Long>

    @Insert
    fun insertLineups(lineups: List<RoomLineup>): Completable

    @Update
    fun updateLineup(lineup: RoomLineup): Completable

    @Update
    fun updateLineupsWithRowCount(lineups: List<RoomLineup>): Single<Int>

    @Delete
    fun deleteLineup(lineup: RoomLineup): Completable

    @Delete
    fun deleteLineups(lineups: List<RoomLineup>): Completable

    @Query("SELECT * FROM lineups ORDER BY id ASC")
    fun getAllLineup(): LiveData<List<RoomLineup>>

    @Query("SELECT * FROM lineups")
    fun getLineups(): Single<List<RoomLineup>>

    @Query("SELECT * FROM lineups WHERE id = :lineupId")
    fun getLineupById(lineupId: Long): LiveData<RoomLineup>

    @Query("SELECT * FROM lineups WHERE hash = :hash")
    fun getLineupByHash(hash: String): Single<RoomLineup>

    @Query("SELECT * FROM lineups WHERE id = :lineupId")
    fun getLineupByIdSingle(lineupId: Long): Single<RoomLineup>

    @Query("""
        SELECT lineups.* FROM lineups
        INNER JOIN tournaments ON lineups.tournamentID = tournaments.id
        INNER JOIN teams ON lineups.teamID = teams.id
        WHERE lineups.tournamentID = :tournamentId AND lineups.teamID = :teamID
    """)
    fun getLineupsForTournament(tournamentId: Long, teamID: Long): LiveData<List<RoomLineup>>

    @Query("""
        SELECT * FROM lineups
        WHERE lineups.tournamentID = :tournamentId AND lineups.teamID = :teamID
    """)
    fun getLineupsForTournamentRx(tournamentId: Long, teamID: Long): Single<List<RoomLineup>>

    @Query("SELECT * FROM lineups WHERE teamID = :teamID ORDER BY editedAt DESC LIMIT 1")
    fun getLastLineup(teamID: Long): Maybe<RoomLineup>

    @Query("""
        SELECT tournaments.id as tournamentID,
        tournaments.name as tournamentName,
        tournaments.createdAt as tournamentCreatedAt,
        playerFieldPosition.id as fieldPositionID,
        lineups.name as lineupName,
        lineups.id as lineupID,
        lineups.teamID as teamID,
        lineups.eventTime as lineupEventTime,
        lineups.createdAt as lineupCreatedTime,
        lineups.roaster as roster,
        lineups.mode as lineupMode,
        lineups.strategy as lineupStrategy,
        lineups.extraHitters as lineupExtraHittersSize,
        x, y, position
        FROM tournaments
        LEFT JOIN lineups ON tournaments.id = lineups.tournamentID
        LEFT JOIN playerFieldPosition ON playerFieldPosition.lineupID = lineups.id
        WHERE teamID = :teamID AND (tournamentName LIKE '%' || :filter || '%' OR lineupName LIKE '%' || :filter || '%')
        ORDER BY tournaments.createdAt DESC
    """)
    fun getAllTournamentsWithLineups(filter: String, teamID: Long): Single<List<RoomTournamentWithLineup>>

    @Query("""
        SELECT
        lineups.name as lineupName,
        lineups.id as lineupID,
        position,
        players.name as playerName,
        players.id as playerID
        FROM lineups
        LEFT JOIN playerFieldPosition ON playerFieldPosition.lineupID = lineups.id
        LEFT JOIN players ON playerFieldPosition.playerID = players.id
        WHERE lineups.teamID = :teamID AND lineups.tournamentID = :tournamentId
    """)
    fun getAllPlayerPositionsForTournament(tournamentId: Long, teamID: Long): Single<List<RoomPlayerInLineup>>
}