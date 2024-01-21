/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.telen.easylineup.repository.model.RoomLineup
import com.telen.easylineup.repository.model.RoomPlayerInLineup
import com.telen.easylineup.repository.model.RoomTournamentWithLineup
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

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
    fun getLineupById(lineupId: Long): LiveData<RoomLineup?>

    @Query("SELECT * FROM lineups WHERE hash = :hash")
    fun getLineupByHash(hash: String): Single<RoomLineup>

    @Query("SELECT * FROM lineups WHERE id = :lineupId")
    fun getLineupByIdSingle(lineupId: Long): Single<RoomLineup>

    @Query(
        """
        SELECT lineups.* FROM lineups
        INNER JOIN tournaments ON lineups.tournamentID = tournaments.id
        INNER JOIN teams ON lineups.teamID = teams.id
        WHERE lineups.tournamentID = :tournamentId AND lineups.teamID = :teamId
    """
    )
    fun getLineupsForTournament(tournamentId: Long, teamId: Long): LiveData<List<RoomLineup>>

    @Query(
        """
        SELECT * FROM lineups
        WHERE lineups.tournamentID = :tournamentId AND lineups.teamID = :teamId
    """
    )
    fun getLineupsForTournamentRx(tournamentId: Long, teamId: Long): Single<List<RoomLineup>>

    @Query("SELECT * FROM lineups WHERE teamID = :teamId ORDER BY editedAt DESC LIMIT 1")
    fun getLastLineup(teamId: Long): Maybe<RoomLineup>

    @Query(
        """
        SELECT tournaments.id as tournamentID,
        tournaments.name as tournamentName,
        tournaments.createdAt as tournamentCreatedAt,
        tournaments.startTime as tournamentStartTime,
        tournaments.endTime as tournamentEndTime,
        tournaments.address as tournamentAddress,
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
        WHERE teamID = :teamId AND (tournamentName LIKE '%' || :filter || '%' OR lineupName LIKE '%' || :filter || '%')
        ORDER BY tournaments.createdAt DESC
    """
    )
    fun getAllTournamentsWithLineups(filter: String, teamId: Long): Single<List<RoomTournamentWithLineup>>

    @Query(
        """
        SELECT
        lineups.name as lineupName,
        lineups.id as lineupID,
        position,
        players.name as playerName,
        players.id as playerID
        FROM lineups
        LEFT JOIN playerFieldPosition ON playerFieldPosition.lineupID = lineups.id
        LEFT JOIN players ON playerFieldPosition.playerID = players.id
        WHERE lineups.teamID = :teamId AND lineups.tournamentID = :tournamentId
    """
    )
    fun getAllPlayerPositionsForTournament(tournamentId: Long, teamId: Long): Single<List<RoomPlayerInLineup>>
}
